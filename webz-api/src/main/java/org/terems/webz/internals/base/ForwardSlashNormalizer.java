package org.terems.webz.internals.base;

import java.util.regex.Pattern;

import org.terems.webz.base.BaseWebzPropertiesInitable;
import org.terems.webz.internals.WebzPathNormalizer;

/** TODO !!! describe !!! **/
public class ForwardSlashNormalizer extends BaseWebzPropertiesInitable implements WebzPathNormalizer {

	protected static final char FWD_SLASH = '/';
	protected static final String FWD_SLASH_STR = "" + FWD_SLASH;

	/** TODO !!! describe !!! **/
	@Override
	public String normalizePathname(String nonNormalizedPathname, boolean trimLeadingSlash) {

		if (nonNormalizedPathname == null) {
			return null;
		}

		String pathname = nonNormalizedPathname.replace('\\', FWD_SLASH);

		if (trimLeadingSlash && pathname.startsWith(FWD_SLASH_STR)) {
			pathname = pathname.substring(1);
		}
		if (pathname.endsWith(FWD_SLASH_STR)) {
			pathname = pathname.substring(0, pathname.length() - 1);
		}

		return pathname;
	}

	private final static Pattern MULTIPLE_PATH_SEPARATORS = Pattern.compile(FWD_SLASH_STR + "{2,}");

	/** TODO !!! describe !!! **/
	@Override
	public boolean isNormalizedPathnameInvalid(String pathname) {

		return pathname == null || pathname.startsWith(FWD_SLASH_STR) || pathname.endsWith(FWD_SLASH_STR)
				|| MULTIPLE_PATH_SEPARATORS.matcher(pathname).find();
	}

	/** TODO !!! describe !!! **/
	@Override
	public boolean belongsToSubtree(String pathname, String subtreePath) {

		if (subtreePath.isEmpty()) {
			return true;
		}
		if (pathname.length() < subtreePath.length()) {
			return false;
		}

		if (!pathname.startsWith(subtreePath)) {
			return false;
		}
		return pathname.length() == subtreePath.length() || pathname.codePointAt(subtreePath.length()) == FWD_SLASH;
	}

	/** TODO !!! describe !!! **/
	@Override
	public String getParentPathname(String pathname) {

		if ("".equals(pathname)) {
			return null;
		}

		int separatorIndex = pathname.lastIndexOf(FWD_SLASH);

		if (separatorIndex < 0) {
			return "";
		}
		return pathname.substring(0, separatorIndex);
	}

	/** TODO !!! describe !!! **/
	@Override
	public String concatPathname(String basePath, String relativePathname) {

		return basePath + FWD_SLASH + relativePathname;
	}

}
