package org.xson.tangyuan.client.http;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;

public interface CustomSSLSocketFactory {

	SSLConnectionSocketFactory create() throws Throwable;

}
