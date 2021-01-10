package io.jenkins.plugins.benchmark;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import javax.servlet.ServletException;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Build;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import io.jenkins.plugins.benchmark.configuration.BenchmarkConfiguration;
import io.jenkins.plugins.benchmark.configuration.ConfigEntry;
import io.jenkins.plugins.benchmark.data.BenchmarkResults;
import io.jenkins.plugins.benchmark.data.BenchmarkResults.WrongFormatException;
import io.jenkins.plugins.benchmark.data.ReadResult;
import io.jenkins.plugins.benchmark.data.Reader.InputException;
import jenkins.tasks.SimpleBuildStep;

public class BenchmarkBuilder extends Builder implements SimpleBuildStep {

	private transient static Set<Integer> ids = new HashSet<Integer>();

	final private String filepath;
	private AbstractProject<?, ?> project;

	private final int randomNr;

	@DataBoundConstructor
	public BenchmarkBuilder (String filepath) {
		this.filepath = filepath;
		int r;
		synchronized (ids) {
			do {
				r = new Random().nextInt(Integer.MAX_VALUE);
			}while(ids.contains(r));
			ids.add(r);
		} 
		randomNr = r;
	}

	public String getFilepath(){
		return filepath;
	}

	@Override
	@SuppressFBWarnings("NS_DANGEROUS_NON_SHORT_CIRCUIT")
	public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
			throws InterruptedException, IOException {
		boolean succes = false;
		ReadResult m = null;
		try {
			m = new BenchmarkResults(filepath).read();
		} catch (InputException | WrongFormatException  e ) {
			listener.getLogger().println(e.getMessage());
		} 
		if(m != null && m.getException() != null) listener.getLogger().println(m.getException());
		
		BenchmarkConfiguration conf = getConfig(run);
		if(m == null || m.getMessurements() == null){
			listener.getLogger().println(Messages.read_error(filepath));
			run.setResult(Result.FAILURE);
		}else{
			listener.getLogger().println(Messages.read_succes());
			boolean tb = timeIsBetter(listener.getLogger(), conf, m.getMessurements());
			Map<String, Double> lastResults = null;
			BenchmarkAction a = getPreviousBenchmarkAction(run);
			if(a!=null)
				lastResults = a.getResults();
			if(!tb | !percentIsBetter(listener.getLogger(), conf, m.getMessurements(), lastResults)){
				run.setResult(Result.FAILURE);
			}else{
				for(String k : m.getMessurements().keySet()){
					conf.add(k, new ConfigEntry(null, null, null,null,null,Integer.parseInt(run.getDisplayName().substring(1))));
				}
				run.setResult(Result.SUCCESS);
				succes = true;
			}
		}

		BenchmarkAction bA = new BenchmarkAction(m,this,succes,conf.copy());
		run.addAction(bA);

	}
	
	private transient BenchmarkConfiguration bc;
	
	public BenchmarkConfiguration getConfig(Run<?, ?> run){
		if(bc==null) {
			return (bc = BenchmarkConfiguration.getConfig(getCurrentWorkspace(run)+"config"+getID()+".config"));
		}
		else return bc;
	}
	
	public BenchmarkConfiguration getConfig(AbstractProject<?, ?> project){
		if(bc==null) {
			return (bc = BenchmarkConfiguration.getConfig(project.getRootDir().getAbsolutePath()+File.separator+"config"+getID()+".config"));
		}
		else return bc;
	}

	private String getCurrentWorkspace(Run<?, ?> run){
		String s = run.getRootDir().getAbsolutePath();
		s = s.substring(0,s.lastIndexOf(File.separator));
		s = s.substring(0,s.lastIndexOf(File.separator)+1);
		return s;
	}

	private BenchmarkAction getPreviousBenchmarkAction(Run<?, ?> run){
		Object o = run.getPreviousSuccessfulBuild();
		if(o!=null && o instanceof Build){
			Optional<? extends BenchmarkAction> l = 
					((Build<?, ?>)o).getActions(BenchmarkAction.class)
					.stream().filter(x->{
						return x.getBenchmarkBuilder().equals(this);
					}).findFirst();
			return (l.isPresent())?l.get():null;
		}
		return null;
	}

	private boolean timeIsBetter(PrintStream printStream, BenchmarkConfiguration conf, Map<String, Double> m){
		boolean better = true;
		for(Entry<String, Double> e : m.entrySet()){
			String key = e.getKey();
			ConfigEntry c = conf.get(key);
			if(c!=null){
				Double curValue = e.getValue();
				Double maxV = c.getMaxValue();
				Double minV = c.getMinValue();
				if(maxV!=null && maxV+0.00000000001<curValue){
					printStream.println(Messages.valueToHigh(key,curValue,maxV,c.getUnitName()));
					better = false;
				}
				if(minV!=null && minV-0.00000000001>curValue){
					printStream.println(Messages.valueToLow(key,curValue,minV,c.getUnitName()));
					better = false;
				}
			}
		}
		return better;
	}

