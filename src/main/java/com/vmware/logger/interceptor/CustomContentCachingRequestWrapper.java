package com.vmware.logger.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;

public class CustomContentCachingRequestWrapper extends ContentCachingRequestWrapper {

	private static final Logger LOG = LoggerFactory.getLogger(CustomContentCachingRequestWrapper.class);

	private ServletInputStream inputStream;

	public CustomContentCachingRequestWrapper(HttpServletRequest request, int contentCacheLimit) {
		super(request, contentCacheLimit);
		try {
			this.inputStream = request.getInputStream();
		} catch (IOException e) {
			LOG.error("Error in CustomContentCachingRequestWrapper  :", e);
		}
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (this.inputStream == null || !(this.inputStream instanceof ContentCachingInputStream)) {
			this.inputStream = new ContentCachingInputStream(getRequest().getInputStream());
		}
		return this.inputStream;
	}

	public void appendStream(byte[] bytes, int offset, int length) {
		if (inputStream instanceof ContentCachingInputStream) {
			((ContentCachingInputStream) inputStream).appendStream(bytes, offset, length);
		}
	}

	private static class ContentCachingInputStream extends ServletInputStream {

		private final ServletInputStream servletInputStream;

		InputStream cacheInputStream;

		public ContentCachingInputStream(ServletInputStream servletInputStream) {
			this.servletInputStream = servletInputStream;
			this.cacheInputStream = servletInputStream;
		}

		public void appendStream(byte[] b, int offset, int length) {
			this.cacheInputStream = new SequenceInputStream(
					new ByteArrayInputStream(b, offset, length), servletInputStream);
		}

		@Override
		public int read() throws IOException {
			return cacheInputStream.read();
		}

		@Override
		public boolean isFinished() {
			return servletInputStream.isFinished();
		}

		@Override
		public boolean isReady() {
			return servletInputStream.isReady();
		}

		@Override
		public void setReadListener(ReadListener listener) {
			servletInputStream.setReadListener(listener);

		}
	}


}
