package io.jenkins.plugins.benchmark.data;

import static org.junit.Assert.*;
import org.junit.Test;

public class ReadFromWeb {

	@Test
	public void test() throws Exception {
		try{
			new CSVReader().read("http://wiki.splunk.com/Http_status.csv");
		}catch(Exception e){
			e.printStackTrace();
			assertTrue(e.getMessage().contains("Falsches Format") || e.getMessage().contains("Wrong format"));
		}
		try{
			new CSVReader().read("https://raw.githubusercontent.com/openmundi/world.csv/master/countries(249)_alpha3.csv");
		}catch(Exception e){
			e.printStackTrace();
			assertTrue(e.getMessage().contains("Falsches Format") || e.getMessage().contains("Wrong format"));
		}
		try{
			new CSVReader().read("ftp://ftp.ebi.ac.uk/pub/databases/chembl/VEHICLe/VEHICLe.csv");
		}catch(Exception e){
			e.printStackTrace();
			assertTrue(e.getMessage().contains("Falsches Format") || e.getMessage().contains("Wrong format"));
		}
		
	}

}
