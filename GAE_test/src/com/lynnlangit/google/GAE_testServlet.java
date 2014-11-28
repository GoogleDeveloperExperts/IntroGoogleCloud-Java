package com.lynnlangit.google;

import java.io.IOException;
import javax.servlet.http.*;

@SuppressWarnings("serial")
public class GAE_testServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		resp.getWriter().println("Hello, world");
	}
}
