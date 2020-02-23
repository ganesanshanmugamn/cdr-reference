package com.vmware.logger.interceptor;

import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;


public class ClientRequestLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ClientRequestLoggingInterceptor.class);


    public static final String NOTIFICATION_PREFIX = "* ";
    public static final String REQUEST_PREFIX = ">";
    public static final String RESPONSE_PREFIX = "< ";
    public static final String CLIENT_RESPONDED_WITH_A_RESPONSE_LIVE_THREAD_COUNT = "Client responded with a response  : Live Client Count ";
    public static final String CLIENT_REQUEST_HAS_BEEN_INITIATED_LIVE_THREAD_COUNT = "Client request has been initiated : Live Client Count ";
    private static final String DELIMITER = "::";
    private static AtomicLong reqCounter = new AtomicLong(0);
    private static AtomicLong liveReqCounter = new AtomicLong(0);
    private boolean enableFullLog = true;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        liveReqCounter.incrementAndGet();
        long startTime = System.currentTimeMillis();
        long id = reqCounter.incrementAndGet();

        logBeforeClientCall(request, body, id);
        ClientHttpResponse response = executeRequest(request, body, execution);
        logAfterClientCall(request, response, id, startTime);

        liveReqCounter.decrementAndGet();
        return response;
    }

    private ClientHttpResponse executeRequest(HttpRequest request,
                                              byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        ClientHttpResponse response;
        try {
            response = execution.execute(request, body);
        } catch (Exception e) {
            liveReqCounter.decrementAndGet();
            throw e;
        }
        return response;
    }


    private void logAfterClientCall(HttpRequest request,
                                    ClientHttpResponse response,
                                    long id, long startTime) throws IOException {
        final StringBuilder b = new StringBuilder();

        InputStream body = null;
        try {
            appendcounter(id, b, CLIENT_RESPONDED_WITH_A_RESPONSE_LIVE_THREAD_COUNT);
            appendMemoryDetails(b);

            prefixId(b, id)
                    .append(RESPONSE_PREFIX)
                    .append(request.getMethod())
                    .append(" ").append(request.getURI())
                    .append(LogUtil.getTimeTaken(startTime))
                    .append(DELIMITER);

            prefixId(b, id).append(RESPONSE_PREFIX)
                    .append("ResponseCode : ")
                    .append(response.getRawStatusCode())
                    .append(DELIMITER);

            if (enableFullLog) {
                appendHeaders(id, b, response.getHeaders(), RESPONSE_PREFIX);
                prefixId(b, id).append(RESPONSE_PREFIX).append(DELIMITER);
                body = response.getBody();
                b.append(new String(ByteStreams.toByteArray(body), Charset.forName("UTF-8")));
                log.debug("" + b + DELIMITER);
            } else {
                log.info("" + b + DELIMITER);
            }
        } catch (IOException e) {
            log.error("Error while ", e);
        } finally {
            if (body != null)
                body.close();
        }

    }


    private void logBeforeClientCall(HttpRequest request, byte[] body, long id) {

        final StringBuilder b = new StringBuilder();
        appendcounter(id, b, CLIENT_REQUEST_HAS_BEEN_INITIATED_LIVE_THREAD_COUNT);
        appendMemoryDetails(b);
        prefixId(b, id)
                .append(REQUEST_PREFIX)
                .append(request.getMethod())
                .append(" ").append(request.getURI())
                .append(DELIMITER);

        if (enableFullLog) {
            appendHeaders(id, b, request.getHeaders(), REQUEST_PREFIX);
            prefixId(b, id)
                    .append(REQUEST_PREFIX)
                    .append(DELIMITER);

            b.append(new String(body, Charset.forName("UTF-8"))).append(DELIMITER);
            log.debug(b.toString());
        } else {
            log.info(b.toString());
        }

    }

    private void appendcounter(long id, StringBuilder b, String clientRespondedWithAResponseLiveThreadCount) {
        prefixId(b, id)
                .append(NOTIFICATION_PREFIX)
                .append(clientRespondedWithAResponseLiveThreadCount + liveReqCounter.get() + " : ");
    }

    private void appendHeaders(long id, StringBuilder b, HttpHeaders headers, String prefix) {
        Set<Entry<String, List<String>>> headerEntrySet = headers.entrySet();
        for (Entry<String, List<String>> entry : headerEntrySet) {
            prefixId(b, id)
                    .append(prefix)
                    .append(entry.getKey()).append(": ")
                    .append(entry.getValue())
                    .append(DELIMITER);
        }
    }

    private StringBuilder appendMemoryDetails(StringBuilder b) {
        return b.append(LogUtil.getCurrentHeapSize())
                .append(LogUtil.getMaxHeapSize())
                .append(LogUtil.getFreeHeapSize() + DELIMITER);
    }

    private StringBuilder prefixId(final StringBuilder b, final long id) {
        b.append("C")
                .append(Long.toString(id))
                .append(" ");
        return b;
    }


}