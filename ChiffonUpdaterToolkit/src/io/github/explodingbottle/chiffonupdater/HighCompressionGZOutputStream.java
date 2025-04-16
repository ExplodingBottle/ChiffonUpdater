/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

public class HighCompressionGZOutputStream extends GZIPOutputStream {

	public HighCompressionGZOutputStream(OutputStream arg0) throws IOException {
		super(arg0);
		def.setLevel(Deflater.BEST_COMPRESSION);
		def.reset();
	}

}
