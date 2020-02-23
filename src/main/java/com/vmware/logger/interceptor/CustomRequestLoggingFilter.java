package com.vmware.logger.interceptor;

import org.slf4j.MDC;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;


public class CustomRequestLoggingFilter extends CommonsRequestLoggingFilter {


    private static final String DELIMITER = "::";
    private static final String NOTIFICATION_PREFIX = "* ";
    private static final String REQUEST_PREFIX = "> ";
    private static final String RESPONSE_PREFIX = "< ";

    private AtomicLong reqCounter = new AtomicLong(0);
    private AtomicLong liveReqCounter = new AtomicLong(0);

    public static int readFully(InputStream in, byte[] b, int off, int len) throws IOException {
        return read(in, b, off, len);
    }

    public static int readFully(InputStream in, byte[] b) throws IOException {
        return readFully(in, b, 0, b.length);
    }

    private static int read(InputStream in, byte[] b, int off, int len) throws IOException {
        if (len < 0) {
            throw new IndexOutOfBoundsException("len is negative");
        }
        int total = 0;
        while (total < len) {
            int result = in.read(b, off + total, len - total);
            if (result == -1) {
                break;
            }
            total += result;
        }
        return total;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Skip all the swagger api rest calls from logRequest.
        if (skipLogging(request, response, filterChain)) {
            return;
        }

        liveReqCounter.addAndGet(1);
        long startTime = System.currentTimeMillis();
        long id = reqCounter.incrementAndGet();
        boolean isFirstRequest = !isAsyncDispatch(request);
        HttpServletRequest requestToUse = request;

        if (isIncludePayload() && isFirstRequest
                && !(request instanceof ContentCachingRequestWrapper)) {
            requestToUse = new CustomContentCachingRequestWrapper(request, getMaxPayloadLength());
        }

        HttpServletResponse responseToUse = response;
        if (!(response instanceof ContentCachingResponseWrapper)) {
            responseToUse = new ContentCachingResponseWrapper(response);
        }

        logRequest(filterChain, startTime, id, isFirstRequest, requestToUse, responseToUse);
    }

    private void logRequest(FilterChain filterChain,
                            long startTime, long id, boolean isFirstRequest,
                            HttpServletRequest requestToUse,
                            HttpServletResponse responseToUse)
            throws IOException, ServletException {

        boolean shouldLog = shouldLog(requestToUse);

        if (shouldLog && isFirstRequest) {
            beforeRequest(requestToUse, id);
        }

        try {
            filterChain.doFilter(requestToUse, responseToUse);
        } finally {
            if (shouldLog && !isAsyncStarted(requestToUse)) {
                afterRequest(responseToUse, id, startTime);
            }
            liveReqCounter.addAndGet(-1);
        }
    }

    private boolean skipLogging(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain)
            throws IOException, ServletException {

        if (request.getRequestURI().contains("swagger")
                || request.getRequestURI().contains("/v2/api-docs")
                || request.getRequestURI().startsWith("/cloudfoundryapplication")) {
            filterChain.doFilter(request, response);
            return true;
        }
        return false;
    }

    private String getResponseContent(HttpServletResponse resp) {

        ContentCachingResponseWrapper responseWrapper = WebUtils.getNativeResponse(resp, ContentCachingResponseWrapper.class);
        String payload = "";
        if (responseWrapper != null) {
            byte[] buf = responseWrapper.getContentAsByteArray();
            try {
                responseWrapper.copyBodyToResponse();
            } catch (IOException e) {
                logger.error("Fail to write response body back", e);
            }
            if (buf.length > 0) {
                payload = "";
                try {
                    payload = new String(buf, 0, buf.length, responseWrapper.getCharacterEncoding());
                } catch (UnsupportedEncodingException ex) {
                    payload = "[unknown]";
                    logger.info("UnsupportedEncodingException : " + ex);
                }

            }
        }
        return payload;
    }


    //org.slf4j.MDC.put("transactionId", transactionId);

    @Override
    protected boolean shouldLog(HttpServletRequest request) {
        return logger.isDebugEnabled();
    }

