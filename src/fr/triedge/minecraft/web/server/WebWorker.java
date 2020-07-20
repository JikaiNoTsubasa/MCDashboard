package fr.triedge.minecraft.web.server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.StringTokenizer;

import fr.triedge.minecraft.web.model.Metric;

public class WebWorker implements Runnable{

	private Socket socket;
	protected boolean verbose = true;
	private File metricFile;

	public WebWorker(Socket socket, File file) {
		setSocket(socket);
		setMetricFile(file);
	}
	
	public WebWorker(Socket socket) {
		setSocket(socket);
	}

	@Override
	public void run() {
		// we manage our particular client connection
		BufferedReader in = null; PrintWriter out = null; BufferedOutputStream dataOut = null;
		String fileRequested = null;

		try {
			// we read characters from the client via input stream on the socket
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// we get character output stream to client (for headers)
			out = new PrintWriter(socket.getOutputStream());
			// get binary output stream to client (for requested data)
			dataOut = new BufferedOutputStream(socket.getOutputStream());

			// get first line of the request from the client
			String input = in.readLine();
			// we parse the request with a string tokenizer
			StringTokenizer parse = new StringTokenizer(input);
			String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
			// we get file requested
			fileRequested = parse.nextToken().toLowerCase();
			System.out.println("File: "+fileRequested);

			// we support only GET and HEAD methods, we check
			if (!method.equals("GET")  &&  !method.equals("HEAD")) {
				if (verbose) {
					System.out.println("501 Not Implemented : " + method + " method.");
				}

				// we return the not supported file to the client
				File file = new File(WebServer.WEB_ROOT, WebServer.METHOD_NOT_SUPPORTED);
				int fileLength = (int) file.length();
				String contentMimeType = "text/html";
				//read content to return to client
				byte[] fileData = readFileData(file, fileLength);

				// we send HTTP Headers with data to client
				out.println("HTTP/1.1 501 Not Implemented");
				out.println("Server: Java HTTP Server from SSaurel : 1.0");
				out.println("Date: " + new Date());
				out.println("Content-type: " + contentMimeType);
				out.println("Content-length: " + fileLength);
				out.println(); // blank line between headers and content, very important !
				out.flush(); // flush character output stream buffer
				// file
				dataOut.write(fileData, 0, fileLength);
				dataOut.flush();

			} else {
				if (fileRequested.endsWith("/")) {
					printDefault(in, out, dataOut, fileRequested, method);
					
				}else if (fileRequested.equals("/metrics")) {
					System.out.println("Display metrics");
					printMetrics(in, out, dataOut, fileRequested, method);
				}

			}

		} catch (FileNotFoundException fnfe) {
			try {
				fileNotFound(out, dataOut, fileRequested);
			} catch (IOException ioe) {
				System.err.println("Error with file not found exception : " + ioe.getMessage());
			}

		} catch (IOException ioe) {
			System.err.println("Server error : " + ioe);
		} finally {
			try {
				in.close();
				out.close();
				dataOut.close();
				socket.close(); // we close socket connection
			} catch (Exception e) {
				System.err.println("Error closing stream : " + e.getMessage());
			} 

			if (verbose) {
				System.out.println("Connection closed.\n");
			}
		}

	}
	
