package org.xson.tangyuan.client.http;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;

public class DefaultCustomSSLSocketFactory implements CustomSSLSocketFactory {

	@Override
	public SSLConnectionSocketFactory create() throws Throwable {
		SSLContext                 sslcontext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
													@Override
													public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
														return true;
													}
												}).build();
		//		// Allow TLSv1 protocol only
		//		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null, new HostnameVerifier() {
		//			@Override
		//			public boolean verify(String hostname, SSLSession session) {
		//				return true;
		//			}
		//		});

		// support "TLSv1", "TLSv1.1", "TLSv1.2"
		SSLConnectionSocketFactory sslsf      = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1", "TLSv1.1", "TLSv1.2" }, null, new HostnameVerifier() {
													@Override
													public boolean verify(String hostname, SSLSession session) {
														return true;
													}
												});

		//		"TLSv1", "TLSv1.1", "TLSv1.2"

		// X509HostnameVerifier

		return sslsf;
	}

}