    /**
     * Writes a log message before the request is processed.
     *
     * @param id
     */
    protected void beforeRequest(HttpServletRequest request, long id) {

        final StringBuilder b = new StringBuilder();
        printRequestLine(b, request, id);

        printRequestHeaders(b, request.getHeaderNames(), request, id);
        setTransactionIDForLog(request);

        try {
            InputStream in = request.getInputStream();

            final byte[] entity = new byte[100000];
            int dataLengRead = readFully(in, entity);
            String reqBody = new String(entity, 0, dataLengRead, "UTF-8").replaceAll("\n", "").replaceAll("\r", "").replaceAll("\r\n", "").replaceAll("\n\r", "");


            String OrderID = LogUtil.getOrderIdFromReqJson(reqBody);
            String correlationId = LogUtil.getcorrelationIdFromReq(request);

            MDC.put("transactionId", correlationId + ":" + OrderID);

            b.append(reqBody);
            //b.append("::");

            ((CustomContentCachingRequestWrapper) request).appendStream(entity, 0, dataLengRead);

        } catch (IOException ex) {
            logger.error("IoException beforeRequest call: ", ex);
        }


        logger.debug("" + b);
    }

    private void printRequestLine(StringBuilder b, HttpServletRequest request, long id) {
        prefixId(b, id).append(NOTIFICATION_PREFIX).append("Server received in-bound request: Live Thread Count " + liveReqCounter.get() + " : ");
        b.append(LogUtil.getCurrentHeapSize())
                .append(LogUtil.getMaxHeapSize())
                .append(LogUtil.getFreeHeapSize())
                .append(DELIMITER);
        prefixId(b, id).append(REQUEST_PREFIX).append(request.getMethod()).append(" ").
                append(request.getRequestURI()).append(DELIMITER);
    }

    /**
     * Writes a log message after the request is processed.
     *
     * @param response
     * @param id
     * @param startTime
     */
    //@Override
    protected void afterRequest(HttpServletResponse response, long id, long startTime) {
        final StringBuilder b = new StringBuilder();
        printResponseLine(b, "Server responded with a response : Live Thread Count " + liveReqCounter.get() + " : ", id, response.getStatus(), startTime);
        printResponseHeaders(b, id, response);
        b.append(getResponseContent(response));
        logger.debug("" + b + DELIMITER);
    }

    private void printResponseHeaders(StringBuilder b, long id, HttpServletResponse response) {
        Collection<String> headerNames = response.getHeaderNames();
        for (String header : headerNames) {
            prefixId(b, id).append(RESPONSE_PREFIX).append(header).append(": ").
                    append(response.getHeader(header)).append(DELIMITER);
        }
        prefixId(b, id).append(RESPONSE_PREFIX).append(DELIMITER);
    }

    private void printResponseLine(final StringBuilder b, final String note, final long id, final int status, long startTime) {
        prefixId(b, id).append(NOTIFICATION_PREFIX).append(note);
        b.append(LogUtil.getCurrentHeapSize())
                .append(LogUtil.getMaxHeapSize())
                .append(LogUtil.getFreeHeapSize())
                .append("\t Time taken millisec: " + (System.currentTimeMillis() - startTime))
                .append(DELIMITER);
        prefixId(b, id).append(RESPONSE_PREFIX).append(Integer.toString(status)).append(DELIMITER);
    }

    private StringBuilder prefixId(final StringBuilder b, final long id) {
        b.append(Long.toString(id)).append(" ");
        return b;
    }

    private void printRequestHeaders(StringBuilder b, Enumeration<String> headerNames, HttpServletRequest request, long id) {

        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            prefixId(b, id).append(REQUEST_PREFIX).append(header).append(": ").
                    append(request.getHeader(header)).append(DELIMITER);
        }
        prefixId(b, id).append(REQUEST_PREFIX).append(DELIMITER);
    }

    private void setTransactionIDForLog(HttpServletRequest request) {
        MDC.put("transactionId", request.getHeader("transactionId"));
    }


}
