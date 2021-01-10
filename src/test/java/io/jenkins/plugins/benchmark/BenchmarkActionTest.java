package io.jenkins.plugins.benchmark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.Builder;
import io.jenkins.plugins.benchmark.configuration.BenchmarkConfiguration;
import io.jenkins.plugins.benchmark.configuration.ConfigEntry;

public class BenchmarkActionTest {

	@Rule public JenkinsRule j = new JenkinsRule(); 

	private static String testdir = HelperClass.testdir;
	
	@Before
	public void createTestDir(){
		HelperClass.createTestDir();
	}
	
	@After
	public void delete(){
		HelperClass.deleteTestFiles();
	}
 

	@Test 
	public void getJsons() throws Exception {
		FreeStyleProject project = j.createFreeStyleProject();
		String file = "testAction1.csv";
		BenchmarkBuilder builder = new BenchmarkBuilder(testdir + File.separatorChar +file);
		project.getBuildersList().add(builder);

		//RUN 1

		HelperClass.writeFile(testdir + File.separatorChar +file, "Metrik1;25\nMetrik2;16");

		FreeStyleBuild build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.SUCCESS,build.getResult());

		BenchmarkAction a = build.getActions(BenchmarkAction.class).get(0);
		BenchmarkConfiguration conf = BenchmarkConfiguration.getConfig(getCurrentWorkspace(build)+"config"+builder.getID()+".config");

		String firstResult = a.getFirstResultsAsJson();
		String lastResult = a.getLastResultsAsJson();
		String curResult = a.getCurrentResultsAsJson();
		String lastStableResult = a.getLastStableResultAsJson();

		assertEquals("undefined", firstResult);
		assertEquals("undefined", lastResult);
		assertEquals("undefined", lastStableResult);
		check(curResult,25d,16d);

		conf.change("Metrik1", new ConfigEntry(null,null,null, 30d, "Unitname",1));

		//RUN 2
		System.out.println("RUN 2");

		HelperClass.writeFile(testdir + File.separatorChar +file, "Metrik1;31\nMetrik2;17");

		build = project.scheduleBuild2(0).get();
		assertEquals(Result.FAILURE,build.getResult());

		a = build.getActions(BenchmarkAction.class).get(0);
		firstResult = a.getFirstResultsAsJson();
		lastResult = a.getLastResultsAsJson();
		curResult = a.getCurrentResultsAsJson();
		lastStableResult = a.getLastStableResultAsJson();

		check(firstResult,25d,16d);
		check(lastResult,25d,16d);
		check(lastStableResult,25d,16d);
		check(curResult,31d,17d);

		//RUN 3
		System.out.println("RUN 3");

