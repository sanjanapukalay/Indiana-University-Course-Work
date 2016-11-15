/**
 * 
 */
package com;

import java.io.File;
import java.io.IOException;

/**
 * @author spukalay
 *
 */
public class CommonUtil {
	public static void cleanDirectory(String... outputFilePaths) throws IOException {
		for (String path : outputFilePaths) {
			File outputFile = new File(path);
			outputFile.getParentFile().mkdirs();
			if (outputFile.exists()) {
				outputFile.delete();
			}
			outputFile.createNewFile();
		}
	}
}
