package io.jenkins.plugins.benchmark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.io.File;
import java.util.Map.Entry;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;

public class BenchmarkResultActionTest {


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
	public void resultTest() throws Exception {
		

		FreeStyleProject project = j.createFreeStyleProject();
		String file = "resultsTest1.csv";
		BenchmarkBuilder builder = new BenchmarkBuilder(testdir + File.separatorChar+file);
		project.getBuildersList().add(builder);

		BenchmarkResultAction benchmarkResult = (BenchmarkResultAction) project.getProminentActions().stream().filter(x->x instanceof BenchmarkResultAction).findFirst().get();
		BenchmarkConfigAction configAction = (BenchmarkConfigAction) project.getProminentActions().stream().filter(x->x instanceof BenchmarkConfigAction).findFirst().get();
		BenchmarkResultAction tProject = new BenchmarkResultAction(j.createFreeStyleProject(),builder);
		
		assertEquals("benchmarkResult_1",benchmarkResult.getUrlName());
		assertEquals(null,tProject.getUrlName());
		assertEquals("Benchmark Results 1",benchmarkResult.getDisplayName());
		assertEquals(null,tProject.getDisplayName());
		assertNotNull(benchmarkResult.getIconFileName());
		assertNull(tProject.getIconFileName());
		
		String result = benchmarkResult.getAllInformations();
		assertEquals(result, benchmarkResult.getAllInformations());

		allEquals(result, "[]", "[]", "{}","{}", "[]");
		
		HelperClass.writeTestFile(file, "Metrik1;25\nMetrik2;16");
		FreeStyleBuild build = project.scheduleBuild2(0).get();
		assertEquals(Result.SUCCESS,build.getResult());
		
		result = benchmarkResult.getAllInformations();
		allEquals(result, "['#1']", "[]", "{\"Metrik1\":[[25.0,null,null,null,null,null]],\"Metrik2\":[[16.0,null,null,null,null,null]]}","{}", "[null]");
		
		HelperClass.writeFile(testdir + File.separatorChar+file, "Metrik1;25\nMetrik2;16a\nname;thisIstheName");
		build = project.scheduleBuild2(0).get();
		assertEquals(Result.FAILURE,build.getResult());
		
		result = benchmarkResult.getAllInformations();
		allEquals(result, "['#1','#2']", "['#2']"
				, "{\"Metrik1\":[[25.0,null,null,null,null,null],[null,null,null,null,null,null]],\"Metrik2\":[[16.0,null,null,null,null,null],[null,null,null,null,null,null]]}","{}", "[null,\"thisIstheName\"]");
		
		HelperClass.writeFile(testdir + File.separatorChar+file, "Metrik1;50\nMetrik2;16\nName;thisIstheName2");
		build = project.scheduleBuild2(0).get();
		assertEquals(Result.SUCCESS,build.getResult());
		
		result = benchmarkResult.getAllInformations();
		allEquals(result, "['#1','#2','#3']", "['#2']", "{"
				+ "\"Metrik1\":[[25.0,null,null,null,null,null],[null,null,null,null,null,null],[50.0,null,null,1.0,null,null]],"
				+ "\"Metrik2\":[[16.0,null,null,null,null,null],[null,null,null,null,null,null],[16.0,null,null,0.0,null,null]]"
				+ "}","{}", "[null,\"thisIstheName\",\"thisIstheName2\"]");
		
		project.getBuildByNumber(2).delete();
		result = benchmarkResult.getAllInformations();
		allEquals(result, "['#1','#3']", "[]", "{"
				+ "\"Metrik1\":[[25.0,null,null,null,null,null],[50.0,null,null,1.0,null,null]],"
				+ "\"Metrik2\":[[16.0,null,null,null,null,null],[16.0,null,null,0.0,null,null]]"
				+ "}","{}", "[null,\"thisIstheName2\"]");
		
		configAction.setMetricUnit("Metrik1", "m1");
		configAction.createMetric("Metrik500");
		configAction.setMetricUnit("Metrik500", "m500");
		configAction.createMetric("Metrik3");
		configAction.setMetricUnit("Metrik3", "m3");
		
		HelperClass.writeFile(testdir + File.separatorChar+file, "Metrik1;75\nMetrik2;8");
		build = project.scheduleBuild2(0).get();
		assertEquals(Result.SUCCESS,build.getResult());
		project.getBuildByNumber(3).delete();
		result = benchmarkResult.getAllInformations();
		allEquals(result, "['#1','#4']", "[]", "{"
				+ "\"Metrik1\":[[25.0,null,null,null,null,null],[75.0,null,null,2.0,null,null]],"
				+ "\"Metrik2\":[[16.0,null,null,null,null,null],[8.0,null,null,-0.5,null,null]]"
				+ "}","{'Metrik1':'m1'}", "[null,null]");
		
		assertEquals(result, benchmarkResult.getAllInformations());
		
		configAction.setMetricMaxValue("Metrik1", 100d);
		configAction.setMetricMaxPercent("Metrik1", 200d);
		
		assertEquals(result, benchmarkResult.getAllInformations());
		
		HelperClass.writeFile(testdir + File.separatorChar+file, "Metrik1;150\nMetrik2;16\nName;thisIstheName3");
		build = project.scheduleBuild2(0).get();
		assertEquals(Result.FAILURE,build.getResult());
		
		result = benchmarkResult.getAllInformations();
		allEquals(result, "['#1','#4','#5']", "['#5']", "{"
				+ "\"Metrik1\":[[25.0,null,null,null,null,null],[75.0,null,null,2.0,null,null],[150.0,null,100.0,1.0,null,2.0]],"
				+ "\"Metrik2\":[[16.0,null,null,null,null,null],[8.0,null,null,-0.5,null,null],[16.0,null,null,1.0,null,null]]"
				+ "}","{'Metrik1':'m1'}", "[null,null,\"thisIstheName3\"]");
		
		HelperClass.writeFile(testdir + File.separatorChar+file, "Name;thisIstheName4\nMetrik1;75\nMetrik2;32\nMetrik3;30");
		build = project.scheduleBuild2(0).get();
		assertEquals(Result.SUCCESS,build.getResult());
		
		result = benchmarkResult.getAllInformations();
		allEquals(result, "['#1','#4','#5','#6']", "['#5']", "{"
				+ "\"Metrik1\":[[25.0,null,null,null,null,null],[75.0,null,null,2.0,null,null],[150.0,null,100.0,1.0,null,2.0],[75.0,null,100.0,0.0,null,2.0]],"
				+ "\"Metrik2\":[[16.0,null,null,null,null,null],[8.0,null,null,-0.5,null,null],[16.0,null,null,1.0,null,null],[32.0,null,null,3.0,null,null]],"
				+ "\"Metrik3\":[[null,null,null,null,null,null],[null,null,null,null,null,null],[null,null,null,null,null,null],[30.0,null,null,null,null,null]]"
				+ "}","{'Metrik1':'m1','Metrik3':'m3'}", "[null,null,\"thisIstheName3\",\"thisIstheName4\"]");
		
		configAction.setMetricMinValue("Metrik1", 95d);
		
		HelperClass.writeFile(testdir + File.separatorChar+file, "Name;thisIstheName5\nMetrik1;90\nMetrik2;50\nMetrik3;90");
		build = project.scheduleBuild2(0).get();
		assertEquals(Result.FAILURE,build.getResult());
		
		result = benchmarkResult.getAllInformations();
		allEquals(result, "['#1','#4','#5','#6','#7']", "['#5','#7']", "{"
				+ "\"Metrik1\":[[25.0,null,null,null,null,null],[75.0,null,null,2.0,null,null],[150.0,null,100.0,1.0,null,2.0],[75.0,null,100.0,0.0,null,2.0],[90.0,95.0,100.0,"+(90d/75d-1d)+",null,2.0]],"
				+ "\"Metrik2\":[[16.0,null,null,null,null,null],[8.0,null,null,-0.5,null,null],[16.0,null,null,1.0,null,null],[32.0,null,null,3.0,null,null],[50.0,null,null,0.5625,null,null]],"
				+ "\"Metrik3\":[[null,null,null,null,null,null],[null,null,null,null,null,null],[null,null,null,null,null,null],[30.0,null,null,null,null,null],[90.0,null,null,2.0,null,null]]"
				+ "}","{'Metrik1':'m1','Metrik3':'m3'}", "[null,null,\"thisIstheName3\",\"thisIstheName4\",\"thisIstheName5\"]");
		
		configAction.setMetricMaxValue("Metrik1", 110d);
		configAction.setMetricMinValue("Metrik1", 80d);
		configAction.setMetricMaxPercent("Metrik1", 210d);
		configAction.setMetricMinPercent("Metrik1", 10d);
		
		build = project.scheduleBuild2(0).get();
		assertEquals(Result.SUCCESS,build.getResult());
		
		String dataSet2 = "{"
				+ "\"Metrik1\":[[25.0,null,null,null,null,null],[75.0,null,null,2.0,null,null],[150.0,null,100.0,1.0,null,2.0],[75.0,null,100.0,0.0,null,2.0],"
				+ "[90.0,95.0,100.0,"+(90d/75d-1d)+",null,2.0],[90.0,80.0,110.0,"+(90d/75d-1d)+",0.1,2.1]],"
				
				+ "\"Metrik2\":[[16.0,null,null,null,null,null],[8.0,null,null,-0.5,null,null],[16.0,null,null,1.0,null,null],[32.0,null,null,3.0,null,null],"
				+ "[50.0,null,null,0.5625,null,null],[50.0,null,null,0.5625,null,null]],"
				
				+ "\"Metrik3\":[[null,null,null,null,null,null],[null,null,null,null,null,null],[null,null,null,null,null,null],[30.0,null,null,null,null,null],"
				+ "[90.0,null,null,2.0,null,null],[90.0,null,null,2.0,null,null]]"
				+ "}";
		
		String builds = "['#1','#4','#5','#6','#7','#8']";
		String unsuccBuilds = "['#5','#7']";
		
		result = benchmarkResult.getAllInformations();
		allEquals(result, builds, unsuccBuilds, dataSet2, 
				"{'Metrik1':'m1','Metrik3':'m3'}", "[null,null,\"thisIstheName3\",\"thisIstheName4\",\"thisIstheName5\",\"thisIstheName5\"]");
		
		result = benchmarkResult.getAllInformations();
		allEquals(result, builds, unsuccBuilds, dataSet2, "{'Metrik1':'m1','Metrik3':'m3'}",
				"[null,null,\"thisIstheName3\",\"thisIstheName4\",\"thisIstheName5\",\"thisIstheName5\"]");
		
		
		configAction.setMetricUnit("Metrik3", "someElse");
		
		result = benchmarkResult.getAllInformations();
		allEquals(result, builds, unsuccBuilds, dataSet2, "{'Metrik1':'m1','Metrik3':'someElse'}",
				"[null,null,\"thisIstheName3\",\"thisIstheName4\",\"thisIstheName5\",\"thisIstheName5\"]");
		
		project = j.createFreeStyleProject();
		file = "resultsTest2.csv";
		builder = new BenchmarkBuilder(testdir + File.separatorChar+file);
		project.scheduleBuild2(0).get();
		project.getBuildersList().add(builder);
		benchmarkResult = (BenchmarkResultAction) project.getProminentActions().stream().filter(x->x instanceof BenchmarkResultAction).findFirst().get();
		HelperClass.writeFile(testdir + File.separatorChar+file, "Metrik1;25\nName;THEName\nMetrik2;16");
		project.scheduleBuild2(0).get();
		result = benchmarkResult.getAllInformations();
		allEquals(result, "['#2']", "[]", "{\"Metrik1\":[[25.0,null,null,null,null,null]],\"Metrik2\":[[16.0,null,null,null,null,null]]}","{}",
				"[\"THEName\"]");
		
		
	}
	
