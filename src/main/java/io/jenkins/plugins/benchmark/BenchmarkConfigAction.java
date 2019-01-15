package io.jenkins.plugins.benchmark;

import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import java.util.Set;
import hudson.model.AbstractProject;
import hudson.model.ProminentProjectAction;
import io.jenkins.plugins.benchmark.configuration.BenchmarkConfiguration;
import io.jenkins.plugins.benchmark.configuration.ConfigEntry;

public class BenchmarkConfigAction implements ProminentProjectAction{


	final private BenchmarkConfiguration configuration;
	final private AbstractProject<?, ?> project;

	public BenchmarkConfigAction(BenchmarkConfiguration config, AbstractProject<?, ?> project) {
		this.configuration = config;
		this.project = project;
	}

	public Set<Entry<String, ConfigEntry>> getConfiguration() {
		return configuration.getEntrys();
	}
	
	@JavaScriptMethod
	public void setMetricMinPercent(String metric,Double minPercent){
		ConfigEntry c = configuration.get(metric);
		if(c==null || minPercent!=null && c.getMaxPercent() != null && c.getMaxPercent()<minPercent) return;
		configuration.change(metric, new ConfigEntry(minPercent, c.getMaxPercent(), c.getMinValue(), c.getMaxValue(), c.getUnit(),  c.getBuildNr()));
	}
	
	@JavaScriptMethod
	public void setMetricMaxPercent(String metric,Double maxPercent){
		ConfigEntry c = configuration.get(metric);
		if(c==null || maxPercent!=null && c.getMinPercent() != null && c.getMinPercent()>maxPercent) return;
		configuration.change(metric, new ConfigEntry(c.getMinPercent(), maxPercent, c.getMinValue(), c.getMaxValue(), c.getUnit(),  c.getBuildNr()));
	}

	@JavaScriptMethod
	public void setMetricMinValue(String metric,Double minValue){
		ConfigEntry c = configuration.get(metric);
		if(c==null || minValue!=null && c.getMaxValue() != null && c.getMaxValue()<minValue) return;
		configuration.change(metric, new ConfigEntry(c.getMinPercent(), c.getMaxPercent(), minValue, c.getMaxValue(), c.getUnit(),  c.getBuildNr()));
	}
	
	@JavaScriptMethod
	public void setMetricMaxValue(String metric,Double maxValue){
		ConfigEntry c = configuration.get(metric);
		if(c==null || maxValue!=null && c.getMinValue() != null && c.getMinValue()>maxValue) return;
		configuration.change(metric, new ConfigEntry(c.getMinPercent(), c.getMaxPercent(), c.getMinValue(), maxValue, c.getUnit(),  c.getBuildNr()));
	}
	
	@JavaScriptMethod
	public void setMetricUnit(String metric,String unit){
		if(unit!=null && unit.toLowerCase().contains("<script")) return;
		ConfigEntry c = configuration.get(metric);
		if(c!=null) configuration.change(metric, new ConfigEntry(c.getMinPercent(), c.getMaxPercent(), c.getMinValue(), c.getMaxValue(), unit,  c.getBuildNr()));
	}

	@JavaScriptMethod
	public boolean createMetric(String metric){
		return configuration.add(metric, new ConfigEntry(null, null, null, null, null, -1));
	}
	
	@JavaScriptMethod
	public boolean deleteMetric(String metric){
		return configuration.delete(metric);
	}

	@Override
	public String getIconFileName() {
		int i = getNumber();
		if(i==-1)
			return null;
		else
			return "/plugin/benchmark-evaluator/images/256x256/configuration.png";
	}

	@Override
	public String getDisplayName() {
		int i = getNumber();
		if(i==-1)
			return null;
		else			
			return Messages.benchmarkConfig()+" "+i;
	}

	@Override
	public String getUrlName() {
		int i = getNumber();
		if(i==-1)
			return null;
		else			
			return "benchmarkConfig_"+i;
	}

	private int getNumber(){
		int i = project.getProminentActions().stream().filter(x->x instanceof BenchmarkConfigAction)
				.collect(Collectors.toList()).indexOf(this);
		return (i==-1)?-1:i+1;
	}

}
