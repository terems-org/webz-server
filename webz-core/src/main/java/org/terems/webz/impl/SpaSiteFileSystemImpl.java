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

package org.terems.webz.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzFilter;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzMetadata.FileSpecific;
import org.terems.webz.base.WebzMetadataProxy;
import org.terems.webz.internals.ParentChildrenMetadata;
import org.terems.webz.internals.WebzFileSystem;
import org.terems.webz.internals.WebzFileSystemStructure;
import org.terems.webz.internals.WebzPathNormalizer;
import org.terems.webz.internals.base.BaseWebzFileSystemImpl;

public class SpaSiteFileSystemImpl extends BaseWebzFileSystemImpl {

	private static final Logger LOG = LoggerFactory.getLogger(SpaSiteFileSystemImpl.class);

	private WebzFileSystem spaFileSystem;
	private WebzFileSystem siteFileSystem;

	private String uniqueId;

	public SpaSiteFileSystemImpl(WebzFileSystem spaFileSystem, WebzFileSystem siteFileSystem) {
		this.spaFileSystem = spaFileSystem;
		this.siteFileSystem = siteFileSystem;
	}

	@Override
	protected void init() {

		uniqueId = siteFileSystem.getUniqueId() + "-" + spaFileSystem.getUniqueId();

		if (LOG.isInfoEnabled()) {
			LOG.info("'" + uniqueId + "' hybrid file system was created");
		}
	}

	@Override
	public String getUniqueId() {
		return uniqueId;
	}

	private static class FileFound {

		WebzMetadata proxiedMetadata;
		WebzFileSystem primaryHost;
		WebzFileSystem secondaryHost; // can be null
		String primaryOrigin;
		String secondaryOrigin; // can be null
		String actualPathname;

		FileFound(WebzMetadata metadataToProxy, String[] originNames, String linkedPathname, WebzFileSystem primaryHost,
				WebzFileSystem secondaryHost, String primaryOrigin, String secondaryOrigin, String actualPathname) {

			this.proxiedMetadata = new ProxiedMetadata(metadataToProxy, originNames, linkedPathname);
			this.primaryHost = primaryHost;
			this.secondaryHost = secondaryHost;
			this.primaryOrigin = primaryOrigin;
			this.secondaryOrigin = secondaryOrigin;
			this.actualPathname = actualPathname;
		}

	}

	private static class ProxiedMetadata extends WebzMetadataProxy {

		WebzMetadata metadataToProxy;
		String[] originNames;
		String linkedPathname;

		ProxiedMetadata(WebzMetadata metadataToProxy, String[] originNames, String linkedPathname) {
			this.metadataToProxy = metadataToProxy;
			this.originNames = originNames;
			this.linkedPathname = linkedPathname;
		}

		@Override
		protected WebzMetadata getInnerMetadata() {
			return metadataToProxy;
		}

		@Override
		public String[] getOriginNames() {
			return originNames;
		}

		@Override
		public String getLinkedPathname() {
			return linkedPathname;
		}

	}

	private FileFound findFile(String pathname) throws IOException, WebzException {

		FileFound found = hitAgainstFullPathname(pathname);
		if (found != null) {
			return found;
		}
		// TODO get rid of hitAgainstFullPathname() method when caching is implemented (integrate it's logic into the algorithm below)

		WebzPathNormalizer pathNormalizer = getPathNormalizer();
		WebzFileSystemStructure siteStructure = siteFileSystem.getStructure();

		String currentPath = "";
		String[] pathMembers = pathNormalizer.splitPathname(pathname);
		for (int i = 0; i < pathMembers.length; i++) {

			boolean matchFound = false;
			Map<String, WebzMetadata> children = siteStructure.getChildPathnamesAndMetadata(currentPath);
			if (children != null) {

				String childPathname = pathNormalizer.concatPathname(currentPath, pathMembers[i]);

				WebzMetadata childMetadata = children.get(childPathname);
				if (childMetadata != null && childMetadata.isFolder()) {

					matchFound = true;
					currentPath = childPathname;
				}
			}
			if (!matchFound) {

				if (i > 0) {

					found = checkLinkedPathnameInSpa(pathNormalizer.constructPathname(pathMembers, i, pathMembers.length));
					if (found != null) {
						return found;
					}
				}
				return null;
			}
		}
		return null;
	}

	private static final String[] ORIGIN_SPA = { WebzFilter.FILE_ORIGIN_SPA };
	private static final String[] ORIGIN_SITE = { WebzFilter.FILE_ORIGIN_SITE };
	private static final String[] ORIGIN_SPA_AND_SITE = { WebzFilter.FILE_ORIGIN_SPA, WebzFilter.FILE_ORIGIN_SITE };
	private static final String[] ORIGIN_LINKED = { WebzFilter.FILE_ORIGIN_LINKED };

	private FileFound checkLinkedPathnameInSpa(final String linkedPathname) throws IOException, WebzException {

		final WebzMetadata metadataToProxy = spaFileSystem.getStructure().getMetadata(linkedPathname);
		if (metadataToProxy != null) {

			return new FileFound(metadataToProxy, ORIGIN_LINKED, linkedPathname, spaFileSystem, null, WebzFilter.FILE_ORIGIN_LINKED, null,
					linkedPathname);
		}
		return null;
	}

