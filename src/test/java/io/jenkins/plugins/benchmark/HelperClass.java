package io.jenkins.plugins.benchmark;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import hudson.model.FreeStyleBuild;

public class HelperClass {
	
	public static void writeFile(String path, String text){
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(path)));
			if(text!=null){
				out.println("Metrik;time");
				out.println(text);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(out!=null)
				out.close();
		}
	}
	
	public static String createTestDir(){
        try {
			final Path tempDirectory = Files.createTempDirectory( "testdir" );
			System.out.println( "Created temp directory: " + tempDirectory.toString());
			return tempDirectory.toString();
		} catch ( IOException e ) {
            throw new RuntimeException( e );
        }
	}
	
	public static void writeTestFile(String testdir, String name, String content){
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(testdir + File.separatorChar + name)));
			if(content!=null){
				out.println(content);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(out!=null)
				out.close();
		}
	}
	
	public static void deleteTestFiles( String testdir ){
		File f = new File( testdir );
		if(f.exists() && f.isDirectory()){
			for(File file : f.listFiles()){
				file.delete();
			}
			f.delete();
		}
	}
	
	public static String getLogs(FreeStyleBuild build) {
		try {
			return build.getLog(5000).stream().reduce("", (a,b) -> a + System.lineSeparator() + b);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
