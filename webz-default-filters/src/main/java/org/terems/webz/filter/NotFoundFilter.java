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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzContext;
import org.terems.webz.WebzException;
import org.terems.webz.base.BaseWebzFilter;
import org.terems.webz.config.StatusCodesConfig;
import org.terems.webz.filter.helpers.StaticContentSender;

public class NotFoundFilter extends BaseWebzFilter {

	private String pathTo404file;
	private StaticContentSender contentSender;

	@Override
	public void init(WebzContext context) throws IOException, WebzException {
		pathTo404file = getAppConfig().getConfigObject(StatusCodesConfig.class).getPathTo404file();
		contentSender = new StaticContentSender(getAppConfig());
	}

	@Override
	public void serve(HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext) throws IOException, WebzException {

		resp.setStatus(HttpServletResponse.SC_NOT_FOUND);

		if (pathTo404file != null) {
			contentSender.serveStaticContent(req, resp, chainContext.getFile(pathTo404file));
		}
	}

}
