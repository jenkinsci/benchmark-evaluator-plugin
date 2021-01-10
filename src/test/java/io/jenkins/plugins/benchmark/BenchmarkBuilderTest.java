package io.jenkins.plugins.benchmark;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import io.jenkins.plugins.benchmark.BenchmarkBuilder.DescriptorImpl;
import io.jenkins.plugins.benchmark.configuration.BenchmarkConfiguration;
import io.jenkins.plugins.benchmark.configuration.ConfigEntry;
import io.jenkins.plugins.benchmark.data.BenchmarkResults;

public class BenchmarkBuilderTest {
	@Rule public JenkinsRule j = new JenkinsRule(); 

	private static String testdir = HelperClass.testdir;
	
	private int wrong;
	private int right;
	
	@Before
	public void createWrongFormatFiles(){
		String[] contents = {
				"Metrik;time_in_ms\n"+
				"Metrik1;10\n"+
				"Metrik2;a",
				
				"Metrik;time_in_ms\n"+
				"Metrik1;10\n"+
				"Metrik2;",
				
				"Metrik;time_in_ms\n"+
				"Metrik1;10\n"+
				"Metrik2;3.092;0",

				"Metrik;time_in_ms\n"+
				"Metrik1;10\n"+
				";5",

				"Metrik;time_in_ms\n"+
				"Metrik1;10.444.000",
				
				"Metrik;time_in_ms\n"+
				"Metrik1;as,10.444000\n"+
				"Metrik2;10.444000",

				"Metrik;time_in_ms\n"+
				"Metrik1;10.44\n"+
				"Metrik2,10.44\n",
				
				"Metrik;time_in_ms\n"+
				"Metrik1;10,44\n"+
				"Metrik2;10.44\n",

				"Metrik;time_in_ms\n"+
				"Metrik1.10,44\n"+
				"Metrik2.10,44",

				"Metrik;time_in_ms\n"+
				"Metrik1;10\n"+
				"Metrik2,10.44",

				"Metrik;time_in_ms\n"+
				"Metrik1;10\n"+
				"Metrik2,10,44",

				"Metrik;time_in_ms\n"+
				"Metrik1;10.444.000",
				
				"Metrik;time_in_ms\n"+
				"Metrik1;10.4a",

				"Metrik,4.0\n"+
				"Metrik1,as,10.4\n"+
				"Metrik2,-10.4",

				"Metrik;Value\n"+
				"Metrik1;1,10.4\n"+
				"Metrik2;-10.4",

				"Metrik;4.0\n"+
				"Metrik1;as;10.4\n"+
				"Metrik2;-10.4",

				"444.0\n"+
				"Metrik1;as;10.4\n"+
				"Metrik2;-10.4",
				
				"Header\n"+
				"Metrik1;10,0.4\n"+
				"Metrik2;-10.4",
				
				//Wrong filename
				"Metrik;time_in_ms\n"+
				"Metrik1;10\n"+
				"Metrik2;5"
		};
		
		wrong = contents.length - 1;
		
		for (int i = 1; i < contents.length; i++) {
			HelperClass.writeTestFile("wrongFormat" + i + ".csv", contents[i-1]);
		}
		
		HelperClass.writeTestFile("wrongFormat.wrong", contents[contents.length - 1]);
	}
	
