package io.jenkins.plugins.benchmark.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.FilePath;
import io.jenkins.plugins.benchmark.Messages;
import io.jenkins.plugins.benchmark.data.BenchmarkResults.WrongFormatException;

public class CSVReader extends Reader {

	@Override
	@SuppressFBWarnings({"RV_RETURN_VALUE_IGNORED_BAD_PRACTICE"})
	public ReadResult read(String path, FilePath workspace) throws InputException{
		
		Map<String,Double> l = new HashMap<String,Double>();
		
		BufferedReader in = null;
		try {
			in = new BufferedReader(getBufferedReader(path, workspace));
			String line = in.readLine();
			String name = null;
			if(line==null) throw new InputException(Messages.fileIsNotPresent());
			int linenr = 1;
			String  e = null;
			boolean searchSep = true;
			char seperator = ';';
			if(isHeader(line)){
				Character sep = findSeperatorInHeader(line);
				if(sep != null){
					seperator = sep;
					searchSep = false;
				}
				line = in.readLine();
				linenr++;
			}
			char dot = '_';
			do{
				if(line == null) break;
				if(searchSep){
					try {
						seperator = findSeperator(line);
						searchSep = false;
					} catch (WrongFormatException e1) {
						e =  Messages.wrong_file_format(linenr,line);
					}
				}
				String[] s = line.split(""+seperator);
				if(s.length==2 && s[0].toLowerCase().equals("name")) {
					name = s[1];
				}else{
					if(s.length==2 && seperator == ';'){
						if(dot == '_'){
							if(s[1].contains(".")) dot = '.';
							else if (s[1].contains(",")) dot = ',';
						}
						if(dot == ',') s[1] = s[1].replace(',', '.');
					}
					if(s.length==2 && s[0].length()>0 && isDouble(s[1])){
						l.put(s[0], Double.parseDouble(s[1]));
					}else if(e == null){
						e =  Messages.wrong_file_format(linenr,line);
					}
				}
				linenr++;	
			}while((line=in.readLine())!=null);
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
	
	private boolean isHeader(String in){
		boolean dot = false;
		for (int i = in.length() -1 ; i >= 0; i--) {
			if(in.charAt(i) == ';') return false;
			if(in.charAt(i) == '.'){
				dot = true;
				continue;
			}
			if(in.charAt(i) == ',' && dot) return false;
			if(!Character.isDigit(in.charAt(i)))  return true;
		}
		return false; 
	}
	
	private Character findSeperatorInHeader(String in){
		//Only one character
		if(in.contains(";") && in.indexOf(';') == in.lastIndexOf(';') && !in.contains(",")) return ';';
		if(in.contains(",") && in.indexOf(',') == in.lastIndexOf(',') && !in.contains(";")) return ',';
		return null;
	}
	
	private char findSeperator(String in) throws WrongFormatException{
		boolean dot = false;
		boolean comma = false;
		boolean simicolon = false;
		loop: for (int i = in.length() -1 ; i >= 0; i--) {
			switch (in.charAt(i)) {
				case ';' : 
					if(dot && comma) throw new WrongFormatException(); 
					else simicolon = true;
					break;
				case '.' : 
					dot = true;
					break;
				case ',' :
					comma = true;
					break;
				default  :
					if(!Character.isDigit(in.charAt(i))){
						break loop;
					}
			}
		}
		return simicolon ? ';' : ',';
	}

}
