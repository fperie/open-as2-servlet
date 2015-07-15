package org.openas2.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openas2.ComponentNotFoundException;
import org.openas2.OpenAS2Exception;
import org.openas2.Session;
import org.openas2.XMLSession;
import org.openas2.processor.ProcessorModule;
import org.openas2.processor.receiver.AS2HttpReceiverModule;
import org.openas2.processor.receiver.AS2ReceiverModule;
import org.openas2.processor.receiver.NetModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class OpenAs2Servlet extends AbstractOpenAs2Servlet
{
	private static final long serialVersionUID = 1L;

	/** Logger for the class. */
	private static final Logger LOGGER = LoggerFactory.getLogger(OpenAs2Servlet.class);

	private static final ThreadLocal<Session> SESSION_THREAD_LOCAL = new ThreadLocal<Session>();

	private static final ThreadLocal<NetModule> MODULE_RECEIVER_THREAD_LOCAL = new ThreadLocal<NetModule>();

	private String configFile;

	private String baseDirectory;

	@Override
	public void init(ServletConfig sc) throws ServletException
	{
		this.configFile = sc.getInitParameter("configFile");
		this.baseDirectory = sc.getInitParameter("baseDirectory");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		final Session session = getSession();
		assert session != null;

		NetModule receiverModule = MODULE_RECEIVER_THREAD_LOCAL.get();
		if (receiverModule == null)
		{
			receiverModule = initReceiverModule(session);
		}

		// inject http request in AS2HttpReceiverModule
		if (receiverModule instanceof AS2HttpReceiverModule)
		{
			((AS2HttpReceiverModule)receiverModule).setRequest(request);
			((AS2HttpReceiverModule)receiverModule).setResponse(response);
		}
		else
		{
			throw new ServletException("the as2 receiver module cannot be cast in AS2HttpReceiverModule...");
		}

		assert receiverModule != null;

		processMessage(request, response, receiverModule, session);
		((AS2HttpReceiverModule)receiverModule).setRequest(null);
		((AS2HttpReceiverModule)receiverModule).setResponse(null);
	}

	/**
	 * Get the current session else it initializes a new session.
	 * 
	 * @return current or new as2 session.
	 * @throws IOException
	 *         exception occured during this operation.
	 * @throws ServletException
	 *         exception occured during this operation.
	 */
	protected Session getSession() throws IOException, ServletException
	{
		Session session = SESSION_THREAD_LOCAL.get();

		if (session == null)
		{
			session = initSession();
		}
		return session;
	}

	private void processMessage(final HttpServletRequest request, final HttpServletResponse response,
			@Nonnull final NetModule receiverModule, Session session)
			throws ServletException, IOException
	{
		InetAddress remoteIp = InetAddress.getByName(request.getRemoteAddr());
		int remotePort = request.getRemotePort();

		InetAddress localIp = InetAddress.getByName(request.getLocalAddr());
		int localPort = request.getLocalPort();

		receiverModule.getHandler().handle(remoteIp, remotePort, localIp, localPort, request.getInputStream(),
				response.getOutputStream());
	}

	@Nonnull
	private Session initSession() throws IOException, ServletException
	{
		Session session;
		InputStream isConfigFile;
		if (StringUtils.startsWithIgnoreCase(configFile, "classpath:"))
		{
			isConfigFile = OpenAs2Servlet.class.getResourceAsStream(StringUtils.substring(configFile, 10));
		}
		else
		{
			isConfigFile = FileUtils.openInputStream(new File(configFile));
		}

		try
		{
			session = new XMLSession(isConfigFile, baseDirectory);
			SESSION_THREAD_LOCAL.set(session);
		}
		catch (OpenAS2Exception | ParserConfigurationException | SAXException e)
		{
			LOGGER.error("Impossible to parse the open-as2-core config file (" + configFile + ").", e);
			throw new ServletException("Impossible to parse the open-as2-core config file", e);
		}

		return session;
	}

	@Nullable
	private NetModule initReceiverModule(@Nonnull final Session session) throws ServletException
	{
		NetModule receiverModule = null;

		try
		{
			List<ProcessorModule> modules = session.getProcessor().getModules();
			for (ProcessorModule module : modules)
			{
				if (module.getClass() == AS2ReceiverModule.class || module.getClass() == AS2HttpReceiverModule.class)
				{
					receiverModule = (NetModule)module;
					break;
				}
			}
		}
		catch (ComponentNotFoundException cnfe)
		{
			LOGGER.error("Impossible to get the as2 receiver module from as2 session.", cnfe);
			throw new ServletException("Impossible to get the as2 receiver module from as2 session.");
		}

		MODULE_RECEIVER_THREAD_LOCAL.set(receiverModule);
		return receiverModule;
	}
}
