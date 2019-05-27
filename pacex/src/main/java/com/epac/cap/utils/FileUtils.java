package com.epac.cap.utils;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {

	public static final boolean compareFiles(final Path filea, final Path fileb) throws IOException {
		if (Files.size(filea) != Files.size(fileb)) {
			return false;
		}

		LogUtils.debug("Compare two files: "+filea+" and "+fileb);
		final long size = Files.size(filea);
		final int mapspan = 8 * 1024 * 1024;
		
		try (FileChannel chana = (FileChannel) Files.newByteChannel(filea);
				FileChannel chanb = (FileChannel) Files.newByteChannel(fileb)) {

			for (long position = 0; position < size; position += mapspan) {
				MappedByteBuffer mba = mapChannel(chana, position, size, mapspan);
				MappedByteBuffer mbb = mapChannel(chanb, position, size, mapspan);

				if (mba.compareTo(mbb) != 0) {
					return false;
				}

			}

		}
		return true;
	}

	private static MappedByteBuffer mapChannel(FileChannel channel, long position, long size, int mapspan)
			throws IOException {
		final long end = Math.min(size, position + mapspan);
		final long maplen = (int) (end - position);
		return channel.map(MapMode.READ_ONLY, position, maplen);
	}
}