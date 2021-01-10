package io.jenkins.plugins.benchmark.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.jenkins.plugins.benchmark.HelperClass;

public class BenchmarkConfigurationTest {

	private String testPath = HelperClass.testdir;
	
	@Before
	public void createTestDir(){
		HelperClass.createTestDir();
	}
	
	@Before
	public void createTestConfigFiles(){
		String[] contents = {
				"metrikname;minPercent_since_last_build;maxPercent_since_last_build;min_value;max_value;unit;buildNr\n"+
				"Metrik1;;10;;5000;;1\n"+
				"Metrik2;;20;;200;;1\n"+
				"Metrik3;;22.555;;10;;1\n",
				
				"metrikname;percent_since_last_build;max_ms\n"+
				"Metrik1;10;5000;1\n"+
				"Metrik2;20;;1\n"+
				"Metrik3;;30;1\n"
		};
		
		String[] names = {"testConfig", "testConfig1"};
		
		for (int i = 0; i < contents.length; i++) {
			HelperClass.writeTestFile(names[i] + ".config", contents[i]);
		}
	}
	
	@After
	public void delete(){
		HelperClass.deleteTestFiles();
	}

	@Test
	public void testFirstSave() throws InterruptedException {
		File f1 = new File(testPath + File.separatorChar + "config1.config");
		assertEquals(false, f1.exists());
		BenchmarkConfiguration.getConfig(f1.getAbsolutePath());
		assertEquals(true, f1.exists());
		f1.delete();
		assertEquals(false, f1.exists());
	}

	@Test
	public void testFirstLoad() throws InterruptedException {
		File f1 = new File(testPath + File.separatorChar + "testConfig.config");
		BenchmarkConfiguration c = BenchmarkConfiguration.getConfig(f1.getAbsolutePath());
		assertEquals(true, BenchmarkConfiguration.getConfig(f1.getAbsolutePath())==c);
		assertEquals(3, c.getSize());
		assertEquals(5000,c.get("Metrik1").getMaxValue(),0.0001);
		assertEquals(200,c.get("Metrik2").getMaxValue(),0.0001);
		assertEquals(10,c.get("Metrik3").getMaxValue(),0.0001);
		assertEquals(10,c.get("Metrik1").getMaxPercent(),0.0001);
		assertEquals(20,c.get("Metrik2").getMaxPercent(),0.0001);
		assertEquals(22.555,c.get("Metrik3").getMaxPercent(),0.0001);
	}

	@Test
	public void testAdd() throws InterruptedException, Exception {
		File f1 = new File(testPath + File.separatorChar + "config2.config");
		assertEquals(false, f1.exists());
		BenchmarkConfiguration c = BenchmarkConfiguration.getConfig(f1.getAbsolutePath());
		assertEquals(true, f1.exists());

		ConfigEntry e = new ConfigEntry(null,null,null, 10d,null,1);

		assertEquals(true,c.add("MetrikTest", e));
		assertEquals(false,c.add("MetrikTest", new ConfigEntry(null,null,null, 10d,null,2)));
		Thread.sleep(1000);
		BufferedReader in = new BufferedReader(new FileReader(f1));
		in.readLine();
		String s = in.readLine();
		String[] sa = s.split(";");
		assertEquals(7, sa.length);
		assertEquals("MetrikTest", sa[0]);
		assertEquals("", sa[2]);
		assertEquals("", sa[1]);
		assertEquals("", sa[3]);
		assertEquals("", sa[5]);
		assertEquals(10d, Double.parseDouble(sa[4]),0.0001);
		assertEquals(1, Integer.parseInt(sa[6]));

		assertEquals(null, in.readLine());

		in.close();
		f1.delete();
		assertEquals(false, f1.exists());
	}

