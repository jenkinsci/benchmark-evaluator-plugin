package io.jenkins.plugins.benchmark.configuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

class BenchmarkConfigLoader {

	private static String head = "metrikname;minPercent_since_last_build;maxPercent_since_last_build;min_value;max_value;unit;buildNr";
	private String path;
	private final Map<String, ConfigEntry> config;

	BenchmarkConfigLoader(String path, Map<String, ConfigEntry> config) throws FileNotFoundException {
		this.path = path;
		this.config = config;
		this.load();
	}

	@SuppressFBWarnings({"DM_DEFAULT_ENCODING"})
	void load() throws FileNotFoundException{
		BufferedReader in = null;
		synchronized (config) {
			File f = new File(path);
			if(!f.exists()){
				System.out.println("New config: " + path);
				PrintWriter out = null;
				try {
					out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
					out.println(head);
					return;
				} catch (IOException e) {
					e.printStackTrace();
				}finally{
					if(out!=null)
						out.close();
					else
						throw new FileNotFoundException("Could not create config: " + f);
				}
				return;
			}
			try {
				in = new BufferedReader(new FileReader(f));
				String line;
				line = in.readLine();
				while((line = in.readLine()) != null){
					String[] colums = line.split(";");
					int buildNr;
					if(colums.length ==  7 && isInteger(colums[6])){
						ConfigEntry c = new ConfigEntry(
								(isDouble(colums[1]))?Double.parseDouble(colums[1]):null, 
								(isDouble(colums[2]))?Double.parseDouble(colums[2]):null,
								(isDouble(colums[3]))?Double.parseDouble(colums[3]):null, 
								(isDouble(colums[4]))?Double.parseDouble(colums[4]):null,
								(colums[5].equals(""))?null:colums[5],
								((buildNr=Integer.parseInt(colums[6])) > -1) ? buildNr : -1
								);
						config.put(colums[0], c);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				if(in!=null)
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
	}

	private boolean isDouble(String b){
		if(b==null || b.length()==0)
			return false;
		boolean point = false;
		for (int i = 0; i < b.length(); i++) {
			if(i==0 && b.charAt(i)=='-') continue;
			if((b.charAt(i)<'0'||b.charAt(i)>'9')&&b.charAt(i)!='.') return false;
			if(b.charAt(i)=='.'){
				if(point) return false;
				else point = true;
			}
		}
		return true;
	}

	private boolean isInteger(String b){
		if(b==null || b.length()==0)
			return false;
		for (int i = 0; i < b.length(); i++) {
			if(i==0 && b.charAt(i)=='-') continue;
			if((b.charAt(i)<'0'||b.charAt(i)>'9')) 
				return false;
		}
		return true;
	}

	private final AtomicBoolean unwrittenChanges = new AtomicBoolean(false);
	private static ExecutorService pool = Executors.newFixedThreadPool(1);

	void save(){
		synchronized (config) {
			if(!unwrittenChanges.get()){
				unwrittenChanges.set(true);
				pool.execute(new SaveRunnable(path, config, unwrittenChanges));
			}
		}
	}

	private String nullasNothing(Double in){
		if(in == null) return "";
		else return in.toString();
	}

	private class SaveRunnable implements Runnable{

		private String path;
		private final Map<String, ConfigEntry> config;
		private final AtomicBoolean unwrittenChanges;

		public SaveRunnable(String path, Map<String, ConfigEntry> config, AtomicBoolean unwrittenChanges) {
			super();
			this.path = path;
			this.config = config;
			this.unwrittenChanges = unwrittenChanges;
		}

		@Override
		@SuppressFBWarnings({"RV_RETURN_VALUE_IGNORED_BAD_PRACTICE"
			,"DM_DEFAULT_ENCODING","JLM_JSR166_UTILCONCURRENT_MONITORENTER"})
		public void run() {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {}
			File f = new File(path + ".tmp");
			PrintWriter out = null;
			boolean b = false;
			synchronized (this.config) {
				b = this.unwrittenChanges.get();
				if(b){
					try {
						out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
						final PrintWriter out2 = out;
						out.println(head);
						config.forEach((m,v)->{
							out2.println(String.format("%s;%s;%s;%s;%s;%s;%s", 
									m,
									nullasNothing(v.getMinPercent()),
									nullasNothing(v.getMaxPercent()),
									nullasNothing(v.getMinValue()),
									nullasNothing(v.getMaxValue()),
									v.getUnit()==null?"":v.getUnit(),
									v.getBuildNr()));
						});
					} catch (IOException e) {
						e.printStackTrace();
					}finally{
						if(out!=null)
							out.close();
					}
					this.unwrittenChanges.set(false);
					try{
						File oldF = new File(path);
						if(oldF.exists())
							oldF.delete();
						f.renameTo(oldF);
					}catch (SecurityException e) {
						e.printStackTrace();
					}
				}
			}
		}

	}



}