	@Test
	public void negativValues() throws Exception{
		FreeStyleProject project = j.createFreeStyleProject();
		String file = "resultsTest5.csv";
		BenchmarkBuilder builder = new BenchmarkBuilder(testdir + File.separatorChar+file);
		project.getBuildersList().add(builder);

		BenchmarkResultAction benchmarkResult = (BenchmarkResultAction) project.getProminentActions().stream().filter(x->x instanceof BenchmarkResultAction).findFirst().get();
		BenchmarkConfigAction configAction = (BenchmarkConfigAction) project.getProminentActions().stream().filter(x->x instanceof BenchmarkConfigAction).findFirst().get();
		
		HelperClass.writeFile(testdir + File.separatorChar+file, "Metrik1;-25\nMetrik2;10");
		FreeStyleBuild build = project.scheduleBuild2(0).get();
		assertEquals(Result.SUCCESS,build.getResult());
		
		String result = benchmarkResult.getAllInformations();
		allEquals(result, "['#1']", "[]", "{\"Metrik1\":[[-25.0,null,null,null,null,null]],\"Metrik2\":[[10.0,null,null,null,null,null]]}","{}","[null]");
		
		configAction.setMetricMaxPercent("Metrik1", 0d);
		configAction.setMetricMaxPercent("Metrik2", 0d);
		
		HelperClass.writeFile(testdir + File.separatorChar+file, "Metrik1;-50\nMetrik2;-10");
		build = project.scheduleBuild2(0).get();
		assertEquals(Result.SUCCESS,build.getResult());
		
		result = benchmarkResult.getAllInformations();
		allEquals(result, "['#1','#2']", "[]", "{"
				+ "\"Metrik1\":[[-25.0,null,null,null,null,null],[-50.0,null,null,-1.0,null,0.0]],"
				+ "\"Metrik2\":[[10.0,null,null,null,null,null],[-10.0,null,null,-2.0,null,0.0]]"
				+ "}","{}","[null,null]");
		
		configAction.setMetricMaxPercent("Metrik1", 300d);
		configAction.setMetricMaxPercent("Metrik2", 300d);
		configAction.setMetricMinPercent("Metrik1", 0d);
		configAction.setMetricMinPercent("Metrik2", 0d);
		
		HelperClass.writeFile(testdir + File.separatorChar+file, "Metrik1;-10\nMetrik2;20");
		build = project.scheduleBuild2(0).get();
		assertEquals(Result.SUCCESS,build.getResult());
		
		result = benchmarkResult.getAllInformations();
		allEquals(result, "['#1','#2','#3']", "[]", "{"
				+ "\"Metrik1\":[[-25.0,null,null,null,null,null],[-50.0,null,null,-1.0,null,0.0],[-10.0,null,null,0.8,0.0,3.0]],"
				+ "\"Metrik2\":[[10.0,null,null,null,null,null],[-10.0,null,null,-2.0,null,0.0],[20.0,null,null,3.0,0.0,3.0]]"
				+ "}","{}","[null,null,null]");
	}
	
