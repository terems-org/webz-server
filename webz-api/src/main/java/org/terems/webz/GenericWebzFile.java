package org.terems.webz;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

/** TODO !!! describe !!! **/
public class GenericWebzFile implements WebzFile {

	/** TODO !!! describe !!! **/
	public static String trimFileSeparators(String pathName) {
		pathName = pathName.trim();
		if (pathName.startsWith("/") || pathName.startsWith("\\")) {
			pathName = pathName.substring(1);
		}
		if (pathName.endsWith("/") || pathName.endsWith("\\")) {
			pathName = pathName.substring(0, pathName.length() - 1);
		}
		return pathName;
	}

	private WebzFileSystem fileSystem;
	private String pathName;

	/** TODO !!! describe !!! **/
	public GenericWebzFile(WebzFileSystem fileSystem, String pathName) {
		this.fileSystem = fileSystem;
		this.pathName = trimFileSeparators(pathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public String getPathName() {
		return pathName;
	}

	/** TODO document that this method will NOT fetch metadata **/
	@Override
	public byte[] getFileContent(long expectedNumberOfBytes) throws IOException, WebzException {
		ByteArrayOutputStream out = new ByteArrayOutputStream((int) expectedNumberOfBytes);
		fileContentToOutputStream(out);
		return out.toByteArray();
	}

	/** TODO document that this method will fetch metadata **/
	@Override
	public byte[] getFileContent() throws IOException, WebzException {

		WebzFileMetadata.FileSpecific fileSpecific = getMetadata().getFileSpecific();
		if (fileSpecific == null) {
			throw new WebzException(pathName + " is not a file");
		}

		return getFileContent(fileSpecific.getNumberOfBytes());
	}

	// TODO remove this commented out piece completely ?
	// @Override
	// public boolean exits() throws IOException, WebzException {
	// return getMetadata() != null;
	// }

	/** TODO !!! describe !!! **/
	@Override
	public WebzFileMetadata getMetadata() throws IOException, WebzException {
		return fileSystem.getMetadata(pathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFileMetadata fileContentToOutputStream(OutputStream out) throws IOException, WebzException {
		return fileSystem.fileContentToOutputStream(pathName, out);
	}

	/** TODO !!! describe !!! **/
	@Override
	public Collection<WebzFile> getChildren() throws IOException, WebzException {

		Collection<String> childPathNames = fileSystem.getChildPathNames(pathName);
		if (childPathNames == null) {
			return null;
		}

		Collection<WebzFile> children = new ArrayList<>(childPathNames.size());
		for (String childPathName : childPathNames) {
			children.add(fileSystem.get(childPathName));
		}
		return children;
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFileMetadata createFolder() throws IOException, WebzException {
		return fileSystem.createFolder(pathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFileMetadata uploadFile(byte[] content) throws IOException, WebzException {
		return fileSystem.uploadFile(pathName, content);
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFileMetadata move(WebzFile destFile) throws IOException, WebzException {
		return move(destFile.getPathName());
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFileMetadata copy(WebzFile destFile) throws IOException, WebzException {
		return copy(destFile.getPathName());
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFileMetadata move(String destPathName) throws IOException, WebzException {
		return fileSystem.move(pathName, destPathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFileMetadata copy(String destPathName) throws IOException, WebzException {
		return fileSystem.copy(pathName, destPathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public void delete() throws IOException, WebzException {
		fileSystem.delete(pathName);
	}

}