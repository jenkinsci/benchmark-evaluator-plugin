package io.jenkins.plugins.benchmark.data;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.Test;

public class BenchmarkResultTest {
	
	@Test
	public void getFormatsTest() throws Exception {
		
		String[] endingsOld = BenchmarkResults.endings;
		
		Field f = BenchmarkResults.class.getField("endings");
		
		Field modifiersField = Field.class.getDeclaredField("modifiers");
	    modifiersField.setAccessible(true);
	    modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
		
		f.setAccessible(true);
		String[] newArray = {"abc"};
		f.set(null, newArray);
		assertEquals("abc", BenchmarkResults.getFormats());
		
		String[] newArray1 = {"abc","def"};
		f.set(null, newArray1);
		assertEquals("abc and def", BenchmarkResults.getFormats());
		
		String[] newArray2 = {"abc","def","ghi"};
		f.set(null, newArray2);
		assertEquals("abc, def and ghi", BenchmarkResults.getFormats());
		
		String[] newArray3 = {"abc","def","ghi", "jkl"};
		f.set(null, newArray3);
		assertEquals("abc, def, ghi and jkl", BenchmarkResults.getFormats());
		
		f.set(null, endingsOld);
	}

}
