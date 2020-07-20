package fr.triedge.minecraft.dashboard.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Metric")
public class Metric implements Comparable<Metric>{
	
	public static final String HP				= "HeapPercentage";
	public static final String HU				= "HeapUsage";
	public static final String HM				= "HeapMax";
	public static final String OP				= "OnlinePlayers";

	public String value;
	public String name;
	public Date date;
	
	public Metric() {
	}
	
	public Metric(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public float getFloatValue() {
		return Float.valueOf(value);
	}
	
	public int getIntValue() {
		return Integer.valueOf(value);
	}
	
	public Double getDoubleValue() {
		return Double.parseDouble(value);
	}
	
	@Override
	public int compareTo(Metric metric) {
		return date.compareTo(metric.date);
	}
	
}