	private boolean percentIsBetter(PrintStream printStream, BenchmarkConfiguration conf, Map<String, Double> m, Map<String, Double> lastResults){
		boolean better = true;
		if(lastResults==null)
			return better;
		for(Entry<String, Double> e : m.entrySet()){
			String key = e.getKey();
			ConfigEntry c = conf.get(key);
			if(c!=null){
				Double lastValue = lastResults.get(e.getKey());
				Double curValue = e.getValue();
				Double maxP = c.getMaxPercent();
				Double minP = c.getMinPercent();
				if(lastValue!=null ){
					if( lastValue!=0){
						double percent = calculateChange(lastValue,curValue)*100;
						
						if (maxP!=null && maxP+0.00000000001 < percent){
							
							printStream.println(Messages.higherThanLastTime(key,percent,maxP));
							better = false;
							
						}else if (minP!=null && minP-0.00000000001 > percent){
							
							printStream.println(Messages.lowerThanLastTime(key,percent,minP));
							better = false;
							
						}
					}else{
						
						if((curValue>0 && maxP!=null) || (curValue == 0  && maxP!=null && maxP<0)){
							printStream.println(Messages.higherThanExpected(key,maxP));
							better = false;
						}else if((curValue<0 && minP!=null) || (curValue == 0 && minP!=null && minP>0)){
							printStream.println(Messages.lowerThanExpected(key,minP));
							better = false;
						}
						
					}
				}
			}
		}
		return better;
	}
	
	private Double calculateChange(double prev, double now){
		double r = ((now/prev)-1);
		if((now > prev && r > 0) || (now == prev && r == 0) || (now < prev && r < 0)){
			return r;
		}else{
			return r * -1;
		}
	}

	@Override
	public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
		List<Action> actions = new ArrayList<>();
		if(this.project==null)
			this.project = project;
		else if(project != this.project){
			return actions;
		}
		actions.addAll(super.getProjectActions(project));
		actions.add(new BenchmarkResultAction(project,this));
		actions.add(new BenchmarkConfigAction(getConfig(project),project));
		return actions;
	}


	@Override
	public boolean equals(Object obj) {
		if(obj instanceof BenchmarkBuilder) {
			BenchmarkBuilder b = (BenchmarkBuilder) obj;
			return this.getFilepath().equals(b.getFilepath())  && this.randomNr == b.randomNr;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return randomNr + this.filepath.hashCode();
	}

	public int getID(){
		return randomNr;
	}

	@Symbol("benchmark")
	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

		public FormValidation doCheckFilepath(@QueryParameter String filepath) throws IOException, ServletException {
			File f = new File(filepath);
			if(f.exists() && f.isDirectory()){
				return FormValidation.error(Messages.fileIsDir());
			}
			String ending = null;
			for(String end : BenchmarkResults.endings){
				if(filepath.toLowerCase().endsWith("."+end)){
					ending = end;
				}
			}
			if(ending==null){
				return FormValidation.error(Messages.wrongFormat(BenchmarkResults.getFormats()));
			}
			boolean web = filepath.toLowerCase().startsWith("http") || filepath.toLowerCase().startsWith("ftp");
			if(!web){
				int lastI = filepath.lastIndexOf(File.separator);
				if(lastI==-1){
					return FormValidation.warning(Messages.noFile());
				}
				if(filepath.substring(filepath.lastIndexOf(File.separatorChar)+1, filepath.lastIndexOf("."+ending)).length()==0){
					return FormValidation.warning(Messages.noFileName());
				}
				if(!f.exists()){
					return FormValidation.warning(Messages.fileIsNotPresent());
				}
			}
			Map<String, Double> m = null;
			try {
				ReadResult r = new BenchmarkResults(filepath).read();
				if(r != null) {
					if(r.getException() != null) return FormValidation.error(r.getException());
					m = r.getMessurements();
				}
			} catch (InputException e) {
				return FormValidation.error(e.getMessage());
			} catch (WrongFormatException e) {}
			if(m == null){
				return FormValidation.error(Messages.read_error(filepath));
			}
			return FormValidation.ok();
		}

		public DescriptorImpl(){
			load();
		}

		@Override
		public String getDisplayName() {
			return Messages.benchmark();
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

	}


}
