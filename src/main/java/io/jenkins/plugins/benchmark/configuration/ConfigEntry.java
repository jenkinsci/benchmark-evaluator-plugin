package io.jenkins.plugins.benchmark.configuration;

public class ConfigEntry {
	
	final private Double minPercent;
	final private Double maxPercent;
	final private Double minValue;
	final private Double maxValue;
	final private String unit;
	final private int buildNr;
	
	public ConfigEntry(Double minPercent, Double maxPercent, Double minValue, Double maxValue, String unit, int buildNr) {
		this.minPercent = minPercent;
		this.maxPercent = maxPercent;
		this.maxValue = maxValue;
		this.buildNr = buildNr;
		this.unit = unit;
		this.minValue = minValue;
	}
	
	public Double getMinPercent() {
		return minPercent;
	}

	public Double getMaxPercent() {
		return maxPercent;
	}

	public Double getMaxValue() {
		return maxValue;
	}

	public int getBuildNr() {
		return buildNr;
	}

	public String getUnit() {
		return unit;
	}

	public Double getMinValue() {
		return minValue;
	}

	public String getUnitName(){
		return unit==null?"":" "+unit;
	}

}