	@Before
	public void createYCSBFiles(){
		String[] contents = {
				"[OVERALL], RunTime(ms), 114415\n[OVERALL], Throughput(ops/sec), 4370.056373727221\n[TOTAL_GCS_PS_Scavenge], Count, 529\n[TOTAL_GC_TIME_PS_Scavenge], Time(ms), 859\n[TOTAL_GC_TIME_%_PS_Scavenge], Time(%), 0.7507756850063366\n[TOTAL_GCS_PS_MarkSweep], Count, 0\n[TOTAL_GC_TIME_PS_MarkSweep], Time(ms), 0\n[TOTAL_GC_TIME_%_PS_MarkSweep], Time(%), 0.0\n[TOTAL_GCs], Count, 529\n[TOTAL_GC_TIME], Time(ms), 859\n[TOTAL_GC_TIME_%], Time(%), 0.7507756850063366\n[CLEANUP], Operations, 1\n[CLEANUP], AverageLatency(us), 2212864.0\n[CLEANUP], MinLatency(us), 2211840\n[CLEANUP], MaxLatency(us), 2213887\n[CLEANUP], 95thPercentileLatency(us), 2213887\n[CLEANUP], 99thPercentileLatency(us), 2213887\n[INSERT], Operations, 500000\n[INSERT], AverageLatency(us), 221.43646\n[INSERT], MinLatency(us), 128\n[INSERT], MaxLatency(us), 101119\n[INSERT], 95thPercentileLatency(us), 288\n[INSERT], 99thPercentileLatency(us), 449\n[INSERT], Return=OK, 500000",
				"[OVERALL], RunTime(ms), 114415\n[OVERALL], Throughput(ops/sec), 4370.056373727221a\n[TOTAL_GCS_PS_Scavenge], Count, 529\n[TOTAL_GC_TIME_PS_Scavenge], Time(ms), 859\n[TOTAL_GC_TIME_%_PS_Scavenge], Time(%), 0.7507756850063366\n[TOTAL_GCS_PS_MarkSweep], Count, 0\n[TOTAL_GC_TIME_PS_MarkSweep], Time(ms), 0\n[TOTAL_GC_TIME_%_PS_MarkSweep], Time(%), 0.0\n[TOTAL_GCs], Count, 529\n[TOTAL_GC_TIME], Time(ms), 859\n[TOTAL_GC_TIME_%], Time(%), 0.7507756850063366\n[CLEANUP], Operations, 1\n[CLEANUP], AverageLatency(us), 2212864.0\n[CLEANUP], MinLatency(us), 2211840\n[CLEANUP], MaxLatency(us), 2213887\n[CLEANUP], 95thPercentileLatency(us), 2213887\n[CLEANUP], 99thPercentileLatency(us), 2213887\n[INSERT], Operations, 500000\n[INSERT], AverageLatency(us), 221.43646\n[INSERT], MinLatency(us), 128\n[INSERT], MaxLatency(us), 101119\n[INSERT], 95thPercentileLatency(us), 288\n[INSERT], 99thPercentileLatency(us), 449\n[INSERT], Return=OK, 500000",
				"[OVERALL], RunTime(ms), 114415\n[OVERALL], Throughput(ops/sec), 4370.056373727221\n[TOTAL_GCS_PS_Scavenge], Count, 529\n[TOTAL_GC_TIME_PS_Scavenge], Time(ms), 859\n[TOTAL_GC_TIME_%_PS_Scavenge], Time(%), 0.7507756850063366\n[TOTAL_GCS_PS_MarkSweep], Count, 0\n[TOTAL_GC_TIME_PS_MarkSweep], Time(ms), 0\n[TOTAL_GC_TIME_%_PS_MarkSweep], Time(%), 0.0\n[TOTAL_GCs], Count, 529\n[TOTAL_GC_TIME], Time(ms), 859\n[TOTAL_GC_TIME_%], Time(%), 0.7507756850063366\n[CLEANUP], Operations, 1\n[CLEANUP], AverageLatency(us), 2212864.0\n[CLEANUP], MinLatency(us), 2211840\n[CLEANUP], MaxLatency(us), 2213887\n[CLEANUP], 95thPercentileLatency(us), 2213887\n[CLEANUP], 99thPercentileLatency(us), 2213887\n[INSERT], Operations, 500000\n[INSERT], AverageLatency(us), 221.43646\n[INSERT], MinLatency(us), 128\n[INSERT], MaxLatency(us), 101119\n[INSERT], 95thPercentileLatency(us), 288\n[INSERT], 99thPercentileLatency(us), 449\n[INSERT], Return=OK, 500000\n[INFO], Name, ThisBuildHasAName",
				"[OVERALL], RunTime(ms), 114415\n[OVERALL], Throughput(ops/sec), 4370.056373727221a\n[TOTAL_GCS_PS_Scavenge], Count, 529\n[TOTAL_GC_TIME_PS_Scavenge], Time(ms), 859\n[TOTAL_GC_TIME_%_PS_Scavenge], Time(%), 0.7507756850063366\n[TOTAL_GCS_PS_MarkSweep], Count, 0\n[TOTAL_GC_TIME_PS_MarkSweep], Time(ms), 0\n[TOTAL_GC_TIME_%_PS_MarkSweep], Time(%), 0.0\n[TOTAL_GCs], Count, 529\n[TOTAL_GC_TIME], Time(ms), 859\n[TOTAL_GC_TIME_%], Time(%), 0.7507756850063366\n[CLEANUP], Operations, 1\n[CLEANUP], AverageLatency(us), 2212864.0\n[CLEANUP], MinLatency(us), 2211840\n[CLEANUP], MaxLatency(us), 2213887\n[CLEANUP], 95thPercentileLatency(us), 2213887\n[CLEANUP], 99thPercentileLatency(us), 2213887\n[INSERT], Operations, 500000\n[INSERT], AverageLatency(us), 221.43646\n[INSERT], MinLatency(us), 128\n[INSERT], MaxLatency(us), 101119\n[INSERT], 95thPercentileLatency(us), 288\n[INSERT], 99thPercentileLatency(us), 449\n[INSERT], Return=OK, 500000\n[INFO], Name, ThisBuildHasAName",
				"[OVERALL], RunTime(ms), 785908\n[OVERALL], Throughput(ops/sec), 3817.2406948395997\n[TOTAL_GCS_PS_Scavenge], Count, 1285\n[TOTAL_GC_TIME_PS_Scavenge], Time(ms), 1570\n[TOTAL_GC_TIME_%_PS_Scavenge], Time(%), 0.19976892969660573\n[TOTAL_GCS_PS_MarkSweep], Count, 1\n[TOTAL_GC_TIME_PS_MarkSweep], Time(ms), 22\n[TOTAL_GC_TIME_%_PS_MarkSweep], Time(%), 0.002799309842882373\n[TOTAL_GCs], Count, 1286\n[TOTAL_GC_TIME], Time(ms), 1592\n[TOTAL_GC_TIME_%], Time(%), 0.2025682395394881\n[READ], Operations, 1500751\n[READ], AverageLatency(us), 311.9093960290548\n[READ], MinLatency(us), 179\n[READ], MaxLatency(us), 77823\n[READ], 95thPercentileLatency(us), 381\n[READ], 99thPercentileLatency(us), 424\n[READ], Return=OK, 1500751\n[CLEANUP], Operations, 1\n[CLEANUP], AverageLatency(us), 2212864.0\n[CLEANUP], MinLatency(us), 2211840\n[CLEANUP], MaxLatency(us), 2213887\n[CLEANUP], 95thPercentileLatency(us), 2213887\n[CLEANUP], 99thPercentileLatency(us), 2213887\n[UPDATE], Operations, 1499249\n[UPDATE], AverageLatency(us), 207.18055773257143\n[UPDATE], MinLatency(us), 120\n[UPDATE], MaxLatency(us), 110847\n[UPDATE], 95thPercentileLatency(us), 264\n[UPDATE], 99thPercentileLatency(us), 297\n[UPDATE], Return=OK, 1499249",
				"[OVERALL], RunTime(ms), 785908\n[OVERALL], Throughput(ops/sec), 3817.2406948395997a\n[TOTAL_GCS_PS_Scavenge], Count, 1285\n[TOTAL_GC_TIME_PS_Scavenge], Time(ms), 1570\n[TOTAL_GC_TIME_%_PS_Scavenge], Time(%), 0.19976892969660573\n[TOTAL_GCS_PS_MarkSweep], Count, 1\n[TOTAL_GC_TIME_PS_MarkSweep], Time(ms), 22\n[TOTAL_GC_TIME_%_PS_MarkSweep], Time(%), 0.002799309842882373\n[TOTAL_GCs], Count, 1286\n[TOTAL_GC_TIME], Time(ms), 1592\n[TOTAL_GC_TIME_%], Time(%), 0.2025682395394881\n[READ], Operations, 1500751\n[READ], AverageLatency(us), 311.9093960290548\n[READ], MinLatency(us), 179\n[READ], MaxLatency(us), 77823\n[READ], 95thPercentileLatency(us), 381\n[READ], 99thPercentileLatency(us), 424\n[READ], Return=OK, 1500751\n[CLEANUP], Operations, 1\n[CLEANUP], AverageLatency(us), 2212864.0\n[CLEANUP], MinLatency(us), 2211840\n[CLEANUP], MaxLatency(us), 2213887\n[CLEANUP], 95thPercentileLatency(us), 2213887\n[CLEANUP], 99thPercentileLatency(us), 2213887\n[UPDATE], Operations, 1499249\n[UPDATE], AverageLatency(us), 207.18055773257143\n[UPDATE], MinLatency(us), 120\n[UPDATE], MaxLatency(us), 110847\n[UPDATE], 95thPercentileLatency(us), 264\n[UPDATE], 99thPercentileLatency(us), 297\n[UPDATE], Return=OK, 1499249",
				"[OVERALL], RunTime(ms), 785908\n[OVERALL], Throughput(ops/sec), 3817.2406948395997\n[TOTAL_GCS_PS_Scavenge], Count, 1285\n[TOTAL_GC_TIME_PS_Scavenge], Time(ms), 1570\n[TOTAL_GC_TIME_%_PS_Scavenge], Time(%), 0.19976892969660573\n[TOTAL_GCS_PS_MarkSweep], Count, 1\n[TOTAL_GC_TIME_PS_MarkSweep], Time(ms), 22\n[TOTAL_GC_TIME_%_PS_MarkSweep], Time(%), 0.002799309842882373\n[TOTAL_GCs], Count, 1286\n[TOTAL_GC_TIME], Time(ms), 1592\n[TOTAL_GC_TIME_%], Time(%), 0.2025682395394881\n[READ], Operations, 1500751\n[READ], AverageLatency(us), 311.9093960290548\n[READ], MinLatency(us), 179\n[READ], MaxLatency(us), 77823\n[READ], 95thPercentileLatency(us), 381\n[READ], 99thPercentileLatency(us), 424\n[READ], Return=OK, 1500751\n[CLEANUP], Operations, 1\n[CLEANUP], AverageLatency(us), 2212864.0\n[CLEANUP], MinLatency(us), 2211840\n[CLEANUP], MaxLatency(us), 2213887\n[CLEANUP], 95thPercentileLatency(us), 2213887\n[CLEANUP], 99thPercentileLatency(us), 2213887\n[UPDATE], Operations, 1499249\n[UPDATE], AverageLatency(us), 207.18055773257143\n[UPDATE], MinLatency(us), 120\n[UPDATE], MaxLatency(us), 110847\n[UPDATE], 95thPercentileLatency(us), 264\n[UPDATE], 99thPercentileLatency(us), 297\n[UPDATE], Return=OK, 1499249\n[INFO], Name, ThisBuildHasAName",
				"[OVERALL], RunTime(ms), 785908\n[OVERALL], Throughput(ops/sec), 3817.2406948395997\n[TOTAL_GCS_PS_Scavenge], Count, 1285\n[TOTAL_GC_TIME_PS_Scavenge], Time(ms), 1570\n[TOTAL_GC_TIME_%_PS_Scavenge], Time(%), 0.19976892969660573\n[TOTAL_GCS_PS_MarkSweep], Count, 1\n[TOTAL_GC_TIME_PS_MarkSweep], Time(ms), 22\n[TOTAL_GC_TIME_%_PS_MarkSweep], Time(%), 0.002799309842882373\n[TOTAL_GCs], Count, 1286\n[TOTAL_GC_TIME], Time(ms), 1592\n[TOTAL_GC_TIME_%], Time(%), 0.2025682395394881\n[READ], Operations, 1500751\n[READ], AverageLatency(us), 311.9093960290548\n[READ], MinLatency(us), 179\n[READ], MaxLatency(us), 77823\n[READ], 95thPercentileLatency(us), 381\n[READ], 99thPercentileLatency(us), 424\n[READ], Return=OK, 1500751\n[CLEANUP], Operations, 1\n[CLEANUP], AverageLatency(us), 2212864.0a\n[CLEANUP], MinLatency(us), 2211840\n[CLEANUP], MaxLatency(us), 2213887\n[CLEANUP], 95thPercentileLatency(us), 2213887\n[CLEANUP], 99thPercentileLatency(us), 2213887\n[UPDATE], Operations, 1499249\n[UPDATE], AverageLatency(us), 207.18055773257143\n[UPDATE], MinLatency(us), 120\n[UPDATE], MaxLatency(us), 110847\n[UPDATE], 95thPercentileLatency(us), 264\n[UPDATE], 99thPercentileLatency(us), 297\n[UPDATE], Return=OK, 1499249\n[INFO], Name, ThisBuildHasAName"	
		};
		
		String[] names = {"load", "load_Failure", "load_Name", "load_Name_Failure", "run", "run_Failure", "run_Name", "run_Name_Failure"};
		
		for (int i = 0; i < contents.length; i++) {
			HelperClass.writeTestFile(names[i] + ".ycsb", contents[i]);
		}
	}
	
