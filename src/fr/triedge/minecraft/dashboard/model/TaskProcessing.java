package fr.triedge.minecraft.dashboard.model;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;

import fr.triedge.minecraft.dashboard.PluginDashboard;

public class TaskProcessing implements Runnable{
	
	private PluginDashboard plugin;
	
	public TaskProcessing(PluginDashboard plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		
		plugin.getLogger().log(Level.INFO,"Starting task processing...");
		// Get metrics
		MetricList list = plugin.getMetricManager().getMetricList();
		
		// Split metrics
		ArrayList<Metric> heapUsage = new ArrayList<>();
		ArrayList<Metric> heapMax = new ArrayList<>();
		ArrayList<Metric> heapPercent = new ArrayList<>();
		ArrayList<Metric> onlinePlayers = new ArrayList<>();
		for (Metric metric : list.getMetrics()) {
			switch(metric.name) {
			case Metric.HU:
				heapUsage.add(metric);
				break;
			case Metric.HM:
				heapMax.add(metric);
				break;
			case Metric.HP:
				heapPercent.add(metric);
				break;
			case Metric.OP:
				onlinePlayers.add(metric);
				break;
				
			}
		}
		
		// Sort metrics by date
		Collections.sort(heapUsage);
		Collections.sort(onlinePlayers);
		Collections.sort(heapPercent);
		//Collections.sort(heapMax);
		//Collections.sort(heapPercent);
		//Collections.sort(onlinePlayers);
		
		String cur_HP = heapPercent.get(heapPercent.size()-1).value;
		String cur_OP = onlinePlayers.get(onlinePlayers.size()-1).value;
		
		// Generate file
		String file_str = "<!DOCTYPE html>\n" + 
				"<html lang=\"fr\">\n" + 
				"  <head>\n" + 
				"    <meta charset=\"utf-8\">\n" + 
				"    <title>Metrics</title>\n" + 
				"    <script type=\"text/javascript\" src=\"https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.9.3/Chart.min.js\"></script>\n" + 
				"    <style>\n" + 
				"    html\n" + 
				"    {\n" + 
				"    margin: 0;\n" + 
				"    padding: 0;\n" + 
				"    width: 100%;\n" + 
				"\n" + 
				"    --color-gold: #e4cd70;\n" + 
				"    --bg-color: #F5F6FB; /* #22252c;*/\n" + 
				"    --bg-note-color: #FFFFFF; /*#282c35;*/\n" + 
				"    --bg-light-color: #2b323c;\n" + 
				"    --title-note-color: #e4707d;\n" + 
				"    --feature-note-color: #70e48b;\n" + 
				"    }\n" + 
				"\n" + 
				"    body\n" + 
				"    {\n" + 
				"    background: var(--bg-color);\n" + 
				"    color: #FFF;\n" + 
				"    font-size: 12px;\n" + 
				"    margin: 0;\n" + 
				"    padding: 0;\n" + 
				"    }\n" + 
				"\n" + 
				"    .title_page\n" + 
				"    {\n" + 
				"      width: 20%;\n" + 
				"      text-align: center;\n" + 
				"      margin: 0;\n" + 
				"      font-size: 30px;\n" + 
				"      line-height: 30px;\n" + 
				"      margin: auto;\n" + 
				"      background: var(--bg-note-color);\n" + 
				"      color: var(--title-note-color);\n" + 
				"      padding-top: 5px;\n" + 
				"      padding-bottom: 5px;\n" + 
				"    }\n" + 
				"\n" + 
				"    .panel\n" + 
				"    {\n" + 
				"      width: 80%;\n" + 
				"      margin: auto;\n" + 
				"      margin-top: 50px;\n" + 
				"    }\n" + 
				"\n" + 
				"    .panel_data\n" + 
				"    {\n" + 
				"      background: var(--bg-note-color);\n" + 
				"      margin-bottom: 20px;\n" + 
				"      width: 100%;\n" + 
				"      display: inline-block;\n" + 
				"      margin-right: 20px;\n" + 
				"      box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);\n" + 
				"    }\n" + 
				"\n" + 
				"    .panel_standard\n" + 
				"    {\n" + 
				"      background: var(--bg-note-color);\n" + 
				"      margin-bottom: 20px;\n" + 
				"      width: 100%;\n" + 
				"      box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);\n" + 
				"    }\n" + 
				"\n" + 
				"    .data_highlight\n" + 
				"    {\n" + 
				"      font-size: 20px;\n" + 
				"      padding: 5px;\n" + 
				"      color: var(--feature-note-color);\n" + 
				"    }\n" + 
				"\n" + 
				"    </style>\n" + 
				"  </head>\n" + 
				"  <body>\n" + 
				"    <div class=\"title_page\">\n" + 
				"      MC Dashboard\n" + 
				"    </div>\n" + 
				"    <div class=\"panel\">\n" + 
				"      <div class=\"panel_standard\">\n" + 
				"        <h3>Current Heap Percentage</h3>\n" + 
				"        <span class=\"data_highlight\">###CUR_HP### %</span>\n" + 
				"      </div>\n" + 
				"\n" + 
				"      <div class=\"panel_standard\">\n" + 
				"        <h3>Current Online Players:</h3>\n" + 
				"        <span class=\"data_highlight\">###CUR_OP###</span>\n" + 
				"      </div>\n" + 
				"\n" + 
				"      <div class=\"panel_data\">\n" + 
				"        <canvas id=\"chartHeap\" width=\"100%\" height=\"300\"></canvas>\n" + 
				"\n" + 
				"      </div><!-- end panel data -->\n" + 
				"      <div class=\"panel_data\">\n" + 
				"        <canvas id=\"chartPlayers\" width=\"100%\" height=\"300\"></canvas>\n" + 
				"      </div>\n" + 
				"\n" + 
				"    </div>\n" + 
				"\n" + 
				"    <script>\n" + 
				"    var labs = [###LABELS###];\n" + 
				"    var valHU = [###DATA_HU###];\n" + 
				"    var valHM = [###DATA_HM###];\n" + 
				"    var valOP = [###DATA_OP###];\n" + 
				"    var ctx = document.getElementById('chartHeap').getContext('2d');\n" + 
				"    var myChart = new Chart(ctx, {\n" + 
				"      type: 'line',\n" + 
				"      data: {\n" + 
				"        labels: labs,\n" + 
				"        datasets: [{\n" + 
				"          label: 'Heap Max',\n" + 
				"          backgroundColor: 'rgba(228, 112, 125, 0.2)',\n" + 
				"          borderColor: 'rgba(228, 112, 125, 1)',\n" + 
				"          hoverBackgroundColor: 'rgba(255, 206, 86, 0.2)',\n" + 
				"          hoverBorderColor: 'rgba(255, 206, 86, 1)',\n" + 
				"          data: valHM,\n" + 
				"          borderWidth: 1\n" + 
				"        },\n" + 
				"        {\n" + 
				"          label: 'Heap Usage',\n" + 
				"          backgroundColor: 'rgba(54, 162, 235, 0.2)',\n" + 
				"          borderColor: 'rgba(54, 162, 235, 1)',\n" + 
				"          hoverBackgroundColor: 'rgba(255, 206, 86, 0.2)',\n" + 
				"          hoverBorderColor: 'rgba(255, 206, 86, 1)',\n" + 
				"          data: valHU,\n" + 
				"          borderWidth: 1\n" + 
				"        }\n" + 
				"      ]\n" + 
				"      },\n" + 
				"      options: {\n" + 
				"        maintainAspectRatio: false\n" + 
				"      }\n" + 
				"    });\n" + 
				"\n" + 
				"    var ctx2 = document.getElementById('chartPlayers').getContext('2d');\n" + 
				"    var myChart2 = new Chart(ctx2, {\n" + 
				"      type: 'line',\n" + 
				"      data: {\n" + 
				"        labels: labs,\n" + 
				"        datasets: [{\n" + 
				"          label: 'Online Players',\n" + 
				"          backgroundColor: 'rgba(228, 112, 125, 0.2)',\n" + 
				"          borderColor: 'rgba(228, 112, 125, 1)',\n" + 
				"          hoverBackgroundColor: 'rgba(255, 206, 86, 0.2)',\n" + 
				"          hoverBorderColor: 'rgba(255, 206, 86, 1)',\n" + 
				"          data: valOP,\n" + 
				"          borderWidth: 1\n" + 
				"        },\n" + 
				"      ]\n" + 
				"      },\n" + 
				"      options: {\n" + 
				"        maintainAspectRatio: false,\n" + 
				"        scales: {\n" + 
				"            yAxes: [{\n" + 
				"                ticks: {\n" + 
				"                    suggestedMin: 0\n" + 
				"                }\n" + 
				"            }]\n" + 
				"        }\n" + 
				"      }\n" + 
				"    });\n" + 
				"    </script>\n" + 
				"  </body>\n" + 
				"</html>\n" + 
				"";
		
		// Filter 2hours ago
		Date current = new Date();
		Calendar cal = Calendar.getInstance(); // creates calendar
	    cal.setTime(current); // sets calendar time/date
	    cal.add(Calendar.HOUR_OF_DAY, -5); // subs 5 hours
	    Date tmpDate = cal.getTime();
	    
	    // Format date
	    SimpleDateFormat form = new SimpleDateFormat("HH:mm");
		
		// Generate String list
		StringBuilder tmp = new StringBuilder();
		StringBuilder tmpHU = new StringBuilder();
		StringBuilder tmpHM = new StringBuilder();
		for (Metric met : heapUsage) {
			if (met.date.before(tmpDate))
				continue;
			tmp.append("'");
			tmp.append(form.format(met.date));
			tmp.append("',");
			tmpHU.append(met.value);
			tmpHU.append(",");
		}
		for (Metric met : heapMax) {
			if (met.date.before(tmpDate))
				continue;
			tmpHM.append(met.value);
			tmpHM.append(",");
		}
		StringBuilder tmpOP = new StringBuilder();
		for(Metric met : onlinePlayers) {
			if (met.date.before(tmpDate))
				continue;
			String valueList = met.value;
			int valOnline = 0;
			if (!valueList.equals("-")) {
				valOnline = valueList.split(",").length;
			}
			tmpOP.append(valOnline);
			tmpOP.append(",");
		}
		
		file_str = file_str.replace("###LABELS###", tmp.toString());
		file_str = file_str.replace("###DATA_HU###", tmpHU.toString());
		file_str = file_str.replace("###DATA_HM###", tmpHM.toString());
		file_str = file_str.replace("###DATA_OP###", tmpOP.toString());
		file_str = file_str.replace("###CUR_OP###", cur_OP);
		file_str = file_str.replace("###CUR_HP###", cur_HP);
		
		try {
			FileWriter w = new FileWriter(PluginDashboard.INDEX_FILE);
			w.write(file_str);
			w.flush();
			w.close();
		} catch (IOException e) {
			plugin.getLogger().log(Level.SEVERE,"Cannot write index file",e);
		}
		plugin.getLogger().log(Level.INFO,"Finished task processing");
	}

}
