package io.jenkins.plugins.benchmark.data;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.plugins.benchmark.Messages;
import io.jenkins.plugins.benchmark.data.Reader.InputException;

public class BenchmarkResults {
	
	private final String path;
	
	@SuppressFBWarnings("MS_MUTABLE_ARRAY")
	//Not final for testing
	public static String[] endings = {"csv", "ycsb"};
	
	private final Reader reader;

	public BenchmarkResults(String path) throws WrongFormatException {
		this.path = path;
		if(path.endsWith(".csv")){
			reader = new CSVReader();
		}else if(path.endsWith(".ycsb")){
			reader = new YCSBReader();
		}else
			throw new WrongFormatException(Messages.wrongFormat(getFormats())); 
	}
	
	@SuppressFBWarnings({"RV_RETURN_VALUE_IGNORED_BAD_PRACTICE","DM_DEFAULT_ENCODING"})
	public ReadResult read() throws InputException{
		return reader.read(path);
	}
	
	public static class WrongFormatException extends Exception{

		private static final long serialVersionUID = -3845234573349339916L;

		WrongFormatException(String message) {
			super(message);
		}
		
		WrongFormatException() {
			super();
		}
		
	}
	
	public static String getFormats(){
		String r = "";
		for (int i = 0; i < endings.length; i++) {
			r=r.concat(endings[i]);
			if(i<endings.length-2)
				r=r.concat(", ");
			if(i==endings.length-2)
				r=r.concat(" ").concat(Messages.and()).concat(" ");
		}
		return r;
	}
	

}
