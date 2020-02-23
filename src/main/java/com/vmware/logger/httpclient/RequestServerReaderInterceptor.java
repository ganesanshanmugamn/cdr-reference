package com.vmware.logger.httpclient;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import java.io.*;
import java.util.stream.Collectors;

@Provider
public class RequestServerReaderInterceptor implements ReaderInterceptor {

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context)
            throws IOException, WebApplicationException {
        InputStream is = context.getInputStream();
        String body = new BufferedReader(new InputStreamReader(is)).lines()
                .collect(Collectors.joining("\n"));

        context.setInputStream(new ByteArrayInputStream(
                (body + " message added in server reader interceptor").getBytes()));

        return context.proceed();
    }
}