		HelperClass.writeFile(testdir + File.separatorChar+file, "Metrik1;30\nMetrik2;18");
		build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.SUCCESS,build.getResult());

		a = build.getActions(BenchmarkAction.class).get(0);
		firstResult = a.getFirstResultsAsJson();
		lastResult = a.getLastResultsAsJson();
		curResult = a.getCurrentResultsAsJson();
		lastStableResult = a.getLastStableResultAsJson();

		check(firstResult,25d,16d);
		check(lastResult,31d,17d);
		check(lastStableResult,25d,16d);
		check(curResult,30d,18d);

		//RUN 4
		System.out.println("RUN 4");

		HelperClass.writeFile(testdir + File.separatorChar+file, "Metrik1;40\nMetrik2;18");
		build = project.scheduleBuild2(0).get();
		assertEquals(Result.FAILURE,build.getResult());

		a = build.getActions(BenchmarkAction.class).get(0);
		firstResult = a.getFirstResultsAsJson();
		lastResult = a.getLastResultsAsJson();
		curResult = a.getCurrentResultsAsJson();
		lastStableResult = a.getLastStableResultAsJson();

		check(curResult,40d,18d);
		check(firstResult,25d,16d);
		check(lastResult,30d,18d);
		check(lastStableResult,30d,18d);

		//RUN 5
		System.out.println("RUN 5");

		HelperClass.writeFile(testdir + File.separatorChar+file, "Metrik1;aa\nMetrik2;18");
		build = project.scheduleBuild2(0).get();
		assertEquals(Result.FAILURE,build.getResult());

		a = build.getActions(BenchmarkAction.class).get(0);
		firstResult = a.getFirstResultsAsJson();
		lastResult = a.getLastResultsAsJson();
		curResult = a.getCurrentResultsAsJson();
		lastStableResult = a.getLastStableResultAsJson();

		assertEquals("undefined",curResult);
		assertEquals("undefined",firstResult);
		assertEquals("undefined",lastResult);
		assertEquals("undefined",lastStableResult);


		//RUN 6
		System.out.println("RUN 6");


		HelperClass.writeFile(testdir + File.separatorChar+file, "Metrik1;41\nMetrik2;19");
		build = project.scheduleBuild2(0).get();
		assertEquals(Result.FAILURE,build.getResult());

		a = build.getActions(BenchmarkAction.class).get(0);
		firstResult = a.getFirstResultsAsJson();
		lastResult = a.getLastResultsAsJson();
		curResult = a.getCurrentResultsAsJson();
		lastStableResult = a.getLastStableResultAsJson();

		check(curResult,41d,19d);
		check(firstResult,25d,16d);
		check(lastResult,40d,18d);
		check(lastStableResult,30d,18d);

		//RUN 7
		System.out.println("RUN 7");

		HelperClass.writeFile(testdir + File.separatorChar+file, "Metrik1;15\nMetrik2;17");
		build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.SUCCESS,build.getResult());

		a = build.getActions(BenchmarkAction.class).get(0);
		firstResult = a.getFirstResultsAsJson();
		lastResult = a.getLastResultsAsJson();
		curResult = a.getCurrentResultsAsJson();
		lastStableResult = a.getLastStableResultAsJson();

		check(curResult,15d,17d);
		check(firstResult,25d,16d);
		check(lastResult,41d,19d);
		check(lastStableResult,30d,18d);
		
		//RUN 8
		System.out.println("RUN 8");

		HelperClass.writeFile(testdir + File.separatorChar+file, "Metrik1;18");
		build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.SUCCESS,build.getResult());

		a = build.getActions(BenchmarkAction.class).get(0);
		firstResult = a.getFirstResultsAsJson();
		lastResult = a.getLastResultsAsJson();
		curResult = a.getCurrentResultsAsJson();
		lastStableResult = a.getLastStableResultAsJson();

		check(curResult,18d,null);
		check(firstResult,25d,null);
		check(lastResult,15d,17d);
		check(lastStableResult,15d,17d);
		
		//RUN 9
		System.out.println("RUN 9");

		HelperClass.writeFile(testdir + File.separatorChar+file, "Metrik1;15\nMetrik2;17");
		build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.SUCCESS,build.getResult());

		a = build.getActions(BenchmarkAction.class).get(0);
		firstResult = a.getFirstResultsAsJson();
		lastResult = a.getLastResultsAsJson();
		curResult = a.getCurrentResultsAsJson();
		lastStableResult = a.getLastStableResultAsJson();

		check(curResult,15d,17d);
		check(firstResult,25d,16d);
		check(lastResult,18d,null);
		check(lastStableResult,18d,null);

		//Other
		System.out.println("OTHER");

		assertEquals(testdir + File.separatorChar+file,a.getFilePath());
		assertEquals("benchmark_1",a.getUrlName());
		assertEquals(Messages.benchmark_result()+" 1",a.getDisplayName());
		assertEquals(conf.getConfigAsJson(), a.getConfigAsJson());
		assertEquals(build, a.getRun());

		//Coverage
		a.getIconFileName();
		
		assertTrue(a.getUnits(),a.getUnits().contains("'Metrik1':'Unitname'"));
		assertFalse(a.getUnits(),a.getUnits().contains("'Metrik2'"));
	}
	
	@Test
	public void startErrors() throws Exception{
		FreeStyleProject project = j.createFreeStyleProject();
		String file = "testAction4.csv";
		BenchmarkBuilder builder = new BenchmarkBuilder(testdir + File.separatorChar+file);
		project.getBuildersList().add(builder);

		//RUN 1

		HelperClass.writeFile(testdir + File.separatorChar+file, "Metrik1;25\nMetrik2;16");

		FreeStyleBuild build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.SUCCESS,build.getResult());

		BenchmarkAction a = build.getActions(BenchmarkAction.class).get(0);
		BenchmarkConfiguration conf = BenchmarkConfiguration.getConfig(getCurrentWorkspace(build)+"config"+builder.getID()+".config");

		String firstResult = a.getFirstResultsAsJson();
		String lastResult = a.getLastResultsAsJson();
		String curResult = a.getCurrentResultsAsJson();
		String lastStableResult = a.getLastStableResultAsJson();

		assertEquals("undefined", firstResult);
		assertEquals("undefined", lastResult);
		assertEquals("undefined", lastStableResult);
		check(curResult,25d,16d);

		conf.change("Metrik1", new ConfigEntry(null,null,null, 30d,null,1));
		
		//RUN 1

		HelperClass.writeFile(testdir + File.separatorChar+file, "Metrik1;35\nMetrik2;16\nMetrik3;188");

		build = project.scheduleBuild2(0).get();
		assertEquals(Result.FAILURE,build.getResult());

		a = build.getActions(BenchmarkAction.class).get(0);
		firstResult = a.getFirstResultsAsJson();
		lastResult = a.getLastResultsAsJson();
		curResult = a.getCurrentResultsAsJson();
		lastStableResult = a.getLastStableResultAsJson();

		check(firstResult, 25d, 16d);
		check(lastResult, 25d, 16d);
		check(lastStableResult, 25d, 16d);
		check(curResult,35d,16d);
	}
	
	@Test
	public void onRunTest() {
		new BenchmarkAction(null,null,false,null).onLoad(null);
	}
	
	private String getCurrentWorkspace(Run<?, ?> run){
		String s = run.getRootDir().getAbsolutePath();
		s = s.substring(0,s.lastIndexOf(File.separator));
		s = s.substring(0,s.lastIndexOf(File.separator)+1);
		return s;
	}

	@Test
	public void errorAtFirstTime() throws Exception{
		FreeStyleProject project = j.createFreeStyleProject();
		String file = "testAction2.csv";
		Builder builder = new BenchmarkBuilder(testdir + File.separatorChar+file);
		project.getBuildersList().add(builder);

		//RUN 1

		HelperClass.writeFile(testdir + File.separatorChar+file, "Metrik1;21a\nMetrik2;16");

		FreeStyleBuild build = project.scheduleBuild2(0).get();
		assertEquals(Result.FAILURE,build.getResult());

		BenchmarkAction a = build.getActions(BenchmarkAction.class).get(0);

		String firstResult = a.getFirstResultsAsJson();
		String lastResult = a.getLastResultsAsJson();
		String curResult = a.getCurrentResultsAsJson();

		assertEquals("undefined", firstResult);
		assertEquals("undefined", lastResult);
		assertEquals("undefined", curResult);
		assertFalse(a.wasCurrentActionSuccesfull());
		
		//RUN 2
		
		HelperClass.writeFile(testdir + File.separatorChar+file, "Metrik1;21\nMetrik2;16");

		build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.SUCCESS,build.getResult());

		a = build.getActions(BenchmarkAction.class).get(0);

		firstResult = a.getFirstResultsAsJson();
		lastResult = a.getLastResultsAsJson();
		curResult = a.getCurrentResultsAsJson();

		assertEquals("undefined", firstResult);
		assertEquals("undefined", lastResult);
		check(curResult,21d,16d);
		assertTrue(a.wasCurrentActionSuccesfull());
		
		//RUN 3
		
		HelperClass.writeFile(testdir + File.separatorChar+file, "Metrik1;26\nMetrik2;1823");

		build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.SUCCESS,build.getResult());

		a = build.getActions(BenchmarkAction.class).get(0);

		firstResult = a.getFirstResultsAsJson();
		lastResult = a.getLastResultsAsJson();
		curResult = a.getCurrentResultsAsJson();

		check(lastResult,21d,16d);
		check(firstResult,21d,16d);
		check(curResult,26d,1823d);
		assertTrue(a.wasCurrentActionSuccesfull());
	}

	@Test
	public void builderAddedLater() throws Exception{
		
		FreeStyleProject project = j.createFreeStyleProject();
		String file = "testAction3.csv";

		//RUN 1

		FreeStyleBuild build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.SUCCESS,build.getResult());
		
		//RUN 2
		
		Builder builder = new BenchmarkBuilder(testdir + File.separatorChar+file);
		project.getBuildersList().add(builder);
		
		HelperClass.writeFile(testdir + File.separatorChar+file, "Metrik1;55\nMetrik2;66");
		build = project.scheduleBuild2(0).get();

		BenchmarkAction a = build.getActions(BenchmarkAction.class).get(0);

		String firstResult = a.getFirstResultsAsJson();
		String lastResult = a.getLastResultsAsJson();
		String curResult = a.getCurrentResultsAsJson();

		assertEquals("undefined", firstResult);
		assertEquals("undefined", lastResult);
		check(curResult,55d,66d);
		
		//RUN 2
		
		HelperClass.writeFile(testdir + File.separatorChar+file, "Metrik1;21\nMetrik2;16");

		build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.SUCCESS,build.getResult());

		a = build.getActions(BenchmarkAction.class).get(0);

		firstResult = a.getFirstResultsAsJson();
		lastResult = a.getLastResultsAsJson();
		curResult = a.getCurrentResultsAsJson();

		check(lastResult,55d,66d);
		check(firstResult,55d,66d);
		check(curResult,21d,16d);
		
	}
	
	@Test
	public void twoBuilds() throws InterruptedException, ExecutionException, IOException{
		
		FreeStyleProject project = j.createFreeStyleProject();
		String d1 = "testAction100.csv";
		String d2 = "testAction101.csv";
		
		//RUN 2
		
		Builder builder1 = new BenchmarkBuilder(testdir + File.separatorChar+ d1);
		project.getBuildersList().add(builder1);
		Builder builder2 = new BenchmarkBuilder(testdir + File.separatorChar+ d2);
		project.getBuildersList().add(builder2);
		
		HelperClass.writeFile(testdir + File.separatorChar+d1, "Metrik1;55\nMetrik2;66");
		HelperClass.writeFile(testdir + File.separatorChar+d2, "Metrik1;-55\nMetrik2;-66");
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		BenchmarkAction a = build.getActions(BenchmarkAction.class).get(0);
		
		String firstResult = a.getFirstResultsAsJson();
		String lastResult = a.getLastResultsAsJson();
		String curResult = a.getCurrentResultsAsJson();

		assertEquals("undefined", firstResult);
		assertEquals("undefined", lastResult);
		check(curResult,55d,66d);
		
		a = build.getActions(BenchmarkAction.class).get(1);
		
		firstResult = a.getFirstResultsAsJson();
		lastResult = a.getLastResultsAsJson();
		curResult = a.getCurrentResultsAsJson();

		assertEquals("undefined", firstResult);
		assertEquals("undefined", lastResult);
		check(curResult,-55d,-66d);
	}

	private void check(String input, Double metrik1,Double metrik2){
		JsonObject jO = JsonParser.parseString(input).getAsJsonObject();
		if(metrik1!=null){
			assertTrue(jO.has("Metrik1"));
			assertEquals(metrik1, jO.get("Metrik1").getAsDouble(),0.000001);
		}else
			assertFalse(jO.has("Metrik1"));
		if(metrik2!=null){
			assertTrue(jO.has("Metrik2"));
			assertEquals(metrik2, jO.get("Metrik2").getAsDouble(),0.000001);
		}else
			assertFalse(jO.has("Metrik2"));
	}

}