	@Before
	public void createRightFormatFiles(){
		String[] contents = {
				"Metrik;time_in_ms\n"+
				"Metrik1;10.3\n"+
				"Metrik2;.3",
				
				"Metrik;time_in_ms\n"+
				"Metrik1;10",

				"Metrik;time_in_ms\n"+
				"Metrik1;10.3",

				"Metrik;time_in_ms\n"+
				"Metrik1;10\n"+
				"Metrik2;3,3",

				"Metrik,time_in_ms\n"+
				"Metrik1;as,10.444000\n"+
				"Metrik2,10.444000",

				"Metrik;time_in_ms\n"+
				"Metrik1,as;10.4\n"+
				"Metrik2;-10.4",
				
				"Metrik;time_in_ms\n"+
				"Metrik1,as;10,4\n"+
				"Metrik2;-10,4",

				"Metrik;4.0\n"+
				"Metrik1,as;10.4\n"+
				"Metrik2;-10.4",

				"Metrik;4,0\n"+
				"Metrik1,as;10,4\n"+
				"Metrik2;-10,4",
				
				"A,B\n"+
				"Metrik1,10.4\n"+
				"Metrik2,-10.4",

				"Header\n"+
				"Metrik1,10.4\n"+
				"Metrik2,-10.4",
				
				"H,e,a;d,e,r\n"+
				"Metrik1;10.4\n"+
				"Metrik2;-10.4",

				"123,4.9\n"+
				"Metrik1,10.4\n"+
				"Metrik2,-10.4",

				"Head;er;a\n"+
				"Metrik1;10.4\n"+
				"Metrik2;-10.4",

				"Head\n"+
				"444;10.4\n"+
				"Metrik2;-10.4",

				"124,2911\n"+
				"444,10.4\n"+
				"Metrik2,-10.4",
				
				"Aa,2911\n"+
				"444,10.4\n"+
				"Metrik2,-10.4",
				
				"Header",
				
				"Metrik;time_in_ms\n"+
				"Metrik1;10\n"+
				"Metrik2;-4"
		};
		
		right = contents.length;
		
		for (int i = 1; i <= contents.length; i++) {
			HelperClass.writeTestFile("rightFormat" + i + ".csv", contents[i-1]);
		}
	}
	
