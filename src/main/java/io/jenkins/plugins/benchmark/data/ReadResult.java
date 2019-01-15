package io.jenkins.plugins.benchmark.data;

import java.util.Map;

public class ReadResult {
	
	private String name;
	private Map<String, Double> messurements;
	private String exception;
	
	public ReadResult(String name, Map<String, Double> messurements, String exception) {
		super();
		this.name = name;
		this.messurements = exception != null ? null : messurements;
		this.exception = exception;
	}

	public String getName() {
		return name;
	}

	public Map<String, Double> getMessurements() {
		return messurements;
	}
	
	public String getException(){
		return this.exception;
	}
}