	@Test
	public void testChange() throws InterruptedException, Exception {
		File f1 = new File(testPath + File.separatorChar + "config3.config");
		assertEquals(false, f1.exists());
		BenchmarkConfiguration c = BenchmarkConfiguration.getConfig(f1.getAbsolutePath());
		assertEquals(true, f1.exists());

		ConfigEntry e = new ConfigEntry(null,null,null, 10d,null,1);

		assertEquals(true,c.add("MetrikTest", e));

		e = new ConfigEntry(null,5.5d,null, 10d,null,1);

		assertEquals(true,c.change("MetrikTest", e));
		assertEquals(false,c.change("MetrikTest2", e));
		Thread.sleep(1000);
		BufferedReader in = new BufferedReader(new FileReader(f1));
		in.readLine();
		String s = in.readLine();
		String[] sa = s.split(";");
		assertEquals(7, sa.length);
		assertEquals("MetrikTest", sa[0]);
		assertEquals(5.5d, Double.parseDouble(sa[2]),0.0001);
		assertEquals(10d, Double.parseDouble(sa[4]),0.0001);
		assertEquals(1,Integer.parseInt(sa[6]));

		assertEquals(null, in.readLine());

		in.close();
		f1.delete();
		assertEquals(false, f1.exists());
	}

	@Test
	public void testMultiThread() throws InterruptedException, Exception {
		List<File> listF = new ArrayList<File>();
		for (int i = 4; i < 14; i++) {
			listF.add(new File(testPath + File.separatorChar + "config"+i+".config"));
		}
		for(File f:listF){
			int poolsize = 10;
			ExecutorService pool = Executors.newFixedThreadPool(poolsize);
			List<myRunable> rl = new ArrayList<myRunable>();
			long start = System.currentTimeMillis() + 1000;
			for (int i = 0; i < poolsize; i++) {
				rl.add(new myRunable(f.getAbsolutePath(),i,start));
			}
			for(Runnable r : rl){
				pool.execute(r);
			}
			pool.shutdown();
			pool.awaitTermination(100, TimeUnit.SECONDS);
			BenchmarkConfiguration c = rl.get(0).c;
			for (int i = 1; i < rl.size(); i++) {
				assertEquals(c, rl.get(i).c);
			}
			for (int i = 0; i < poolsize; i++) {
				ConfigEntry ce = c.get("Metrik" + i);
				assertEquals(i*10d, ce.getMaxPercent(),0.00001);
				assertEquals(i*22d, ce.getMaxValue(),0.00001);
			}
			assertEquals(poolsize, countRows(f));
			f.delete();
		}
	}

	@Test
	public void testMultiThread1() throws InterruptedException, Exception {
		File f1 = new File(testPath + File.separatorChar + "config14.config");
		int poolsize = 50;
		ExecutorService pool = Executors.newFixedThreadPool(poolsize);
		
		BenchmarkConfiguration c = BenchmarkConfiguration.getConfig(f1.getAbsolutePath());
		int j = 0;
		for(int i = 0;i<poolsize*2;i++){
			pool.execute(new myRunable2(j,c));
			j+=100;
		}
		pool.shutdown();
		pool.awaitTermination(1000, TimeUnit.DAYS);
		for (int t = 0; t < j + 20; t++) {
			assertNotNull(c.get("Metrik"+t));
		}
		//+1, weil die 0 inkl ist -100, da die letzten 100 nicht inkl sind.
		assertEquals(j + 120 + 1 - 100, c.getSize());
		assertEquals(c.getSize(), countRows(f1));
		f1.delete();
	}

	private class myRunable implements Runnable{
		int i;
		long start;
		public BenchmarkConfiguration c;
		private String path;

		public myRunable(String path,int i,long start) {
			this.path = path;
			this.i = i;
			this.start = start;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(start - System.currentTimeMillis());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			c = BenchmarkConfiguration.getConfig(path);
			try {
				Thread.sleep(start + 1000 - System.currentTimeMillis());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			ConfigEntry ce = new ConfigEntry(null,i*10d,null, i*22d,null,1);
			assertEquals(true,c.add("Metrik" + i, ce));
		}

	}
	
	@Test
	public void wrongFormatTest(){
		assertNull(BenchmarkConfiguration.getConfig("file.csv"));
	}
	