	private FileFound hitAgainstFullPathname(String pathname) throws IOException, WebzException {

		WebzFileSystemStructure spaStructure = spaFileSystem.getStructure();
		WebzFileSystemStructure siteStructure = siteFileSystem.getStructure();

		WebzMetadata metadata = siteStructure.getMetadata(pathname);
		if (metadata != null) {

			if (metadata.isFolder()) {

				WebzMetadata secondaryMetadata = spaStructure.getMetadata(pathname);
				if (secondaryMetadata != null && secondaryMetadata.isFolder()) {
					return new FileFound(metadata, ORIGIN_SPA_AND_SITE, null, siteFileSystem, spaFileSystem, WebzFilter.FILE_ORIGIN_SITE,
							WebzFilter.FILE_ORIGIN_SPA, pathname);
				}
			}
			return new FileFound(metadata, ORIGIN_SITE, null, siteFileSystem, null, WebzFilter.FILE_ORIGIN_SITE, null, pathname);
		}

		metadata = spaStructure.getMetadata(pathname);
		if (metadata != null) {

			return new FileFound(metadata, ORIGIN_SPA, null, spaFileSystem, null, WebzFilter.FILE_ORIGIN_SPA, null, pathname);
		}

		return null;
	}

	@Override
	public WebzMetadata getMetadata(String pathname) throws IOException, WebzException {

		FileFound found = findFile(pathname);
		if (found == null) {
			return null;
		}
		return found.proxiedMetadata;
	}

	@Override
	public ParentChildrenMetadata getParentChildrenMetadata(String parentPathname) throws IOException, WebzException {

		FileFound found = findFile(parentPathname);
		if (found == null) {
			return null;
		}
		return new ParentChildrenMetadata(found.proxiedMetadata, fetchMergedChildren(found), null);
	}

	@Override
	public Map<String, WebzMetadata> getChildPathnamesAndMetadata(String parentPathname) throws IOException, WebzException {

		FileFound found = findFile(parentPathname);
		if (found == null) {
			return null;
		}
		return fetchMergedChildren(found);
	}

	private Map<String, WebzMetadata> fetchMergedChildren(FileFound found) throws IOException, WebzException {

		Map<String, WebzMetadata> primary = found.primaryHost.getStructure().getChildPathnamesAndMetadata(found.actualPathname);
		Map<String, WebzMetadata> secondary = null;
		if (found.secondaryHost != null) {
			secondary = found.secondaryHost.getStructure().getChildPathnamesAndMetadata(found.actualPathname);
		}

		String[] originsPrimary = new String[] { found.primaryOrigin };
		String[] originsSecondary = new String[] { found.secondaryOrigin };
		String[] originsBoth = new String[] { found.secondaryOrigin, found.primaryOrigin };

		if (secondary == null) {
			return wrapMetadataMap(primary, originsPrimary);
		}
		if (primary == null) {
			return wrapMetadataMap(secondary, originsSecondary);
		}
		Map<String, WebzMetadata> merged = new LinkedHashMap<String, WebzMetadata>(primary);
		for (Map.Entry<String, WebzMetadata> secondaryEntry : secondary.entrySet()) {

			WebzMetadata alsoInPrimary = merged.get(secondaryEntry.getKey());
			if (alsoInPrimary == null) {

				merged.put(secondaryEntry.getKey(), new ProxiedMetadata(secondaryEntry.getValue(), originsSecondary, null));
			} else {

				if (alsoInPrimary.isFolder() && secondaryEntry.getValue().isFolder()) {
					merged.put(secondaryEntry.getKey(), new ProxiedMetadata(secondaryEntry.getValue(), originsBoth, null));
				} else {
					merged.put(secondaryEntry.getKey(), new ProxiedMetadata(secondaryEntry.getValue(), originsSecondary, null));
				}
			}
		}

		Set<String> newSecondaryKeys = new HashSet<String>(secondary.keySet());
		newSecondaryKeys.removeAll(primary.keySet());

		return merged;
	}

	private Map<String, WebzMetadata> wrapMetadataMap(Map<String, WebzMetadata> metadataMap, String[] origins) {

		if (metadataMap != null) {

			for (Map.Entry<String, WebzMetadata> entry : metadataMap.entrySet()) {
				entry.setValue(new ProxiedMetadata(entry.getValue(), origins, null));
			}
		}
		return metadataMap;
	}

	@Override
	public WebzFileDownloader getFileDownloader(String pathname) throws IOException, WebzException {

		FileFound found = findFile(pathname);
		if (found == null) {
			return null;
		}

		WebzFileDownloader downloader = found.primaryHost.getOperations().getFileDownloader(found.actualPathname);
		if (downloader == null) {
			return null;
		}

		return new WebzFileDownloader(found.proxiedMetadata.getFileSpecific(), downloader.content);
	}

	@Override
	public WebzMetadata createFolder(String pathname) throws IOException, WebzException {
		return siteFileSystem.getOperations().createFolder(pathname);
	}

	@Override
	public FileSpecific uploadFile(String pathname, InputStream content, long numBytes) throws IOException, WebzException {
		return siteFileSystem.getOperations().uploadFile(pathname, content, numBytes);
	}

	@Override
	public FileSpecific uploadFile(String pathname, InputStream content) throws IOException, WebzException {
		return siteFileSystem.getOperations().uploadFile(pathname, content);
	}

	@Override
	public WebzMetadata move(String srcPathname, String destPathname) throws IOException, WebzException {
		return siteFileSystem.getOperations().move(srcPathname, destPathname);
	}

	@Override
	public WebzMetadata copy(String srcPathname, String destPathname) throws IOException, WebzException {
		return siteFileSystem.getOperations().copy(srcPathname, destPathname);
	}

	@Override
	public void delete(String pathname) throws IOException, WebzException {
		siteFileSystem.getOperations().delete(pathname);
	}

}
