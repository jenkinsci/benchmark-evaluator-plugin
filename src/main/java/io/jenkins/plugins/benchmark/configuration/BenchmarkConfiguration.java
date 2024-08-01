package io.jenkins.plugins.benchmark.configuration;

import java.io.FileNotFoundException;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;

public class BenchmarkConfiguration {

	private static ConcurrentHashMap<String,BenchmarkConfiguration> allConfigurations = new ConcurrentHashMap<String,BenchmarkConfiguration>();

	private ConcurrentHashMap<String,ConfigEntry> config; 
	private BenchmarkConfigLoader bl;
	private String path;

	private BenchmarkConfiguration(String path) {
		this.path = path;
	}

	public static BenchmarkConfiguration getConfig(String path){
		if(!path.toLowerCase().endsWith(".config")) return null;
		BenchmarkConfiguration bc = allConfigurations.compute(path, (k,v) -> v==null?new BenchmarkConfiguration(k):v);
		try {
			bc.load();
		} catch (FileNotFoundException e) {
			System.out.println("Could not find configuration file: " + path);
			e.printStackTrace();
			return null;
		}
		return bc;
	}

	private final Object loadhelper = new Object();

	private void load() throws FileNotFoundException{
		synchronized (loadhelper) {
			if(bl == null){
				config = new ConcurrentHashMap<String,ConfigEntry>();
				bl = new BenchmarkConfigLoader(path,config);
			}
		}
	}

	public boolean add(final String metric, final ConfigEntry config){
		ConfigEntry r = this.config.compute(metric, (m,a)->{
			if(a==null){
				return config;
			}else if(a.getBuildNr() == -1){
				return new ConfigEntry(a.getMinPercent(), a.getMaxPercent(), a.getMinValue(), a.getMaxValue(), a.getUnit(), config.getBuildNr());
			}else
				return a;
		});
		bl.save();
		return r == config;
	}

	public ConfigEntry get(String metric){
		return this.config.get(metric);
	}

	public boolean change(final String metric, final ConfigEntry config){
		
		ConfigEntry r = this.config.computeIfPresent(metric, (x,y)->config);
		if(r==config){
			bl.save();
			return true;
		}
		return false;
	}
	
	public boolean delete(String metric){
		//helper Object to difference between was not present and was deleted
		ConfigEntry deleted = new ConfigEntry(null, null, null, null, null, -1000);
		ConfigEntry r = this.config.computeIfPresent(metric, (x,y)->{
			if(y.getBuildNr()==-1){
				this.config.remove(x);
				return deleted;
			}
			return y;
		});
		bl.save();
		return r == deleted;
	}
	
	public int getSize(){
		return config.size();
	}
	
	public String getConfigAsJson(){
		return new Gson().toJson(config);
	}
	
	public Set<Entry<String, ConfigEntry>> getEntrys(){
		return config.entrySet();
	}
	
	public BenchmarkConfiguration copy(){
		BenchmarkConfiguration b = new BenchmarkConfiguration(null);
		b.config = new ConcurrentHashMap<String,ConfigEntry>();
		this.config.forEach((x,y)->b.config.put(x,y));
		return b;
	}

}