	@Test
	public void zeroValues() throws Exception{
		FreeStyleProject project = j.createFreeStyleProject();
		String file = "resultsTest6.csv";
		BenchmarkBuilder builder = new BenchmarkBuilder(testdir + File.separatorChar+file);
		project.getBuildersList().add(builder);

		BenchmarkResultAction benchmarkResult = (BenchmarkResultAction) project.getProminentActions().stream().filter(x->x instanceof BenchmarkResultAction).findFirst().get();
		
		HelperClass.writeFile(testdir + File.separatorChar+file, "Metrik1;0\nMetrik2;0\nMetrik3;0");
		FreeStyleBuild build = project.scheduleBuild2(0).get();
		assertEquals(Result.SUCCESS,build.getResult());
		
		String result = benchmarkResult.getAllInformations();
		allEquals(result, "['#1']", "[]", "{"
				+ "\"Metrik1\":[[0.0,null,null,null,null,null]],"
				+ "\"Metrik2\":[[0.0,null,null,null,null,null]],"
				+ "\"Metrik3\":[[0.0,null,null,null,null,null]]"
				+ "}","{}","[null]");
		
		HelperClass.writeFile(testdir + File.separatorChar+file, "Metrik1;100\nMetrik2;-200\nMetrik3;0");
		build = project.scheduleBuild2(0).get();
		assertEquals(Result.SUCCESS,build.getResult());
		
		result = benchmarkResult.getAllInformations();
		allEquals(result, "['#1','#2']", "[]", "{"
				+ "\"Metrik1\":[[0.0,null,null,null,null,null],[100.0,null,null,'inf',null,null]],"
				+ "\"Metrik2\":[[0.0,null,null,null,null,null],[-200.0,null,null,'-inf',null,null]],"
				+ "\"Metrik3\":[[0.0,null,null,null,null,null],[0.0,null,null,0.0,null,null]]"
				+ "}","{}","[null,null]");
	}
	
