package io.jenkins.plugins.benchmark.data;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.Test;

public class BenchmarkResultTest {
	
	@Test
	public void getFormatsTest() throws Exception {
		
		String[] endingsOld = BenchmarkResults.endings;
		
		String[] newArray = {"abc"};
		BenchmarkResults.endings = newArray;
		assertEquals("abc", BenchmarkResults.getFormats());
		
		String[] newArray1 = {"abc","def"};
		BenchmarkResults.endings = newArray1;
		assertEquals("abc and def", BenchmarkResults.getFormats());
		
		String[] newArray2 = {"abc","def","ghi"};
		BenchmarkResults.endings = newArray2;
		assertEquals("abc, def and ghi", BenchmarkResults.getFormats());
		
		String[] newArray3 = {"abc","def","ghi", "jkl"};
		BenchmarkResults.endings = newArray3;
		assertEquals("abc, def, ghi and jkl", BenchmarkResults.getFormats());
		
		BenchmarkResults.endings = endingsOld;
	}

}
