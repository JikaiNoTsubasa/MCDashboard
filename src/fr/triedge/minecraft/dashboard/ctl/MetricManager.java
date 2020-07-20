package fr.triedge.minecraft.dashboard.ctl;

import fr.triedge.minecraft.dashboard.PluginDashboard;
import fr.triedge.minecraft.dashboard.model.MetricList;

public class MetricManager {

	private MetricList metricList = new MetricList();
	private PluginDashboard plugin;
	
	public MetricManager(PluginDashboard plugin) {
		setPlugin(plugin);
	}

	public PluginDashboard getPlugin() {
		return plugin;
	}

	public void setPlugin(PluginDashboard plugin) {
		this.plugin = plugin;
	}

	public MetricList getMetricList() {
		return metricList;
	}

	public void setMetricList(MetricList metricList) {
		this.metricList = metricList;
	}
}
