package io.jenkins.plugins.benchmark;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import hudson.model.FreeStyleBuild;

public class HelperClass {
	
	public static String testdir = "testdir";
	
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
	
	public static void createTestDir(){
		File f = new File("testdir/");
		if(!f.exists()) f.mkdir();
	}
	
	public static void writeTestFile(String name, String content){
		createTestDir();
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
	
	public static void deleteTestFiles(){
		File f = new File("testdir/");
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
