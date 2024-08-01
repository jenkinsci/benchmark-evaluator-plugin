package io.jenkins.plugins.benchmark;

import static org.junit.Assert.*;
import java.io.File;
import java.util.Collection;
import java.util.Map.Entry;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import io.jenkins.plugins.benchmark.configuration.BenchmarkConfiguration;
import io.jenkins.plugins.benchmark.configuration.ConfigEntry;

public class BenchmarkConfigActionTest {
	
	@Rule public JenkinsRule j = new JenkinsRule(); 

	private String testdir;
	
	@Before
	public void createTestDir(){
		testdir = HelperClass.createTestDir();
	}
	
	@After
	public void delete(){
		HelperClass.deleteTestFiles( testdir );
	}
 

	@Test
	public void configTest() throws Exception {
		
		FreeStyleProject project = j.createFreeStyleProject();
		String file = "configTest1.csv";
		BenchmarkBuilder builder = new BenchmarkBuilder(testdir + File.separatorChar+file);
		project.getBuildersList().add(builder);

		BenchmarkConfiguration conf = BenchmarkConfiguration.getConfig(getCurrentWorkspace(project)+"config"+builder.getID()+".config");
		assertTrue(conf.getEntrys().isEmpty());
		
		Collection<? extends Action> actions = builder.getProjectActions(project);
		actions.removeIf(x->!(x instanceof BenchmarkConfigAction));
		assertEquals(1, actions.size());
		BenchmarkConfigAction configAction = (BenchmarkConfigAction) project.getProminentActions().stream().filter(x->x instanceof BenchmarkConfigAction).findFirst().get();
		
		assertTrue(configAction.getConfiguration().isEmpty());
		
		BenchmarkConfigAction tProject = new BenchmarkConfigAction(conf, j.createFreeStyleProject());
		
		assertEquals("benchmarkConfig_1",configAction.getUrlName());
		assertEquals(null,tProject.getUrlName());
		assertEquals("Benchmark Configuration 1",configAction.getDisplayName());
		assertEquals(null,tProject.getDisplayName());
		assertNotNull(configAction.getIconFileName());
		assertNull(tProject.getIconFileName());
		
		//Create Data
		HelperClass.writeFile(testdir + File.separatorChar+file, "Metrik1;25\nMetrik2;16");

		FreeStyleBuild build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.SUCCESS,build.getResult());
		
		boolean metrik1 = false;
		boolean metrik2 = false;
		boolean other = false;
		for(Entry<String, ConfigEntry> entry : configAction.getConfiguration()){
			if(entry.getKey().equals("Metrik1")){
				assertFalse(metrik1);
				metrik1 = true;
			}else if(entry.getKey().equals("Metrik2")){
				assertFalse(metrik2);
				metrik2 = true;
			}else{
				other = true;
				break;
			}
			assertEquals(1, entry.getValue().getBuildNr());
			assertNull(entry.getValue().getMaxPercent());
			assertNull(entry.getValue().getMaxValue());
		}
		assertFalse(other);
		
		configAction.setMetricMaxValue("Metrik3", 100d);
		assertEquals(2, configAction.getConfiguration().size());
		for(Entry<String, ConfigEntry> entry : configAction.getConfiguration()){
			assertEquals(1, entry.getValue().getBuildNr());
			assertNull(entry.getValue().getMaxPercent());
			assertNull(entry.getValue().getMaxValue());
		}
		
		configAction.setMetricMaxValue("Metrik1", 100d);
		configAction.setMetricMaxValue("Metrik1", -11100d);
		assertEquals(2, configAction.getConfiguration().size());
		for(Entry<String, ConfigEntry> entry : configAction.getConfiguration()){
			assertEquals(1, entry.getValue().getBuildNr());
			assertNull(entry.getValue().getMaxPercent());
			if(entry.getKey().equals("Metrik1"))
				assertEquals(-11100d, entry.getValue().getMaxValue(),0.000001);
			else
				assertNull(entry.getValue().getMaxValue());
		}
		
		configAction.setMetricMaxPercent("Metrik1", 10d);
		configAction.setMetricMaxPercent("Metrik1", -11d);
		configAction.setMetricMaxPercent("Metrik3", 1d);
		assertFalse(configAction.createMetric("Metrik1"));
		assertEquals(2, configAction.getConfiguration().size());
		for(Entry<String, ConfigEntry> entry : configAction.getConfiguration()){
			assertEquals(1, entry.getValue().getBuildNr());
			if(entry.getKey().equals("Metrik1"))
				assertEquals(-11d, entry.getValue().getMaxPercent(),0.000001);
			else
				assertNull(entry.getValue().getMaxPercent());
			if(entry.getKey().equals("Metrik1"))
				assertEquals(-11100d, entry.getValue().getMaxValue(),0.000001);
			else
				assertNull(entry.getValue().getMaxValue());
		}
		
