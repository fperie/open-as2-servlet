package org.openas2.processor.receiver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openas2.OpenAS2Exception;
import org.openas2.params.InvalidParameterException;

public class AS2HttpReceiverModule extends AS2ReceiverModule
{
	private HttpServletRequest request;

	private HttpServletResponse response;

	@Override
	public NetModuleHandler getHandler()
	{
		AS2HttpReceiverHandler res = new AS2HttpReceiverHandler(this);
		res.setRequest(request);
		res.setResponse(response);
		return res;
	}

	/**
	 * Return request property.
	 * 
	 * @return request property.
	 */
	public HttpServletRequest getRequest()
	{
		return request;
	}

	/**
	 * Set the request property.
	 * 
	 * @param request
	 *        new value of request property.
	 */
	public void setRequest(HttpServletRequest request)
	{
		this.request = request;
	}

	/**
	 * Return the response property.
	 * 
	 * @return response property.
	 */
	public HttpServletResponse getResponse()
	{
		return response;
	}

	/**
	 * Set the new value of response property.
	 * 
	 * @param response
	 *        new value of response property.
	 */
	public void setResponse(HttpServletResponse response)
	{
		this.response = response;
	}

	@Override
	public void doStart() throws OpenAS2Exception
	{
		throw new IllegalArgumentException("Forbidden to start a thread!");
	}

	@Override
	public void doStop() throws OpenAS2Exception
	{
		throw new IllegalArgumentException("Forbidden to stop a thread!");
	}

	@Override
	protected void afterInit() throws InvalidParameterException
	{
		// do nothing ...
	}
}
