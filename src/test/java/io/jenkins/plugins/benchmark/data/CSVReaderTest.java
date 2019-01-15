package io.jenkins.plugins.benchmark.data;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.Test;

public class CSVReaderTest {

	@Test
	public void isDoubleTest() throws Exception {
		Method m = Reader.class.getDeclaredMethod("isDouble", String.class);
		m.setAccessible(true);
		CSVReader csvR = new CSVReader();
		String in = null;
		assertEquals(Boolean.FALSE,m.invoke(csvR, in));
		assertEquals(Boolean.FALSE,m.invoke(csvR, ""));
		assertEquals(Boolean.FALSE,m.invoke(csvR, "3.555.999"));
	}

}