	@After
	public void delete(){
		HelperClass.deleteTestFiles();
	}
	
	@Test 
	public void wrongFormat() throws Exception {
		for (int i = 1; i <= wrong; i++) {
			FreeStyleProject project = j.createFreeStyleProject();
			Builder builder = new BenchmarkBuilder(testdir + File.separatorChar + "wrongFormat"+i+".csv");
			project.getBuildersList().add(builder);
			FreeStyleBuild build = project.scheduleBuild2(0).get();
			System.out.println(HelperClass.getLogs(build));
			assertEquals("wrongFormat"+i+".csv",Result.FAILURE,build.getResult());
			j.assertLogContains("Wrong format.", build);
		}
	}
	
	@Test
	public void wrongFileFormat() throws Exception{
		FreeStyleProject project = j.createFreeStyleProject();
		Builder builder = new BenchmarkBuilder(testdir + File.separatorChar + "wrongFormat.wrong");
		project.getBuildersList().add(builder);
		FreeStyleBuild build = project.scheduleBuild2(0).get();
		assertEquals(Result.FAILURE,build.getResult());
		j.assertLogContains(Messages.wrongFormat("csv and ycsb"), build);
		
		project = j.createFreeStyleProject();
		builder = new BenchmarkBuilder(testdir);
		project.getBuildersList().add(builder);
		build = project.scheduleBuild2(0).get();
		assertEquals(Result.FAILURE,build.getResult());
		j.assertLogContains(Messages.read_error(testdir), build);
		
		project = j.createFreeStyleProject();
		builder = new BenchmarkBuilder(testdir+File.separatorChar + "");
		project.getBuildersList().add(builder);
		build = project.scheduleBuild2(0).get();
		assertEquals(Result.FAILURE,build.getResult());
		j.assertLogContains(Messages.read_error(testdir+File.separatorChar + ""), build);
		
		project = j.createFreeStyleProject();
		builder = new BenchmarkBuilder(testdir+File.separatorChar + "doesNotEx.csv");
		project.getBuildersList().add(builder);
		build = project.scheduleBuild2(0).get();
		assertEquals(Result.FAILURE,build.getResult());
		j.assertLogContains(Messages.read_error(testdir+File.separatorChar + "doesNotEx.csv"), build);
		
		project = j.createFreeStyleProject();
		File f = new File(testdir+File.separatorChar + "testBuild5.csv");
		HelperClass.writeFile(f.getAbsolutePath(), null);
		builder = new BenchmarkBuilder(f.getAbsolutePath());
		project.getBuildersList().add(builder);
		build = project.scheduleBuild2(0).get();
		assertEquals(Result.FAILURE,build.getResult());
		j.assertLogContains(Messages.read_error(f.getAbsolutePath()), build);
	}

