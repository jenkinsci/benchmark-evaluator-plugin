package io.jenkins.plugins.benchmark.configuration;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.jenkins.plugins.benchmark.HelperClass;
import org.jvnet.hudson.test.JenkinsRule;

public class BenchmarkConfigLoaderTest {

	@Rule
	public JenkinsRule j = new JenkinsRule();
	private String testdir;
	private HelperClass helper;

	@Before
	public void createTestDir(){
		helper = new HelperClass(j);
		testdir = helper.createTestDir();
	}
	
	@After
	public void delete(){
		helper.deleteTestFiles();
	}
 

	@Test
	public void isNumberTest() throws Exception {
		
		File f1 = new File(testdir + File.separatorChar + "configLoad1.config");
		String t = "Metrik1;10;11;12;13;;1\n";
		helper.writeFile(f1.getPath(), t);
		
		Method m = BenchmarkConfigLoader.class.getDeclaredMethod("isDouble", String.class);
		m.setAccessible(true);
		BenchmarkConfigLoader bl = new BenchmarkConfigLoader(f1.getPath(),new HashMap<>());
		String in = null;
		assertEquals(Boolean.FALSE,m.invoke(bl, in));
		assertEquals(Boolean.FALSE,m.invoke(bl, ""));
		assertEquals(Boolean.FALSE,m.invoke(bl, "3.555.999"));
		assertEquals(Boolean.FALSE,m.invoke(bl, "5-10"));
		
		m = BenchmarkConfigLoader.class.getDeclaredMethod("isInteger", String.class);
		m.setAccessible(true);
		assertEquals(Boolean.FALSE,m.invoke(bl, in));
		assertEquals(Boolean.FALSE,m.invoke(bl, ""));
		assertEquals(Boolean.FALSE,m.invoke(bl, "3.555.999"));
		assertEquals(Boolean.FALSE,m.invoke(bl, "5-10"));
		assertEquals(Boolean.FALSE,m.invoke(bl, "5A10"));
		assertEquals(Boolean.FALSE,m.invoke(bl, "5{10}"));
	}
	
}
