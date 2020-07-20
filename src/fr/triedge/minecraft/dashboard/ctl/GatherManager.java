package fr.triedge.minecraft.dashboard.ctl;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.triedge.minecraft.dashboard.model.Metric;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

public class GatherManager {

	public static String getOnlinePlayers() {
		if (Bukkit.getOnlinePlayers().size() > 0) {
			String list = "";
			for (Player p : Bukkit.getOnlinePlayers()) {
				list += p.getName()+",";
			}
			return list;
		}else {
			return "-";
		}
	}
	
	public static Metric getMetricOnlinePlayer() {
		return new Metric(Metric.OP, getOnlinePlayers());
	}
	
	public static Metric getMetricHeapPercent() {
		String val = String.valueOf(getHeapUsagePercent());
		return new Metric(Metric.HP, val);
	}
	
	public static Metric getMetricHeapUsage() {
		String val = String.valueOf(getHeapUsage());
		return new Metric(Metric.HU, val);
	}
	
	public static Metric getMetricHeapMax() {
		String val = String.valueOf(getHeapMax());
		return new Metric(Metric.HM, val);
	}

	public static double getHeapUsagePercent() {
		MemoryUsage mem = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		return (mem.getUsed()*100)/mem.getMax();
	}
	
	public static double getHeapUsage() {
		MemoryUsage mem = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		return mem.getUsed()/1048576;
	}
	
	public static double getHeapMax() {
		MemoryUsage mem = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		return mem.getMax()/1048576;
	}
	
	public static long getAvailableMemory() {
		SystemInfo si = new SystemInfo();
		HardwareAbstractionLayer hal = si.getHardware();
		return hal.getMemory().getAvailable()/1048576;
	}
	
	public static long getTotalMemory() {
		SystemInfo si = new SystemInfo();
		HardwareAbstractionLayer hal = si.getHardware();
		return hal.getMemory().getTotal()/1048576;
	}
}
