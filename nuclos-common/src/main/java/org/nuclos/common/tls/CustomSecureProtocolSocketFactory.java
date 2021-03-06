//Copyright (C) 2010  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.common.tls;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.log4j.Logger;

/**
 * An custom SSL/TLS socket factory (for apache httpcomponents) that accepts self-signed certificates
 * and does not check the associated hostname.
 * 
 * @author Thomas Pasch
 */
public class CustomSecureProtocolSocketFactory extends SSLSocketFactory {

	private static final Logger log = Logger.getLogger(CustomSecureProtocolSocketFactory.class);

	private SSLContext sslcontext = null;

	public CustomSecureProtocolSocketFactory() {
		super(createCustomSSLContext(), ALLOW_ALL_HOSTNAME_VERIFIER);
	}

	private static SSLContext createCustomSSLContext() {
		try {
			SSLContext context = SSLContext.getInstance("SSL");
			context.init(null, new TrustManager[] { new CustomX509TrustManager(null) }, null);
			return context;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new IllegalStateException(e);
		}
	}

	private SSLContext getSSLContext() {
		if (this.sslcontext == null) {
			this.sslcontext = createCustomSSLContext();
		}
		return this.sslcontext;
	}

	/*
	@Override
	public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort) throws IOException, UnknownHostException {
		return getSSLContext().getSocketFactory().createSocket(host, port, clientHost, clientPort);
	}
	 */

	/*
	@Override
	public Socket createSocket(final String host, final int port, final InetAddress localAddress, final int localPort, final HttpConnectionParams params)
			throws IOException, UnknownHostException {
		if (params == null) {
			throw new IllegalArgumentException("Parameters may not be null");
		}
		int timeout = params.getConnectionTimeout();
		SocketFactory socketfactory = getSSLContext().getSocketFactory();
		if (timeout == 0) {
			return socketfactory.createSocket(host, port, localAddress, localPort);
		} else {
			Socket socket = socketfactory.createSocket();
			SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
			SocketAddress remoteaddr = new InetSocketAddress(host, port);
			socket.bind(localaddr);
			socket.connect(remoteaddr, timeout);
			return socket;
		}
	}
	 */

	/*
	@Override
	public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
		return getSSLContext().getSocketFactory().createSocket(host, port);
	}
	 */

	/*
	@Override
	public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
		return getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose);
	}

	@Override
	public boolean equals(Object obj) {
		return ((obj != null) && obj.getClass().equals(CustomSecureProtocolSocketFactory.class));
	}

	@Override
	public int hashCode() {
		return CustomSecureProtocolSocketFactory.class.hashCode();
	}
	 */

}