	@Test
	public void beforeStart() throws Exception{
		FreeStyleProject project = j.createFreeStyleProject();
		String file = "resultsTest7.csv";
		BenchmarkBuilder builder = new BenchmarkBuilder(testdir + File.separatorChar+file);
		project.getBuildersList().add(builder);

		BenchmarkResultAction benchmarkResult = (BenchmarkResultAction) project.getProminentActions().stream().filter(x->x instanceof BenchmarkResultAction).findFirst().get();
		BenchmarkConfigAction configAction = (BenchmarkConfigAction) project.getProminentActions().stream().filter(x->x instanceof BenchmarkConfigAction).findFirst().get();
		
		HelperClass.writeFile(testdir + File.separatorChar+file, "Metrik1;0\nMetrik2;0\nMetrik3;0");
		
		String result = benchmarkResult.getAllInformations();
		allEquals(result, "[]", "[]", "{}", "{}", "[]");
		
		FreeStyleBuild build = project.scheduleBuild2(0).get();
		assertEquals(Result.SUCCESS,build.getResult());
		configAction.setMetricUnit("Metrik2", "abc");
		
		result = benchmarkResult.getAllInformations();
		allEquals(result, "['#1']", "[]", "{"
				+ "\"Metrik1\":[[0.0,null,null,null,null,null]],"
				+ "\"Metrik2\":[[0.0,null,null,null,null,null]],"
				+ "\"Metrik3\":[[0.0,null,null,null,null,null]]"
				+ "}","{'Metrik2':'abc'}","[null]");
		
		configAction.setMetricUnit("Metrik2", null);
		
		result = benchmarkResult.getAllInformations();
		allEquals(result, "['#1']", "[]", "{"
				+ "\"Metrik1\":[[0.0,null,null,null,null,null]],"
				+ "\"Metrik2\":[[0.0,null,null,null,null,null]],"
				+ "\"Metrik3\":[[0.0,null,null,null,null,null]]"
				+ "}","{}","[null]");
		
		
	}
	
	private void allEquals(String result, String buildNr,String unsuccesfullBuildsNames, String dataSet2, String metricInfo, String names){
		assertEquals(buildNr, getElement("buildNr",result));
		assertEquals(unsuccesfullBuildsNames, getElement("unsuccesfullBuildsNames",result));
		assertEquals(names, getElement("buildNames",result));
		JsonParser parser = new JsonParser();
		JsonObject o1 = parser.parse(dataSet2).getAsJsonObject();
		JsonObject o2 = parser.parse(getElement("dataSet2",result)).getAsJsonObject();	
		for(Entry<String, JsonElement> a : o1.entrySet()){
			assertEquals(a.getValue().toString(), o2.get(a.getKey()).toString());
		}
		assertEquals(o1, o2);
		o1 = parser.parse(metricInfo).getAsJsonObject();
		o2 = parser.parse(getElement("metricInfo",result)).getAsJsonObject();	
		assertEquals(o1, o2);
	}
	
	private String getElement(String name,String result){
		for(String line : result.split("\n")){
			if(line.contains(name)){
				String l = line.substring(line.indexOf(name+ " = ")+name.length()+3,line.length()-1);
				return l;
			}
		}
		return null;
	}

}
