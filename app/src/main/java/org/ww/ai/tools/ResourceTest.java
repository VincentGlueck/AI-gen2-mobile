package org.ww.ai.tools;

import java.io.IOException;
import java.io.InputStream;

public class ResourceTest {

	public ResourceTest() {
		InputStream in = getInputStream("generator.xml");
		try {
			System.out.println(in.available());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	private InputStream getInputStream(final String fileName) {
		ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }
	}
	
}
