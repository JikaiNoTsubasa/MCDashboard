package fr.triedge.minecraft.dashboard.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="MetricList")
public class MetricList {

	private ArrayList<Metric> metrics = new ArrayList<>();
	
	public MetricList() {
	}

	public ArrayList<Metric> getMetrics() {
		return metrics;
	}

	@XmlElementWrapper(name="Metrics")
    @XmlElement(name="Metric")
	public void setMetrics(ArrayList<Metric> metrics) {
		this.metrics = metrics;
	}
	
	
}