		assertTrue(configAction.createMetric("Metrik3"));
		assertEquals(3, configAction.getConfiguration().size());
		boolean metrik3 = false;
		for(Entry<String, ConfigEntry> entry : configAction.getConfiguration()){
			if(entry.getKey().equals("Metrik3")){
				assertEquals(-1, entry.getValue().getBuildNr());
				metrik3 = true;
			}else{
				assertEquals(1, entry.getValue().getBuildNr());
			}
			if(entry.getKey().equals("Metrik1")){
				assertEquals(-11d, entry.getValue().getMaxPercent(),0.000001);
				assertEquals(-11100d, entry.getValue().getMaxValue(),0.000001);
			}else{
				assertNull(entry.getValue().getMaxPercent());
				assertNull(entry.getValue().getMaxValue());
			}
		}
		
		assertTrue(metrik3);
	}
	
	@Test
	public void configTestWithMin() throws Exception {
		
		FreeStyleProject project = j.createFreeStyleProject();
		String file = "configTest2.csv";
		BenchmarkBuilder builder = new BenchmarkBuilder(testdir + File.separatorChar+file);
		project.getBuildersList().add(builder);

		BenchmarkConfiguration conf = BenchmarkConfiguration.getConfig(getCurrentWorkspace(project)+"config"+builder.getID()+".config");
		assertTrue(conf.getEntrys().isEmpty());
		
		Collection<? extends Action> actions = builder.getProjectActions(project);
		actions.removeIf(x->!(x instanceof BenchmarkConfigAction));
		assertEquals(1, actions.size());
		BenchmarkConfigAction configAction = (BenchmarkConfigAction) project.getProminentActions().stream().filter(x->x instanceof BenchmarkConfigAction).findFirst().get();
		
		assertTrue(configAction.getConfiguration().isEmpty());
		
		assertFalse(configAction.deleteMetric("metrik1"));
		assertTrue(configAction.createMetric("metrik1"));
		assertFalse(configAction.createMetric("metrik1"));
		assertTrue(configAction.deleteMetric("metrik1"));
		
		HelperClass.writeFile(testdir + File.separatorChar+file, "metrik1;25\nmetrik2;16");

		FreeStyleBuild build = project.scheduleBuild2(0).get();
		assertEquals(HelperClass.getLogs(build),Result.SUCCESS,build.getResult());
		assertFalse(configAction.deleteMetric("metrik1"));
		assertFalse(configAction.createMetric("metrik1"));
		
		assertTrue(configAction.createMetric("metrik3"));
		
		configAction.setMetricMinValue("metrik3", 10d);
		ConfigEntry metric = conf.get("metrik3");
		assertEquals(10d, metric.getMinValue(),0.000001d);
		assertNull(metric.getMaxValue());
		assertNull(metric.getMaxPercent());
		assertNull(metric.getMinPercent());
		assertNull(metric.getUnit());
		
		configAction.setMetricMaxValue("metrik3", 9d);
		metric = conf.get("metrik3");
		assertEquals(10d, metric.getMinValue(),0.000001d);
		assertNull(metric.getMaxValue());
		assertNull(metric.getMaxPercent());
		assertNull(metric.getMinPercent());
		assertNull(metric.getUnit());
		
		configAction.setMetricMaxValue("metrik3", 11d);
		metric = conf.get("metrik3");
		assertEquals(10d, metric.getMinValue(),0.000001d);
		assertEquals(11d, metric.getMaxValue(),0.000001d);
		assertNull(metric.getMaxPercent());
		assertNull(metric.getMinPercent());
		assertNull(metric.getUnit());
		
		configAction.setMetricMinValue("metrik3", 13d);
		metric = conf.get("metrik3");
		assertEquals(10d, metric.getMinValue(),0.000001d);
		assertEquals(11d, metric.getMaxValue(),0.000001d);
		assertNull(metric.getMaxPercent());
		assertNull(metric.getMinPercent());
		assertNull(metric.getUnit());
		
		configAction.setMetricMinValue("metrik3", 8d);
		metric = conf.get("metrik3");
		assertEquals(8d, metric.getMinValue(),0.000001d);
		assertEquals(11d, metric.getMaxValue(),0.000001d);
		assertNull(metric.getMaxPercent());
		assertNull(metric.getMinPercent());
		assertNull(metric.getUnit());
		
		configAction.setMetricUnit("metrik3", null);
		metric = conf.get("metrik3");
		assertEquals(8d, metric.getMinValue(),0.000001d);
		assertEquals(11d, metric.getMaxValue(),0.000001d);
		assertNull(metric.getMaxPercent());
		assertNull(metric.getMinPercent());
		assertNull(metric.getUnit());
		
		configAction.setMetricUnit("metrik3", "<script>console.log('muhahaha');</script>");
		metric = conf.get("metrik3");
		assertEquals(8d, metric.getMinValue(),0.000001d);
		assertEquals(11d, metric.getMaxValue(),0.000001d);
		assertNull(metric.getMaxPercent());
		assertNull(metric.getMinPercent());
		assertNull(metric.getUnit());
		
		configAction.setMetricUnit("metrik3", "nanoseconds");
		metric = conf.get("metrik3");
		assertEquals(8d, metric.getMinValue(),0.000001d);
		assertEquals(11d, metric.getMaxValue(),0.000001d);
		assertNull(metric.getMaxPercent());
		assertNull(metric.getMinPercent());
		assertEquals("nanoseconds",metric.getUnit());
		
		configAction.setMetricUnit("metrik3", "doener");
		metric = conf.get("metrik3");
		assertEquals(8d, metric.getMinValue(),0.000001d);
		assertEquals(11d, metric.getMaxValue(),0.000001d);
		assertNull(metric.getMaxPercent());
		assertNull(metric.getMinPercent());
		assertEquals("doener",metric.getUnit());
		
		configAction.setMetricMaxPercent("metrik3", -5d);
		metric = conf.get("metrik3");
		assertEquals(8d, metric.getMinValue(),0.000001d);
		assertEquals(11d, metric.getMaxValue(),0.000001d);
		assertEquals(-5d,metric.getMaxPercent(),0.000001);
		assertNull(metric.getMinPercent());
		assertEquals("doener",metric.getUnit());
		
		configAction.setMetricMaxPercent("metrik3", -1000d);
		metric = conf.get("metrik3");
		assertEquals(8d, metric.getMinValue(),0.000001d);
		assertEquals(11d, metric.getMaxValue(),0.000001d);
		assertEquals(-1000d,metric.getMaxPercent(),0.000001);
		assertNull(metric.getMinPercent());
		assertEquals("doener",metric.getUnit());
		
		configAction.setMetricMinPercent("metrik3", 1000d);
		metric = conf.get("metrik3");
		assertEquals(8d, metric.getMinValue(),0.000001d);
		assertEquals(11d, metric.getMaxValue(),0.000001d);
		assertEquals(-1000d,metric.getMaxPercent(),0.000001);
		assertNull(metric.getMinPercent());
		assertEquals("doener",metric.getUnit());
		
		configAction.setMetricMinPercent("metrik3", -1001d);
		metric = conf.get("metrik3");
		assertEquals(8d, metric.getMinValue(),0.000001d);
		assertEquals(11d, metric.getMaxValue(),0.000001d);
		assertEquals(-1000d,metric.getMaxPercent(),0.000001);
		assertEquals(-1001d,metric.getMinPercent(),0.000001);
		assertEquals("doener",metric.getUnit());
		
		
		configAction.setMetricMinValue("metrik3", 12d);
		metric = conf.get("metrik3");
		assertEquals(8d, metric.getMinValue(),0.000001d);
		assertEquals(11d, metric.getMaxValue(),0.000001d);
		assertEquals(-1000d,metric.getMaxPercent(),0.000001);
		assertEquals(-1001d,metric.getMinPercent(),0.000001);
		assertEquals("doener",metric.getUnit());
		
		configAction.setMetricMinPercent("metrik3", null);
		configAction.setMetricMaxPercent("metrik3", null);
		configAction.setMetricMinValue("metrik3", null);
		configAction.setMetricMaxValue("metrik3", null);
		configAction.setMetricUnit("metrik3", null);
		metric = conf.get("metrik3");
		assertNull(metric.getMinValue());
		assertNull(metric.getMaxValue());
		assertNull(metric.getMaxPercent());
		assertNull(metric.getMinPercent());
		assertNull(metric.getUnit());
		
		configAction.setMetricMinPercent("metrik3", 1d);
		metric = conf.get("metrik3");
		assertNull(metric.getMinValue());
		assertNull(metric.getMaxValue());
		assertNull(metric.getMaxPercent());
		assertEquals(1d,metric.getMinPercent(),0.000001);
		assertNull(metric.getUnit());
		
		configAction.setMetricMaxPercent("metrik3", 0d);
		metric = conf.get("metrik3");
		assertNull(metric.getMinValue());
		assertNull(metric.getMaxValue());
		assertNull(metric.getMaxPercent());
		assertEquals(1d,metric.getMinPercent(),0.000001);
		assertNull(metric.getUnit());
		
		configAction.setMetricMaxPercent("metrik3", 2d);
		metric = conf.get("metrik3");
		assertNull(metric.getMinValue());
		assertNull(metric.getMaxValue());
		assertEquals(2d,metric.getMaxPercent(),0.000001);
		assertEquals(1d,metric.getMinPercent(),0.000001);
		assertNull(metric.getUnit());
		
		int size = conf.getSize();
		
		configAction.setMetricMinPercent("a", -1001d);
		configAction.setMetricMaxPercent("b", 1d);
		configAction.setMetricMinValue("c", 50d);
		configAction.setMetricMaxValue("d", 51d);
		configAction.setMetricUnit("e", "unit");
		
		assertEquals(size, conf.getSize());
	}
	
	private String getCurrentWorkspace(AbstractProject<?, ?> project){
		return project.getRootDir().getAbsolutePath()+File.separator;
	}
	
}
