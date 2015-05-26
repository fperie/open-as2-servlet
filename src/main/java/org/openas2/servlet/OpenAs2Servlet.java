package org.openas2.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openas2.OpenAS2Exception;
import org.openas2.Session;
import org.openas2.XMLSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class OpenAs2Servlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	/** Logger for the class. */
	private static final Logger LOGGER = LoggerFactory.getLogger(OpenAs2Servlet.class);

	private static final ThreadLocal<Session> THREAD_LOCAL = new ThreadLocal<Session>();

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
		InputStream isConfigFile;
		if (StringUtils.startsWithIgnoreCase(configFile, "classpath:"))
		{
			isConfigFile = OpenAs2Servlet.class.getResourceAsStream(StringUtils.substring(configFile, 10));
		}
		else
		{
			isConfigFile = FileUtils.openInputStream(new File(configFile));
		}

		Session session = THREAD_LOCAL.get();

		if (session == null)
		{
			try
			{
				session = new XMLSession(isConfigFile, baseDirectory);
				THREAD_LOCAL.set(session);
			}
			catch (OpenAS2Exception | ParserConfigurationException | SAXException e)
			{
				LOGGER.error("Impossible to parse the open-as2-core config file (" + configFile + ").", e);
				throw new ServletException("Impossible to parse the open-as2-core config file", e);
			}
		}


		// TODO : c'est la fete
		// TODO : c'est la fete
		// TODO : c'est la fete
		// TODO : c'est la fete

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/html");
		LOGGER.info("c'est la fete");
		response.getWriter().println("<html><body><h1>C'est la fete du slip...</h1></body></html>");
		response.getWriter().close();
	}


	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		forbiddenAction(response);
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException
	{
		forbiddenAction(response);
	}

	@Override
	protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		forbiddenAction(response);
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		forbiddenAction(response);
	}

	private void forbiddenAction(HttpServletResponse response) throws IOException
	{
		response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		response.setContentType("text/html");
		response.getWriter().println("<html><body><p>Use POST only!</p></body></html>");
		response.getWriter().close();
	}
}
