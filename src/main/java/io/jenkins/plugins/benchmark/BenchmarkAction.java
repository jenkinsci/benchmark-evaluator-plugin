package io.jenkins.plugins.benchmark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import hudson.model.Build;
import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.plugins.benchmark.configuration.BenchmarkConfiguration;
import io.jenkins.plugins.benchmark.configuration.ConfigEntry;
import io.jenkins.plugins.benchmark.data.ReadResult;
import jenkins.model.RunAction2;

public class BenchmarkAction implements RunAction2{

	final private ReadResult results;
	private transient Run<?, ?> run;
	private BenchmarkBuilder benchmarkBuilder;
	private final boolean succes;
	private final BenchmarkConfiguration benchmarkConfig;

	public BenchmarkAction(ReadResult m, BenchmarkBuilder benchmarkBuilder
			,boolean succes, BenchmarkConfiguration conf) {
		this.results = m;
		this.benchmarkBuilder = benchmarkBuilder;
		this.succes = succes;
		this.benchmarkConfig = conf;
	}

	public Map<String, Double> getResults() {
		return results != null ? results.getMessurements() : null;
	}
	
	static private class Pair{
		public final String metric;
		public final int buildnr;
		public Pair(String metric, int buildnr) {
			this.metric = metric;
			this.buildnr = buildnr;
		}
	}

	public String getFirstResultsAsJson() {
		if(this.getResults() == null) return "undefined";
		List<Build<?, ?>> allBuilds = new ArrayList<Build<?, ?>>();
		Object o = run.getPreviousSuccessfulBuild();
		while (o!= null && o instanceof Build){
			allBuilds.add((Build<?, ?>)o);
			o = ((Build)o).getPreviousSuccessfulBuild();
		}
		if(!allBuilds.isEmpty()){
			Map<String, Double> firstresults = new HashMap<String, Double>();
			List<Pair> list = results.getMessurements().entrySet().parallelStream()
			.map(x->{
				ConfigEntry y = this.getConfig().get(x.getKey());
				return (y!=null)?new Pair(x.getKey(),y.getBuildNr()):null;
			})
			.filter(x->x!=null)
			.sorted((x,y)->x.buildnr-y.buildnr)
			.collect(Collectors.toList());
			BenchmarkAction a;
			int index = allBuilds.size()-1;
			do{
				int buildnr = Integer.parseInt(allBuilds.get(index).getDisplayName().substring(1));
				a = getPreviousAction(allBuilds.get(index));
				//If success a.getResults can't be null
				if(a!=null && a.getResults()!=null){
					int i = 0;
					Pair p = null;
					while(i<list.size() && (p = list.get(i)).buildnr<=buildnr){
						Double c = a.getResults().get(p.metric);
						if(c!=null){
							list.remove(p);
							firstresults.put(p.metric, c);
						}else
							i++;
					}
				}
				index--;
			}while(index>=0 && !list.isEmpty());
			if(!firstresults.isEmpty())
				return new Gson().toJson(firstresults);
		}
		return "undefined";
	}
	
	public String getLastStableResultAsJson(){
		return getLastResultsAsJson(true);
	}
	
	public String getLastResultsAsJson(){
		return getLastResultsAsJson(false);
	}
	
	public String getLastResultsAsJson(boolean stable){
		if(this.getResults() == null)
			return "undefined";
		Object o;
		if(stable)
			o = run.getPreviousSuccessfulBuild();
		else {
			o = run.getPreviousBuild();
		}
		while (o!= null && o instanceof Build){
			BenchmarkAction r = getPreviousAction((Build<?, ?>)o);
			if(r!=null && r.getResults()!=null){
				return new Gson().toJson(r.getResults());
			}
			o = (wasSuccesfull()&&r!=null)?null:((Build)o).getPreviousBuild();
		}
		return "undefined";
	}

	public String getCurrentResultsAsJson(){
		if(this.getResults() != null)
			return new Gson().toJson(this.getResults());
		else return "undefined";
	}

	public String getFilePath(){
		return this.benchmarkBuilder.getFilepath();
	}

	@Override
	public String getIconFileName() {
		return "/plugin/benchmark-evaluator/images/256x256/result.png";
	}
	
	public String getName() {
		return (results != null) ? results.getName() : null;
	}

	@Override
	public String getDisplayName() {
		return Messages.benchmark_result() + " " + (getName() == null ? "" + getBuildNumber() : getName()); 
	}

	@Override
	public String getUrlName() {
		return "benchmark" + "_"  + this.getBuildNumber(); 
	}

	@Override
	public void onAttached(Run<?, ?> run) {
		this.run = run; 
	}

	@Override
	public void onLoad(Run<?, ?> run) {
		this.run = run; 
	}

	public Run<?, ?> getRun() { 
		return run;
	}

	public BenchmarkBuilder getBenchmarkBuilder() {
		return benchmarkBuilder;
	}

	private int getBuildNumber(){
		return this.run.getActions(this.getClass()).indexOf(this) + 1;
	}

	private BenchmarkAction getPreviousAction(Build<?, ?> b){
		Optional<? extends BenchmarkAction> l = 
				b.getActions(this.getClass())
				.stream().filter(x->{
					return x.getBenchmarkBuilder().equals(this.benchmarkBuilder);
				}).findFirst();
		return (l.isPresent())?l.get():null;
	}

	public boolean wasSuccesfull(){
		return run.getResult() == Result.SUCCESS;
	}

	public BenchmarkConfiguration getConfig(){
		return benchmarkConfig;
	}

	public String getConfigAsJson(){
		return getConfig().getConfigAsJson();
	}
	
	public boolean wasCurrentActionSuccesfull(){
		return succes;
	}
	
	public String getUnits(){
		return "{"+benchmarkBuilder.getConfig(run).getEntrys().stream()
				.filter(x->x.getValue().getUnit()!=null)
				.map(x->String.format("'%s':'%s'",x.getKey(),x.getValue().getUnit()))
				.collect(Collectors.joining(","))
				+"}";
	}
}
