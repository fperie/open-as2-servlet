package org.openas2.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AbstractOpenAs2Servlet extends HttpServlet
{
	/** Version of serialization. */
	private static final long serialVersionUID = 1L;

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
