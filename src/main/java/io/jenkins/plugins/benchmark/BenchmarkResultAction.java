package io.jenkins.plugins.benchmark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ProminentProjectAction;
import hudson.model.Result;
import io.jenkins.plugins.benchmark.configuration.BenchmarkConfiguration;
import io.jenkins.plugins.benchmark.configuration.ConfigEntry;

public class BenchmarkResultAction  implements ProminentProjectAction{
	

	final private AbstractProject<?, ?> project;
	final private BenchmarkBuilder benchmarkBuilder;
	private String lastResult = null;
	private int lastLength = -1;
	private AbstractBuild<?, ?> lastElement = null;
	Map<String,String> metricValues = new ConcurrentHashMap<String,String>();
	final private BenchmarkConfiguration globalConfig;
	
	
	public BenchmarkResultAction(AbstractProject<?, ?> project,BenchmarkBuilder benchmarkBuilder) {
		this.project = project;
		this.benchmarkBuilder = benchmarkBuilder;
		this.globalConfig = benchmarkBuilder.getConfig(project);
	}
	
	
	@Override
	public String getIconFileName() {
		int i = getNumber();
		if(i==-1)
			return null;
		else
			return "/plugin/benchmark-evaluator/images/256x256/diagramm.png";
	}

	@Override
	public String getDisplayName() {
		int i = getNumber();
		if(i==-1)
			return null;
		else			
		return Messages.benchmarkResults()+" "+i;
	}

	@Override
	public String getUrlName() {
		int i = getNumber();
		if(i==-1)
			return null;
		else			
			return "benchmarkResult_"+i;
	}

	private int getNumber(){
		int i = project.getProminentActions().stream().filter(x->x instanceof BenchmarkResultAction)
				.collect(Collectors.toList()).indexOf(this);
		return (i==-1)?-1:i+1;
	}
	
	private String getBuildNrs(Stream<AbstractBuild<?, ?>> stream){
		String b = stream.map(x->x.getDisplayName()).reduce("[", (x,y)->x.concat("'").concat(y).concat("',"));
		if(b.length()==1){
			return "[]";
		}
		return b.substring(0,b.length() - 1)+']';
	}
	
	private ArrayList<AbstractBuild<?, ?>> getAllActions(){
		ArrayList<AbstractBuild<?, ?>> list = new ArrayList<AbstractBuild<?, ?>>();
		for( AbstractBuild<?, ?> b = project.getLastBuild() ; b != null ; b = b.getPreviousBuild() ) {
			if(getPreviousAction(b)!=null) list.add(0,b);
        }
		return list;
	}
	
	private Set<String> lastKeySet = null;
	
