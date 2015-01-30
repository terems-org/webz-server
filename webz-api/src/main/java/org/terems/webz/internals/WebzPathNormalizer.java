package org.terems.webz.internals;

import org.terems.webz.WebzPropertiesInitable;

/** TODO !!! describe !!! **/
public interface WebzPathNormalizer extends WebzPropertiesInitable {

	/** TODO !!! describe !!! **/
	public String normalizePathname(String nonNormalizedPathname, boolean trimLeadingSlash);

	/** TODO !!! describe !!! **/
	public boolean isNormalizedPathnameInvalid(String pathname);

	/** TODO !!! describe !!! **/
	public String getParentPathname(String pathname);

	/** TODO !!! describe !!! **/
	public String concatPathname(String basePath, String relativePathname);

	/** TODO !!! describe !!! **/
	public boolean belongsToSubtree(String pathname, String subtreePath);

	/** TODO !!! describe !!! **/
	public boolean isHidden(String pathname);

}
