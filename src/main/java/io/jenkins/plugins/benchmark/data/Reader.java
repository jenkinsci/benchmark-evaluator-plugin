package io.jenkins.plugins.benchmark.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.FilePath;

public abstract class Reader {
	
	public abstract ReadResult read(String file, FilePath workspace) throws InputException;
	
	@SuppressFBWarnings("DM_DEFAULT_ENCODING")
	protected InputStreamReader getBufferedReader(String path, FilePath workspace) throws IOException{
		if (workspace != null) {
			FilePath fileOnNode = workspace.child(path);
			try {
				if(fileOnNode.exists())
					return new InputStreamReader(fileOnNode.toVirtualFile().open());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		File f = new File(path);
		if(!f.exists() || f.isDirectory()){
			if(path.toLowerCase().startsWith("http://")||path.toLowerCase().startsWith("ftp://")){
				URL u = new URL(path);
				return new InputStreamReader(u.openStream());
			}else if(path.toLowerCase().startsWith("https://") || path.toLowerCase().startsWith("ftps://")){
				URL u = new URL(path);
				HttpsURLConnection con = (HttpsURLConnection)u.openConnection();
				return new InputStreamReader(con.getInputStream());
			}else{
				throw new FileNotFoundException();
			}
		}
		return new FileReader(f);
	}
	
	protected boolean isDouble(String b){
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
	
	public static class InputException extends Exception{

		private static final long serialVersionUID = -5482787821376104256L;

		InputException(String message) {
			super(message);
		}
		
	}

}