	@Test 
	public void rightFormat() throws Exception {
		for (int i = 1; i <= right; i++) {
			FreeStyleProject project = j.createFreeStyleProject();
			Builder builder = new BenchmarkBuilder(testdir + File.separatorChar + "rightFormat"+i+".csv");
			project.getBuildersList().add(builder);
			FreeStyleBuild build = project.scheduleBuild2(0).get();//j.buildAndAssertSuccess(project);
			assertEquals(HelperClass.getLogs(build),Result.SUCCESS,build.getResult());
			j.assertLogContains(Messages.read_succes(), build);
			// Assert that the console log contains the output we expect
			//j.assertLogContains("", build);
		}
	}
	
	@Test
	public void readYCSB() throws Exception {
		String[] files = {"run","load","run_Name","load_Name"};
		for (String f : files) {
			FreeStyleProject project = j.createFreeStyleProject();
			Builder builder = new BenchmarkBuilder(testdir + File.separatorChar + f + ".ycsb");
			project.getBuildersList().add(builder);
			FreeStyleBuild build = project.scheduleBuild2(0).get();//j.buildAndAssertSuccess(project);
			assertEquals(HelperClass.getLogs(build),Result.SUCCESS,build.getResult());
			j.assertLogContains(Messages.read_succes(), build);
			
			BenchmarkAction a = build.getActions(BenchmarkAction.class).get(0);
			if(f.contains("Name")) {
				assertEquals(a.getName(),"ThisBuildHasAName");
			}else {
				assertNull(a.getName());
			}
			
		}
	}
	
	@Test
	public void readYCSBFailure() throws Exception {
		String[] files = {"run_Failure","load_Failure","run_Name_Failure","load_Name_Failure"};
		for (String f : files) {
			FreeStyleProject project = j.createFreeStyleProject();
			Builder builder = new BenchmarkBuilder(testdir + File.separatorChar + f + ".ycsb");
			project.getBuildersList().add(builder);
			FreeStyleBuild build = project.scheduleBuild2(0).get();//j.buildAndAssertSuccess(project);
			assertEquals(HelperClass.getLogs(build),Result.FAILURE,build.getResult());
			j.assertLogContains("Wrong format", build);
			
			BenchmarkAction a = build.getActions(BenchmarkAction.class).get(0);
			if(f.contains("Name")) {
				assertEquals(a.getName(),"ThisBuildHasAName");
			}else {
				assertNull(a.getName());
			}
		}
	}