	private void printMetrics(BufferedReader in, PrintWriter out, BufferedOutputStream dataOut, String fileRequested,
			String method) throws IOException {
		File data = getMetricFile();
		Scanner scan = new Scanner(data);
		ArrayList<String> labels = new ArrayList<String>();
		ArrayList<String> HUs = new ArrayList<>();
		ArrayList<Metric> metrics = new ArrayList<Metric>();
		while(scan.hasNext()) {
			String line = scan.nextLine();
			String[] elements = line.split(" ");
			Metric met = new Metric();
			HUs.add(elements[0].split(":")[1]);
			met.HU = Float.parseFloat(elements[0].split(":")[1]);
			met.HM = Float.parseFloat(elements[1].split(":")[1]);
			met.HP = Float.parseFloat(elements[2].split(":")[1]);
			met.OP = elements[3].split(":")[1];
			String time = elements[4];
			labels.add(time);
			SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
			try {
				met.date = format.parse(time);
				
			} catch (ParseException e) {
				e.printStackTrace();
			}
			metrics.add(met);
		}
		scan.close();
		
		StringBuilder tmp = new StringBuilder();
		for (String s: HUs) {
			tmp.append(s);
			tmp.append(",");
		}
		String data_concats = tmp.toString();
		
		StringBuilder tmp2 = new StringBuilder();
		for(String s : labels) {
			tmp2.append("'");
			tmp2.append(s);
			tmp2.append("'");
			tmp2.append(",");
		}
		String labels_concats = tmp2.toString();
		
		File template = new File("metric.html");
		Scanner scan2 = new Scanner(template);
		FileWriter w = new FileWriter(new File("metric_tmp.html"));
		while(scan2.hasNext()) {
			String line = scan2.nextLine();
			line = line.replace("###DATA###", data_concats);
			line = line.replace("###LABELS###", labels_concats);
			w.write(line+"\r\n");
		}
		w.flush();
		scan2.close();
		w.close();
		
		File file = new File("metric_tmp.html");
		int fileLength = (int) file.length();
		String content = getContentType(fileRequested);

		if (method.equals("GET")) { // GET method so we return content
			byte[] fileData = readFileData(file, fileLength);

			// send HTTP Headers
			out.println("HTTP/1.1 200 OK");
			out.println("Server: Java HTTP Server from SSaurel : 1.0");
			out.println("Date: " + new Date());
			out.println("Content-type: " + content);
			out.println("Content-length: " + fileLength);
			out.println(); // blank line between headers and content, very important !
			out.flush(); // flush character output stream buffer

			dataOut.write(fileData, 0, fileLength);
			dataOut.flush();
		}

		if (verbose) {
			System.out.println("File " + fileRequested + " of type " + content + " returned");
		}
	}

	public void printDefault(BufferedReader in, PrintWriter out, BufferedOutputStream dataOut, String fileRequested, String method) throws IOException {
		// GET or HEAD method
		if (fileRequested.endsWith("/")) {
			fileRequested += WebServer.DEFAULT_FILE;
		}

		File file = new File(WebServer.WEB_ROOT, fileRequested);
		int fileLength = (int) file.length();
		String content = getContentType(fileRequested);

		if (method.equals("GET")) { // GET method so we return content
			byte[] fileData = readFileData(file, fileLength);

			// send HTTP Headers
			out.println("HTTP/1.1 200 OK");
			out.println("Server: Java HTTP Server from SSaurel : 1.0");
			out.println("Date: " + new Date());
			out.println("Content-type: " + content);
			out.println("Content-length: " + fileLength);
			out.println(); // blank line between headers and content, very important !
			out.flush(); // flush character output stream buffer

			dataOut.write(fileData, 0, fileLength);
			dataOut.flush();
		}

		if (verbose) {
			System.out.println("File " + fileRequested + " of type " + content + " returned");
		}
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	private byte[] readFileData(File file, int fileLength) throws IOException {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];

		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if (fileIn != null) 
				fileIn.close();
		}

		return fileData;
	}

	// return supported MIME Types
	private String getContentType(String fileRequested) {
		if (fileRequested.endsWith(".htm")  ||  fileRequested.endsWith(".html"))
			return "text/html";
		else
			return "text/plain";
	}

	private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
		File file = new File(WebServer.WEB_ROOT, WebServer.FILE_NOT_FOUND);
		int fileLength = (int) file.length();
		String content = "text/html";
		byte[] fileData = readFileData(file, fileLength);

		out.println("HTTP/1.1 404 File Not Found");
		out.println("Server: Java HTTP Server from SSaurel : 1.0");
		out.println("Date: " + new Date());
		out.println("Content-type: " + content);
		out.println("Content-length: " + fileLength);
		out.println(); // blank line between headers and content, very important !
		out.flush(); // flush character output stream buffer

		dataOut.write(fileData, 0, fileLength);
		dataOut.flush();

		if (verbose) {
			System.out.println("File " + fileRequested + " not found");
		}
	}

	public File getMetricFile() {
		return metricFile;
	}

	public void setMetricFile(File metricFile) {
		this.metricFile = metricFile;
	}

}
