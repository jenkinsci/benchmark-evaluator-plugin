package io.jenkins.plugins.benchmark;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;

public class HelperClass {

	public JenkinsRule jenkinsRule;
	private LinkedList<File> files = new LinkedList<File>();

	public HelperClass( JenkinsRule jenkinsRule ){
		this.jenkinsRule = jenkinsRule;
	}
	
	public void writeFile(String path, String text){
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

	/**
	 * Create a new FreeStyleProject
	 * Project is initialized here to keep track of the files created
	 * @return the created FreeStyleProject
	 */
	public FreeStyleProject createFreeStyleProject(){
		try {
			final FreeStyleProject freeStyleProject = jenkinsRule.createFreeStyleProject();
			files.add( freeStyleProject.getRootDir() );
			return freeStyleProject;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String createTestDir(){
        try {
			final Path tempDirectory = Files.createTempDirectory( "testdir" );
			System.out.println( "Created temp directory: " + tempDirectory.toString());
			files.add( tempDirectory.toFile() );
			return tempDirectory.toString();
		} catch ( IOException e ) {
            throw new RuntimeException( e );
        }
	}
	
	public void writeTestFile(String testdir, String name, String content){
		PrintWriter out = null;
		try {
			final String fileName = testdir + File.separatorChar + name;
			files.add( new File( fileName ) );
			out = new PrintWriter(new BufferedWriter(new FileWriter( fileName )));
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
	
	public void deleteTestFiles(){
		for(File f : files){
			if(f.exists()){
				if( f.isDirectory() ){
					for(File file : f.listFiles()){
						file.delete();
					}
					f.delete();
				} else {
					f.delete();
				}
			}
		}
		files.clear();
	}

	public String getLogs(FreeStyleBuild build) {
		try {
			return build.getLog(5000).stream().reduce("", (a,b) -> a + System.lineSeparator() + b);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