	@Test 
	public void succesBuild() throws Exception {
		FreeStyleProject project = j.createFreeStyleProject();
		String f = testdir + File.separatorChar + "testBuild1.csv";
		BenchmarkBuilder builder = new BenchmarkBuilder(f);
		HelperClass.writeFile(f, "Metrik1;10\nMetrik2;20");
		project.getBuildersList().add(builder);
		FreeStyleBuild build = project.scheduleBuild2(0).get();//j.buildAndAssertSuccess(project);
		assertEquals(HelperClass.getLogs(build),Result.SUCCESS,build.getResult());
		System.out.println(build.getWorkspace());
		System.out.println(build.getWorkspace().getRemote());
		System.out.println(File.separatorChar + "config"+builder.getID()+".config");
		BenchmarkConfiguration conf = BenchmarkConfiguration.getConfig(
				build.getWorkspace().getRemote()+File.separatorChar + "config"+builder.getID()+".config");
		conf.change("Metrik1", new ConfigEntry(null,10d,null, 20d,null,1));
		conf.change("Metrik2", new ConfigEntry(null,40d,null, 20d,null,1));
		
		j.assertLogContains(Messages.read_succes(), build);
		
		HelperClass.writeFile(f, "Metrik1;21\nMetrik2;20");
		build = project.scheduleBuild2(0).get();
		j.assertLogContains(Messages.valueToHigh("Metrik1", 21, 20,""), build);
		assertEquals("ON FAILURE mvn clean package",Result.FAILURE,build.getResult());
		
		HelperClass.writeFile(f, "Metrik1;19\nMetrik2;20");
		build = project.scheduleBuild2(0).get();
		j.assertLogContains(Messages.higherThanLastTime("Metrik1", 90, 10), build);
		assertEquals(Result.FAILURE,build.getResult());
		
		HelperClass.writeFile(f, "Metrik1;18\nMetrik2;21");
		build = project.scheduleBuild2(0).get();
		//j.assertLogContains(Messages.higherThanLastTime("Metrik1", 80, 10), build);
		j.assertLogContains(Messages.valueToHigh("Metrik2", 21, 20,""), build);
		assertEquals(Result.FAILURE,build.getResult());
		
		HelperClass.writeFile(f, "Metrik1;5\nMetrik2;10");
		build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.SUCCESS,build.getResult());
		
		HelperClass.writeFile(f, "Metrik1;10\nMetrik2;15");
		build = project.scheduleBuild2(0).get();
		j.assertLogContains(Messages.higherThanLastTime("Metrik1", 100, 10), build);
		j.assertLogContains(Messages.higherThanLastTime("Metrik2", 50, 40), build);
		assertEquals(Result.FAILURE,build.getResult());
		
		conf.change("Metrik1", new ConfigEntry(null,null,null, 30d,null,1));
		conf.change("Metrik2", new ConfigEntry(null,50d, null,null,null,1));
		
		HelperClass.writeFile(f, "Metrik1;25\nMetrik2;16");
		build = project.scheduleBuild2(0).get();
		assertEquals(Result.FAILURE,build.getResult());
		
