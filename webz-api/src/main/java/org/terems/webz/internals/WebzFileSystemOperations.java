package org.terems.webz.internals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzMetadata;

/** TODO !!! describe !!! **/
public interface WebzFileSystemOperations extends WebzPathNormalizerSettable, WebzFileSystemStructureSettable {

	/** TODO !!! describe !!! **/
	// TODO should WebzReadException and WebzWriteException be mentioned explicitly in copyContentToOutputStream's throws declaration ?
	public WebzMetadata.FileSpecific copyContentToOutputStream(String pathname, OutputStream out) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzFileDownloader getFileDownloader(String pathname) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata createFolder(String pathname) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata.FileSpecific uploadFile(String pathname, InputStream content, long numBytes) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata.FileSpecific uploadFile(String pathname, InputStream content) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata move(String srcPathname, String destPathname) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata copy(String srcPathname, String destPathname) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public void delete(String pathname) throws IOException, WebzException;

}