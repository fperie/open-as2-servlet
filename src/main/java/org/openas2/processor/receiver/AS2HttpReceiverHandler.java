package org.openas2.processor.receiver;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Enumeration;

import javax.annotation.Nonnull;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openas2.message.AS2Message;
import org.openas2.message.MessageMDN;
import org.openas2.message.NetAttribute;
import org.openas2.util.HTTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AS2HttpReceiverHandler extends AS2ReceiverHandler
{
	/** Logger for the class. */
	private static final Logger LOGGER = LoggerFactory.getLogger(AS2HttpReceiverHandler.class);

	private HttpServletRequest request;

	private HttpServletResponse response;

	public AS2HttpReceiverHandler(AS2HttpReceiverModule module)
	{
		super(module);
	}

	/**
	 * Retourne la propriété request.
	 * 
	 * @return La propriété request.
	 */
	public HttpServletRequest getRequest()
	{
		return request;
	}

	/**
	 * Définit la propriété request.
	 * 
	 * @param request
	 *        La nouvelle valeur de la propriété request.
	 */
	public void setRequest(HttpServletRequest request)
	{
		this.request = request;
	}

	/**
	 * Retourne la propriété response.
	 * 
	 * @return La propriété response.
	 */
	public HttpServletResponse getResponse()
	{
		return response;
	}

	/**
	 * Définit la propriété response.
	 * 
	 * @param response
	 *        La nouvelle valeur de la propriété response.
	 */
	public void setResponse(HttpServletResponse response)
	{
		this.response = response;
	}

	@Override
	public void handle(InetAddress remoteIp, int remotePort, InetAddress localIp, int localPort,
			InputStream inputStream, OutputStream outputStream)
	{
		super.handle(remoteIp, remotePort, localIp, localPort, inputStream, outputStream);

		this.request = null;
		this.response = null;
	}

	@Override
	protected byte[] readMessage(InputStream inputStream, OutputStream outputStream, @Nonnull AS2Message msg)
			throws IOException, MessagingException
	{
		if (request == null)
		{
			throw new IOException("HTTP request is not specified...");
		}

		InetAddress localIp = InetAddress.getByName(request.getLocalAddr());
		int localPort = request.getLocalPort();
		InetAddress remoteIp = InetAddress.getByName(request.getRemoteAddr());
		int remotePort = request.getRemotePort();

		msg.setAttribute(NetAttribute.MA_SOURCE_IP, localIp.toString());
		msg.setAttribute(NetAttribute.MA_SOURCE_PORT, localPort + "");
		msg.setAttribute(NetAttribute.MA_DESTINATION_IP, remoteIp.toString());
		msg.setAttribute(NetAttribute.MA_DESTINATION_PORT, remotePort + "");

		msg.setAttribute(HTTPUtil.MA_HTTP_REQ_TYPE, request.getMethod());
		msg.setAttribute(HTTPUtil.MA_HTTP_REQ_URL, request.getRequestURI());

		InternetHeaders internetHeaders = new InternetHeaders();

		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements())
		{
			String key = headerNames.nextElement();
			String value = request.getHeader(key);

			LOGGER.debug("header http response, attribute {} => {}", key, value);

			internetHeaders.addHeader(key, value);
		}
		msg.setHeaders(internetHeaders);

		return HTTPUtil.populateData(outputStream, msg, inputStream);
	}

	@Override
	protected void populateResponse(OutputStream out, MessageMDN mdn, int size) throws IOException
	{
		if (response == null)
		{
			throw new IOException("HTTP response is not specified...");
		}

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentLength(size);
		response.setCharacterEncoding("UTF-8");
	}

	@Override
	protected void populateResponseHeaders(final MessageMDN mdn, BufferedOutputStream out) throws IOException
	{
		if (response == null)
		{
			throw new IOException("HTTP response is not specified...");
		}

		Enumeration<Header> headers = mdn.getHeaders().getAllHeaders();
		while (headers.hasMoreElements())
		{
			Header header = headers.nextElement();
			response.setHeader(header.getName(), header.getValue());
		}
	}
}