	@Test
	public void loadAtSameTime() throws InterruptedException{
		File f1 = new File(testPath + File.separatorChar + "config15.config");
		ExecutorService pool = Executors.newFixedThreadPool(50);
		
		Set<BenchmarkConfiguration> s = new HashSet<>();
		
		for (int i = 0; i < 50; i++) {
			pool.execute(()->s.add(BenchmarkConfiguration.getConfig(f1.getAbsolutePath())));
		}
		
		pool.shutdown();
		pool.awaitTermination(1000, TimeUnit.DAYS);
		assertEquals(1, s.size());
	}
	
	
	
//	@Test
//	public void unWritableFile() throws InterruptedException{
//		File f1 = new File("C:"+File.separatorChar +"Windows"+File.separatorChar +"config16.config");
//		assertNull(BenchmarkConfiguration.getConfig(f1.getAbsolutePath()));
//	}
	
	@Test
	public void wrongFormatedConfig() {
		File f1 = new File(testPath + File.separatorChar + "config17.config");
		String t = "Metrik1;10;11;12;13;;1\n"
				+ "Metrik2;A;B;C;D;UNIT;-99\n"
				+ "Metrik3;;UNIT;4\n"
				+ "Metrik4;--1;3.420.212;null;4;UNIT;Qwert\n"
				+ "Metrik5;1;2;3;4;UNIT;\n"
				+ "Metrik6;1;2;3;4;UNIT;aws;\n";
		HelperClass.writeFile(f1.getPath(), t);
		BenchmarkConfiguration config = BenchmarkConfiguration.getConfig(f1.getAbsolutePath());
		ConfigEntry c = config.get("Metrik1");
		assertEquals(10d, c.getMinPercent(),1E-10);
		assertEquals(11d, c.getMaxPercent(),1E-10);
		assertEquals(12d, c.getMinValue(),1E-10);
		assertEquals(13d, c.getMaxValue(),1E-10);
		assertNull(c.getUnit());
		assertEquals(1, c.getBuildNr());
		c = config.get("Metrik2");
		assertNotNull(c);
		assertNull(c.getMinPercent());
		assertNull(c.getMaxPercent());
		assertNull(c.getMinValue());
		assertNull(c.getMaxValue());
		assertEquals("UNIT", c.getUnit());
		assertEquals(-1, c.getBuildNr());
		c = config.get("Metrik3");
		assertNull(c);
		c = config.get("Metrik4");
		assertNull(c);
		c = config.get("Metrik5");
		assertNull(c);
		c = config.get("Metrik6");
		assertNull(c);
	}
	
	@Test
	public void rightFormatedConfig() {
		File f1 = new File(testPath + File.separatorChar + "config18.config");
		String t = "Metrik1;-10;11;-12;13;;1\n";
		HelperClass.writeFile(f1.getPath(), t);
		BenchmarkConfiguration config = BenchmarkConfiguration.getConfig(f1.getAbsolutePath());
		ConfigEntry c = config.get("Metrik1");
		assertEquals(-10d, c.getMinPercent(),1E-10);
		assertEquals(11d, c.getMaxPercent(),1E-10);
		assertEquals(-12d, c.getMinValue(),1E-10);
		assertEquals(13d, c.getMaxValue(),1E-10);
		assertNull(c.getUnit());
		assertEquals(1, c.getBuildNr());
	}
	
	private int countRows(File f1){
		BufferedReader in;
		int rows = 0;
		try {
			Thread.sleep(1000);
			in = new BufferedReader(new FileReader(f1));
			while(in.readLine()!=null){
				rows++;
			}
			in.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} catch (InterruptedException e) {
		}
		return rows-1;
	}
	
	private class myRunable2 implements Runnable{

		int j;
		BenchmarkConfiguration c;
		
		public myRunable2(int j, BenchmarkConfiguration c) {
			this.j = j;
			this.c = c;
		}

		@Override
		public void run() {
			for(int x = j;x<=j+120;x++){
				ConfigEntry cc = new ConfigEntry(null,Math.random()*100,null,Math.random()*100000,null,1);
				if(x>j+21 && x<j+99)
					assertEquals(true,c.add("Metrik"+x, cc));
				else
					c.add("Metrik"+x, cc);
			}
		}
		
	}

}
