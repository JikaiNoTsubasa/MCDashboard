package fr.triedge.minecraft.dashboard.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;

import javax.xml.bind.JAXBException;

import fr.triedge.minecraft.dashboard.PluginDashboard;
import fr.triedge.minecraft.dashboard.ctl.GatherManager;
import fr.triedge.minecraft.dashboard.utils.Utils;

public class TaskGathering implements Runnable{
	
	private PluginDashboard plugin;
	
	public TaskGathering(PluginDashboard plugin) {
		super();
		this.plugin = plugin;
	}

	@Override
	public void run() {
		plugin.getLogger().info("Gathering data from server...");
		Date date = new Date();
		Metric HP = GatherManager.getMetricHeapPercent();
		Metric HU = GatherManager.getMetricHeapUsage();
		Metric HM = GatherManager.getMetricHeapMax();
		Metric OP = GatherManager.getMetricOnlinePlayer();
		
		HP.date = date;
		HU.date = date;
		HM.date = date;
		OP.date = date;
		
		ArrayList<Metric> list = plugin.getMetricManager().getMetricList().getMetrics();
		list.add(HU);
		list.add(HP);
		list.add(HM);
		list.add(OP);
		
		File file = new File(PluginDashboard.METRIC_FILE);
		File folder = file.getParentFile();
		if (!folder.exists())
			folder.mkdirs();
		
		try {
			Utils.storeXml(plugin.getMetricManager().getMetricList(), file);
		} catch (JAXBException e) {
			plugin.getLogger().log(Level.SEVERE,"Cannot save metrics",e);
		}
		
		plugin.getLogger().info("Data saved");
	}
	
	

}
