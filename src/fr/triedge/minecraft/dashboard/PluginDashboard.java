package fr.triedge.minecraft.dashboard;

import java.io.File;
import java.util.logging.Level;

import javax.xml.bind.JAXBException;

import org.bukkit.plugin.java.JavaPlugin;

import fr.triedge.minecraft.dashboard.ctl.MetricManager;
import fr.triedge.minecraft.dashboard.model.TaskGathering;
import fr.triedge.minecraft.dashboard.model.TaskProcessing;
import fr.triedge.minecraft.dashboard.model.MetricList;
import fr.triedge.minecraft.dashboard.utils.Utils;
import fr.triedge.minecraft.web.server.WebServer;

public class PluginDashboard extends JavaPlugin{
	
	public static final String METRIC_FILE					= "plugins/MCDashboard/metrics.xml";
	public static final String INDEX_FILE					= "index.html";
	
	private MetricManager metricManager;

	@Override
	public void onDisable() {
		super.onDisable();
	}

	@Override
	public void onEnable() {
		super.onEnable();
		
		// Set metric manager
		setMetricManager(new MetricManager(this));
		
		// Load metrics
		try {
			loadMetrics();
		} catch (JAXBException e) {
			getLogger().log(Level.SEVERE,"Cannot load metric file",e);
		}
		
		// Registering gathering task
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new TaskGathering(this), 0L, 5000L);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new TaskProcessing(this), 0L, 5000L);
		
		// Starting web server
		WebServer server = new WebServer();
		server.setPort(55000);
		Thread th = new Thread(server);
		th.start();
		getLogger().info("Web server started on port 55000");
	}

	@Override
	public void onLoad() {
		super.onLoad();
	}
	
	private void loadMetrics() throws JAXBException {
		File file = new File(METRIC_FILE);
		MetricList list = Utils.loadXml(MetricList.class, file);
		getMetricManager().setMetricList(list);
	}

	public MetricManager getMetricManager() {
		return metricManager;
	}

	public void setMetricManager(MetricManager metricManager) {
		this.metricManager = metricManager;
	}

	
}
