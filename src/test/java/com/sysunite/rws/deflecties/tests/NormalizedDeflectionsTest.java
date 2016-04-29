package com.sysunite.rws.deflecties.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.sysunite.rws.deflecties.F25File;
import com.sysunite.rws.deflecties.NormalizedDeflections;

public class NormalizedDeflectionsTest {
	
	private static final String TEST_FILE_NAME = "marked-up.f25";
	
	@Test
	public void testDeflections() {
		try {
			F25File f = new F25File(getTestFile());
			List<NormalizedDeflections> ndList = NormalizedDeflections.getDeflections(f);
			assertNull(NormalizedDeflections.error);
			NormalizedDeflections nd = ndList.get(0);
			double eps = 0.1;
			
			assertEquals(nd.D0bs[0], 106.2, eps);
			assertEquals(nd.D0bs[1],  84.9, eps);
			assertEquals(nd.D0bs[2],  74.9, eps);
			assertEquals(nd.D0bs[3],  64.8, eps);
			assertEquals(nd.D0bs[4],  56.0, eps);
			assertEquals(nd.D0bs[5],  48.2, eps);
			assertEquals(nd.D0bs[6],  41.5, eps);
			
			assertEquals(nd.D0bt, 111.5, eps);
			assertEquals(nd.IDK300bt, 24.9, eps);
		}
		catch (Exception e) {
			fail("Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private File getTestFile() {
		String url = getClass().getClassLoader().getResource(TEST_FILE_NAME).getFile();
		File file = new File(url);
		return file;
	}
	
}