	public synchronized String getAllInformations(){
		ArrayList<AbstractBuild<?, ?>> in = getAllActions();
		if(in.size() == lastLength && (in.isEmpty() || in.get(in.size()-1) == lastElement)){
			boolean error = false;
			for(String lk : lastKeySet){
				String u1 = globalConfig.get(lk).getUnit();
				String u2 = metricValues.containsKey(lk) ? metricValues.get(lk) : null;
				if((u1 == null && u2 != null)||(u1 != null && u2 == null)||(u1 != null && !u1.equals(u2))){
					error = true;
				}
			}
			if(!error) return lastResult;
		}
		metricValues.clear();
		lastLength = in.size();
		lastElement = (in.isEmpty())?null:in.get(in.size()-1);
		BenchmarkAction[] actions = in.parallelStream().map(x->getPreviousAction(x)).toArray(BenchmarkAction[]::new);
		BenchmarkAction[] prevActions = in.parallelStream().map(x->getPreviousAction(x.getPreviousSuccessfulBuild())).toArray(BenchmarkAction[]::new);
		final Map<String,Double[][]> map = new ConcurrentHashMap<String,Double[][]>();
		final String[] names = new String[actions.length];
		
		
		for (int i = 0; i < actions.length; i++) {
			final int iteration = i;
			final BenchmarkAction action = actions[i];
			final BenchmarkAction prevAction = prevActions[i];
			if(action != null) {
				names[i] = action.getName();
			}
			//action can't be null
			if(action!=null && action.getResults()!=null){
				final Map<String, Double> prevResult;
				if(prevAction!=null){
					prevResult = prevAction.getResults();
				}else
					prevResult = null;
				Map<String, Double> result = action.getResults();
				result.entrySet().parallelStream().map(x->{
					Double[][] metricsResults = null;
					String key = x.getKey();
					
					if(!map.containsKey(key)){
						ConfigEntry t = globalConfig.get(key);
						if(t.getUnit() != null) metricValues.put(key, t.getUnit());
						metricsResults = new Double[actions.length][6];
						map.put(key, metricsResults);
						for (int j = 0; j < actions.length; j++) {
							ConfigEntry c;
							//actions[j] and .getConfig can't be null
							if(actions[j]!=null && actions[j].getConfig()!=null && (c = actions[j].getConfig().get(key))!=null){
								metricsResults[j][1] = c.getMinValue();
								metricsResults[j][2] = c.getMaxValue();
								Double p_min = c.getMinPercent();
								Double p_max = c.getMaxPercent();
								metricsResults[j][4] = (p_min==null)?null:(p_min/100);
								metricsResults[j][5] = (p_max==null)?null:(p_max/100);
							}
						}
					}else{
						metricsResults = map.get(key);
					}
					
					metricsResults[iteration][0] = x.getValue();
					if(prevResult!=null && prevResult.get(key)!=null){
						metricsResults[iteration][3] = calculateChange(prevResult.get(key), x.getValue());
					}
					return null;
				}).forEach(x->{});
			}
		}
		
		lastKeySet = map.keySet();
		String result = "";
		String dataSet2 = "{";
		
		dataSet2 = dataSet2.concat(map.entrySet().parallelStream().map(e->{
			return "'".concat(e.getKey())
				.concat("':[")
				.concat(Arrays.stream(e.getValue()).map(x -> {
					Double per = x[3];
					String perS = "";
					if(per == null){
						perS = "null";
					}else if(per == Double.POSITIVE_INFINITY){
						perS = "'inf'";
					}else if(per == Double.NEGATIVE_INFINITY){
						perS = "'-inf'";
					}else
						perS = per.toString();
					return "[" + x[0] + "," + x[1] + "," + x[2] + "," + perS + "," + x[4] + "," + x[5] + "]";
				})
				.collect(Collectors.joining(",")))
				.concat("]");
		}).collect(Collectors.joining(","))).concat("}");
		
		result +="var buildNr = " + getBuildNrs(in.stream()) + ";\n";
		result +="var unsuccesfullBuildsNames = " + getBuildNrs(in.stream().filter(x->x.getResult()!=Result.SUCCESS)) + ";\n";
		result +="var dataSet2 = " + dataSet2 + ";\n";
		result +="var metricInfo = " + new Gson().toJson(metricValues) + ";\n";
		result +="var buildNames = " + new Gson().toJson(names) + ";";
		lastResult = result;
		return result;
	}
	
	private Double calculateChange(double prev, double now){
		if(prev == 0){
			if(now > 0){
				return Double.POSITIVE_INFINITY;
			}else if(now < 0){
				return Double.NEGATIVE_INFINITY;
			}else return 0d;
		}
		double r = ((now/prev)-1);
		if((now > prev && r > 0) || (now == prev && r == 0) || (now < prev && r < 0)){
			return r;
		}else{
			return r * -1;
		}
	}
	
	private BenchmarkAction getPreviousAction(AbstractBuild<?, ?> b){
		if(b==null)return null;
		Optional<? extends BenchmarkAction> l = 
				b.getActions(BenchmarkAction.class)
				.stream().filter(x->{
					return x.getBenchmarkBuilder().equals(this.benchmarkBuilder);
				}).findFirst();
		return (l.isPresent())?l.get():null;
	}
}
