package fr.triedge.minecraft.web.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

import org.apache.commons.cli.CommandLine;


public class WebServer implements Runnable{
	
	public static final int DEFAULT_PORT								= 8080;
	public static final File WEB_ROOT 									= new File(".");
	public static final String DEFAULT_FILE 							= "index.html";
	public static final String FILE_NOT_FOUND 							= "404.html";
	public static final String METHOD_NOT_SUPPORTED 					= "not_supported.html";
	
	private boolean running = true;
	private CommandLine cmd;
	
	private int port;
	
	public void init(String[] args) {

	}
	
	public void setPort(int port) {
		this.port = port;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public static void main(String[] args) {
		WebServer server = new WebServer();
		server.init(args);
	}

	@Override
	public void run() {
		try {
			ServerSocket serverConnect = new ServerSocket(port);
			System.out.println("Listening on port: "+port);
			while (isRunning()) {
				WebWorker worker = new WebWorker(serverConnect.accept());
				
				// create dedicated thread to manage the client connection
				Thread thread = new Thread(worker);
				thread.start();
			}
			serverConnect.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
