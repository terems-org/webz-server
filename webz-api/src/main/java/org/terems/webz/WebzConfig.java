package org.terems.webz;

import org.terems.webz.plugin.WebzConfigObject;

/** TODO !!! describe !!! **/
public interface WebzConfig {

	/** TODO !!! describe !!! **/
	public <T extends WebzConfigObject> T getConfigObject(Class<T> configObjectClass) throws WebzException;

}
