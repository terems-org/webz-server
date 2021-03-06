/*
 * WebZ Server can serve web pages from various local and remote file sources.
 * Copyright (C) 2014-2015  Oleksandr Tereschenko <http://www.terems.org/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.terems.webz.filter;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzContext;
import org.terems.webz.WebzDefaults;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzMetadata;
import org.terems.webz.base.BaseWebzFilter;
import org.terems.webz.base.WebzContextProxy;
import org.terems.webz.config.GeneralAppConfig;
import org.terems.webz.util.WebzUtils;

public class WelcomeFilter extends BaseWebzFilter {

	private Collection<String> welcomeFilenameSuffixesLowerCased;
	private Collection<String> welcomeFilenamesLowerCased;

	// dilemma: 301 (permanent) redirects are more SEO friendly however browsers usually treat them as "eternal"
	private boolean welcomeRedirectsPermanent;

	@Override
	public void init(WebzContext context) throws IOException, WebzException {
		GeneralAppConfig generalConfig = getAppConfig().getConfigObject(GeneralAppConfig.class);
		welcomeFilenameSuffixesLowerCased = generalConfig.getWelcomeFilenameSuffixesLowerCased();
		welcomeFilenamesLowerCased = generalConfig.getWelcomeFilenamesLowerCased();
		welcomeRedirectsPermanent = generalConfig.isWelcomeRedirectsPermanent();
	}

	@Override
	public void serve(HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext) throws IOException, WebzException {

		boolean methodHead = WebzUtils.isHttpMethodHead(req);
		if (methodHead || WebzUtils.isHttpMethodGet(req)) {

			WebzFile file = chainContext.resolveFile(req);
			WebzMetadata metadata = file.getMetadata();
			if (metadata != null) {

				String linkedPathname = metadata.getLinkedPathname();
				if (linkedPathname != null) {

					redirectToLinkedUri(req, resp, chainContext.resolveUri(file, req), methodHead);
					return;

				} else {
					boolean uriEndsWithSlash = req.getRequestURI().endsWith("/");

					if (!uriEndsWithSlash && metadata.isFolder()) {

						redirectToFileOrFolder(req, resp, true, methodHead);
						return;

					} else if (uriEndsWithSlash && metadata.isFile()) {

						redirectToFileOrFolder(req, resp, false, methodHead);
						return;

					}
				}
			}
		}
		chainContext.nextPlease(req, resp, new WelcomeContextProxy(chainContext));
	}

	private void redirectToFileOrFolder(HttpServletRequest req, HttpServletResponse resp, boolean toFolder, boolean methodHead)
			throws IOException {

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
		WebzUtils.doRedirect(resp, urlBuffer.toString(), welcomeRedirectsPermanent, methodHead);
	}

	private void redirectToLinkedUri(HttpServletRequest req, HttpServletResponse resp, String linkedUri, boolean methodHead)
			throws IOException {

		String queryString = req.getQueryString();
		if (queryString != null) {
			linkedUri += '?' + queryString;
		}
		WebzUtils.doRedirect(resp, linkedUri, welcomeRedirectsPermanent, methodHead);
	}

	private class WelcomeContextProxy extends WebzContextProxy {

		private WebzContext context;

		private WelcomeContextProxy(WebzContext context) {
			this.context = context;
		}

		@Override
		public WebzFile resolveFile(HttpServletRequest req) throws IOException, WebzException {

			// TODO store resolved WebzFile as a request attribute
			WebzFile file = super.resolveFile(req);

			WebzMetadata metadata = file.getMetadata();
			if (metadata != null && !file.isPathnameInvalid() && !metadata.isFile()) {

				String parentFolderNameLowerCased = null;

				Map<String, WebzFile> childrenMap = buildChildrenMap(file);

				for (String welcomeSuffixLowerCased : welcomeFilenameSuffixesLowerCased) {

					for (String welcomeFilenameLowerCased : welcomeFilenamesLowerCased) {

						if (WebzDefaults.USE_PARENT_FOLDER_NAME.equals(welcomeFilenameLowerCased)) {
							if (parentFolderNameLowerCased == null) {
								parentFolderNameLowerCased = WebzUtils.toLowerCaseEng(metadata.getName());
							}
							welcomeFilenameLowerCased = parentFolderNameLowerCased;
						}
						WebzFile child = childrenMap.get(welcomeFilenameLowerCased + welcomeSuffixLowerCased);

						if (child != null) {
							return child;
						}
					}
				}
			}
			return file;
		}

		private Map<String, WebzFile> buildChildrenMap(WebzFile file) throws IOException, WebzException {
			Map<String, WebzFile> childrenMap = new HashMap<String, WebzFile>();

			for (WebzFile child : file.listChildren(false)) {
				String childPathname = child.getPathname();

				if (childPathname != null) {
					childrenMap.put(WebzUtils.toLowerCaseEng(childPathname.substring(childPathname.lastIndexOf('/') + 1)), child);
				}
			}
			return childrenMap;
		}

		@Override
		protected WebzContext getInternalContext() {
			return context;
		}
	}

}