		// Assert that the console log contains the output we expect
		//j.assertLogContains("", build);
	}
	
	@Test
	public void getWorkspaceTest() throws Exception{
		FreeStyleProject project = j.createFreeStyleProject();
		String f = testdir + File.separatorChar + "testBuild7.csv";
		BenchmarkBuilder builder = new BenchmarkBuilder(f);
		HelperClass.writeFile(f, "Metrik1;10\nMetrik2;20");
		project.getBuildersList().add(builder);
		FreeStyleBuild build = project.scheduleBuild2(0).get();
		assertNotNull(builder.getConfig(build));
		
		j.createFreeStyleProject();
		String f2 = testdir + File.separatorChar + "testBuild7.csv";
		BenchmarkBuilder builder2 = new BenchmarkBuilder(f2);
		assertNotNull(builder2.getConfig(build));
	}
	
	@Test
	public void zeroTest() throws Exception{
		FreeStyleProject project = j.createFreeStyleProject();
		String f = testdir + File.separatorChar + "testBuild6.csv";
		BenchmarkBuilder builder = new BenchmarkBuilder(f);
		HelperClass.writeFile(f, "Metrik1;0\nMetrik2;0");
		project.getBuildersList().add(builder);
		FreeStyleBuild build = project.scheduleBuild2(0).get();//j.buildAndAssertSuccess(project);
		assertEquals(HelperClass.getLogs(build),Result.SUCCESS,build.getResult());
		
		BenchmarkConfiguration conf = BenchmarkConfiguration.getConfig(build.getWorkspace().getRemote()+File.separatorChar + "config"+builder.getID()+".config");
		conf.change("Metrik1", new ConfigEntry(null,10d,null, null, null,1));
		conf.change("Metrik2", new ConfigEntry(10d,null,null, null, null,1));
		
		HelperClass.writeFile(f, "Metrik1;-10\nMetrik2;20");
		build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.SUCCESS,build.getResult());
		
		conf.change("Metrik1", new ConfigEntry(null,null,null, null, null,1));
		conf.change("Metrik2", new ConfigEntry(null,null,null, null, null,1));
		
		HelperClass.writeFile(f, "Metrik1;0\nMetrik2;0");
		build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.SUCCESS,build.getResult());
		
		conf.change("Metrik1", new ConfigEntry(null,10d,null, null, null,1));
		
		HelperClass.writeFile(f, "Metrik1;1\nMetrik2;0");
		build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.FAILURE,build.getResult());
		j.assertLogContains(Messages.higherThanExpected("Metrik1", 10d), build);
		
		conf.change("Metrik1", new ConfigEntry(null,null,null, null, null,1));
		conf.change("Metrik2", new ConfigEntry(null,null,null, null, null,1));
		
		HelperClass.writeFile(f, "Metrik1;0\nMetrik2;0");
		build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.SUCCESS,build.getResult());
		
		conf.change("Metrik2", new ConfigEntry(10d,null,null, null, null,1));
		
		HelperClass.writeFile(f, "Metrik1;-10\nMetrik2;-20");
		build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.FAILURE,build.getResult());
		j.assertLogContains(Messages.lowerThanExpected("Metrik2", 10d), build);
		
		
	}
	
	@Test
	public void addBuildStepLater() throws Exception{
		FreeStyleProject project = j.createFreeStyleProject();
		String f = testdir + File.separatorChar + "testBuild2.csv";
		BenchmarkBuilder builder = new BenchmarkBuilder(f);
		HelperClass.writeFile(f, "Metrik1;10\nMetrik2;20");
		project.getBuildersList().add(builder);
		FreeStyleBuild build = project.scheduleBuild2(0).get();//j.buildAndAssertSuccess(project);
		assertEquals(HelperClass.getLogs(build),Result.SUCCESS,build.getResult());
		
		
		HelperClass.writeFile(f, "Metrik1;11\nMetrik2;21");
		
		String f1 = testdir + File.separatorChar + "testBuild2.csv";
		HelperClass.writeFile(f1, "Metrik1;90\nMetrik2;80");
		BenchmarkBuilder builder1 = new BenchmarkBuilder(f1);
		project.getBuildersList().add(builder1);
		FreeStyleBuild build2 = project.scheduleBuild2(0).get();//j.buildAndAssertSuccess(project);
		assertEquals(Result.SUCCESS,build2.getResult());
	}
	
	@Test
	public void equalsAndHashTest(){
		String a = "a";
		BenchmarkBuilder b1 = new BenchmarkBuilder(a);
		BenchmarkBuilder b2 = new BenchmarkBuilder("b");
		BenchmarkBuilder b3 = new BenchmarkBuilder("a");
		assertFalse(b1.equals(b2));
		assertFalse(b1.equals(b3));
		assertFalse(b1.equals("Builder"));
		assertTrue(b1.equals(b1));
		assertEquals(a.hashCode()+b1.getID(), b1.hashCode());
	}
	
	@Test public void getProjectActions() throws Exception{
		FreeStyleProject project = j.createFreeStyleProject();
		String a = "a";
		BenchmarkBuilder b1 = new BenchmarkBuilder(a);
		assertFalse(b1.getProjectActions(project).isEmpty());
		assertTrue(b1.getProjectActions(j.createFreeStyleProject()).isEmpty());
	}
	
	@Test 
	public void lowerBound() throws Exception {
		FreeStyleProject project = j.createFreeStyleProject();
		String f = testdir + File.separatorChar + "testBuild4.csv";
		BenchmarkBuilder builder = new BenchmarkBuilder(f);
		HelperClass.writeFile(f, "Metrik1;10\nMetrik2;20");
		project.getBuildersList().add(builder);
		FreeStyleBuild build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.SUCCESS,build.getResult());
		BenchmarkConfiguration conf = BenchmarkConfiguration.getConfig(
				build.getWorkspace().getRemote()+File.separatorChar + "config"+builder.getID()+".config");
		conf.change("Metrik1", new ConfigEntry(null,null,10d,null,null,1));
		conf.change("Metrik2", new ConfigEntry(null,null,40d,null,null,1));
		
		HelperClass.writeFile(f, "Metrik1;10.000001\nMetrik2;55");
		build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.SUCCESS,build.getResult());
		
		conf.change("Metrik1", new ConfigEntry(null,null,10d,null,"ms",1));
		conf.change("Metrik2", new ConfigEntry(null,null,40d,null,"ns",1));
		HelperClass.writeFile(f, "Metrik1;9.99999\nMetrik2;57");
		build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.FAILURE,build.getResult());
		j.assertLogContains(Messages.valueToLow("Metrik1", 9.99999, 10d, " ms"), build);
		
		HelperClass.writeFile(f, "Metrik1;10\nMetrik2;16");
		build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.FAILURE,build.getResult());
		j.assertLogContains(Messages.valueToLow("Metrik2", 16d, 40d, " ns"), build);
		
		HelperClass.writeFile(f, "Metrik1;20\nMetrik2;60");
		conf.change("Metrik1", new ConfigEntry(0d,null,10d,null,"ms",1));
		conf.change("Metrik2", new ConfigEntry(0d,null,40d,null,"ns",1));
		build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.SUCCESS,build.getResult());
		
		HelperClass.writeFile(f, "Metrik1;120\nMetrik2;100");
		conf.change("Metrik1", new ConfigEntry(0d,null,10d,null,"ms",1));
		conf.change("Metrik2", new ConfigEntry(0d,null,40d,null,"ns",1));
		build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.SUCCESS,build.getResult());
		
		HelperClass.writeFile(f, "Metrik1;40\nMetrik2;50");
		build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.FAILURE,build.getResult());
		j.assertLogContains(Messages.lowerThanLastTime("Metrik2", -50d, 0), build);
		
		HelperClass.writeFile(f, "Metrik1;30\nMetrik2;50");
		build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.FAILURE,build.getResult());
		j.assertLogContains(Messages.lowerThanLastTime("Metrik2", -50d, 0), build);
		j.assertLogContains(Messages.lowerThanLastTime("Metrik1", -75d, 0), build);
		
		HelperClass.writeFile(f, "Metrik1;5\nMetrik2;45");
		build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.FAILURE,build.getResult());
		j.assertLogContains(Messages.lowerThanLastTime("Metrik2", -55d, 0), build);
		j.assertLogContains(Messages.lowerThanLastTime("Metrik1", -95.83d, 0), build);
		j.assertLogContains(Messages.valueToLow("Metrik1", 5d, 10d, " ms"), build);
		
	}
	
	@Test
	public void desriptorTest() throws Exception{
		
		DescriptorImpl descriptor = new BenchmarkBuilder.DescriptorImpl();
		assertTrue(descriptor.isApplicable(null));
		
		FormValidation answer = descriptor.doCheckFilepath(testdir);
		assertEquals(Messages.fileIsDir().replace("'", "&#039;"), answer.getMessage());
		
		answer = descriptor.doCheckFilepath("superFile.supersupersuper");
		assertEquals(Messages.wrongFormat(BenchmarkResults.getFormats()), answer.getMessage());
		
		answer = descriptor.doCheckFilepath("superFile.csv");
		assertEquals(Messages.noFile(), answer.getMessage());
		
		answer = descriptor.doCheckFilepath(testdir+File.separatorChar+".csv");
		assertEquals(testdir+File.separatorChar+".csv",Messages.noFileName(), answer.getMessage());
		
		answer = descriptor.doCheckFilepath(testdir + File.separatorChar + "validName.csv");
		assertEquals(Messages.fileIsNotPresent(), answer.getMessage());
		
		answer = descriptor.doCheckFilepath("ftp://ftp.ebi.ac.uk/pub/databases/chembl/VEHICLe/VEHICLe.csv");
		assertTrue(answer.getMessage().contains("Line 2: Wrong format."));
		
		answer = descriptor.doCheckFilepath("https://raw.githubusercontent.com/openmundi/world.csv/master/countries(249)_alpha3.csv");
		assertTrue(answer.getMessage().contains("Line 2: Wrong format."));
		
		answer = descriptor.doCheckFilepath("http://wiki.splunk.com/Http_status.csv");
		assertTrue(answer.getMessage().contains("Line 2: Wrong format."));
		
		String path = "http://noUrlThatReallyExsistIHope1238217312.de/results.csv";
		answer = descriptor.doCheckFilepath(path);
		assertEquals(Messages.read_error(path).replace("'", "&#039;"), answer.getMessage());
		
		path = testdir+File.separatorChar+"rightFormat1.csv";
		answer = descriptor.doCheckFilepath(path);
		System.out.println(answer.getMessage());
				
		String[] endings = BenchmarkResults.endings;
		String[] newArray = {"csv","wrong"};
		BenchmarkResults.endings = newArray;
		
		answer = descriptor.doCheckFilepath(testdir + File.separatorChar + "wrongFormat.wrong");
		assertEquals(Messages.read_error(testdir + File.separatorChar + "wrongFormat.wrong").replace("'", "&#039;"), answer.getMessage());
		
		BenchmarkResults.endings = endings;
	}
	
}