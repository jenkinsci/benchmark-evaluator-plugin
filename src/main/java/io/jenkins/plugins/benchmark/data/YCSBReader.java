package io.jenkins.plugins.benchmark.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.FilePath;
import io.jenkins.plugins.benchmark.Messages;

public class YCSBReader extends Reader {

	@Override
	@SuppressFBWarnings({"RV_RETURN_VALUE_IGNORED_BAD_PRACTICE"})
	public ReadResult read(String path, FilePath workspace) throws InputException {
		Map<String,Double> l = new HashMap<String,Double>();
		
		BufferedReader in = null;
		try {
			in = new BufferedReader(getBufferedReader(path, workspace));
			String line;
			String name = null;
			int linenr = 1;
			boolean filePresent = false;
			String e = null;
			while((line=in.readLine())!=null){
				filePresent = true;
				String[] s = line.split(",");
				if(s.length==3 && s[0].equals("[INFO]") && s[1].equals(" Name")) {
					name = s[2].substring(1);
				}else if(s.length==3 && s[0].length()>0 && s[1].length()>0 && isDouble(s[2].substring(1))){
					l.put(s[0].substring(1, s[0].length()-1) + s[1], Double.parseDouble(s[2]));
				}else if(e == null){
					e = Messages.wrong_file_format(linenr,line);
				}
				linenr++;	
			}
			if(!filePresent) throw new InputException(Messages.fileIsNotPresent());
			return new ReadResult(name, l, e);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(in!=null)
				try {
					in.close();
				} catch (IOException e) {}
		}
		return null;
	}

}
