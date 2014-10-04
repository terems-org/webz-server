package org.terems.webz.internals.cache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.terems.webz.WebzWriteException;

/** TODO !!! describe !!! **/
public class WebzByteArrayInputStream extends ByteArrayInputStream {

	/** TODO !!! describe !!! **/
	public long writeAvailableToOutputStream(OutputStream out) throws WebzWriteException {

		int available = available();

		try {
			out.write(this.buf, this.pos, available);
		} catch (IOException e) {
			throw new WebzWriteException(e);
		}
		return skip(available);
	}

	/**
	 * @see java.io.ByteArrayInputStream#ByteArrayInputStream(byte[])
	 */
	public WebzByteArrayInputStream(byte buf[]) {
		super(buf);
	}

	/**
	 * @see java.io.ByteArrayInputStream#ByteArrayInputStream(byte[], int, int)
	 */
	public WebzByteArrayInputStream(byte buf[], int offset, int length) {
		super(buf, offset, length);
	}

}