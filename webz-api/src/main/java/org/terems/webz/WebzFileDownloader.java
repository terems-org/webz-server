package org.terems.webz;

import java.io.IOException;
import java.io.OutputStream;

/** TODO !!! describe !!! **/
public abstract class WebzFileDownloader {

	/** TODO !!! describe !!! **/
	public WebzFileMetadata.FileSpecific fileSpecific;

	/** TODO !!! describe !!! **/
	// TODO consider exposing input stream instead...
	public abstract void fileContentToOutputStream(OutputStream out) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzFileDownloader(WebzFileMetadata.FileSpecific fileSpecific) {
		this.fileSpecific = fileSpecific;
	}

}
