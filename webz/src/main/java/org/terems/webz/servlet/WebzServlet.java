package org.terems.webz.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzEngine;
import org.terems.webz.WebzException;
import org.terems.webz.impl.WebzEngineMain;

@SuppressWarnings("serial")
public class WebzServlet extends HttpServlet {

	private WebzEngine webzEngine;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		try {
			webzEngine = new WebzEngineMain(config.getInitParameter("dropboxPath"), config.getInitParameter("accessToken"));
		} catch (WebzException e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) {
		// TODO leverage from "last modified" http logic supported in parent implementation ?
		webzEngine.fulfilRequest(req, resp);
	}

}
