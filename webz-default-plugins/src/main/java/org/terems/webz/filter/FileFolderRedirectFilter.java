package org.terems.webz.filter;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.DefaultWebzFileRequestResolver;
import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFileMetadata;
import org.terems.webz.WebzFileRequestResolver;
import org.terems.webz.plugin.BaseWebzFilter;

public class FileFolderRedirectFilter extends BaseWebzFilter {

	private static final WebzFileRequestResolver DEFAULT_FILE_RESOLVER = new DefaultWebzFileRequestResolver();

	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext) throws IOException, WebzException {

		String requestMethod = req.getMethod();
		boolean isMethodHead = "HEAD".equals(requestMethod);

		if (("GET".equals(requestMethod) || isMethodHead)) {

			// resolving file using default resolver...
			WebzFileMetadata metadata = DEFAULT_FILE_RESOLVER.resolve(chainContext.fileFactory(), req).getMetadata();
			if (metadata != null) {

				boolean uriEndsWithSlash = req.getRequestURI().endsWith("/");

				if (!uriEndsWithSlash && metadata.isFolder()) {

					doRedirect(req, resp, true, isMethodHead);
					return;

				} else if (uriEndsWithSlash && metadata.isFile()) {

					doRedirect(req, resp, false, isMethodHead);
					return;

				}
			}
		}
		chainContext.nextPlease(req, resp);
	}

	private void doRedirect(HttpServletRequest req, HttpServletResponse resp, boolean toFolder, boolean isMethodHead) throws IOException {

		StringBuffer urlBuffer = req.getRequestURL();
		if (toFolder) {
			urlBuffer.append('/');
		} else {
			urlBuffer.setLength(urlBuffer.length() - 1);
		}

		String queryString = req.getQueryString();
		if (queryString != null) {
			urlBuffer.append('?').append(queryString);
		}

		String redirectUrl = urlBuffer.toString();

		// TODO dilemma: 301 (permanent) redirect is more SEO friendly but some browsers and crawlers may treat it as "eternal"
		resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
		resp.setHeader("Location", redirectUrl);

		if (!isMethodHead) {
			resp.getWriter().write("Redirect to " + redirectUrl);
		}
	}

}
