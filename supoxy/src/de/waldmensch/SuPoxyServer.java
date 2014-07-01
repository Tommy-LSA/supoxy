package de.waldmensch;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


public class SuPoxyServer {

	public static ArrayList<SuPoxyDataObject> SunnyList;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		// we load the config file if given as parameter. If not we give a try to load it from working path
		if(args.length > 0){
			SuPoxySettings.LoadConfig(args[0]);
		} else {
			SuPoxySettings.LoadConfig("sunny.conf");
		}

		// only if the config is OK we can start
		if(SuPoxySettings.configOK){

			SunnyList = new ArrayList<SuPoxyDataObject>();
			new SuPoxyConnect("PortalConnector").start();

			HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
			server.createContext("/history", new SendHistory());
			server.createContext("/actual", new SendActual());
			server.setExecutor(null); // creates a default executor
			server.start();
			System.out.println("SuPoxy is running at  "+ SuPoxySettings.httpport);

		} else {

			System.out.println("SuPoxy is dead (config error)");

		}

	}

	/**
	 * 
	 * This function responds with the whole content of ArrayList. Oldest records first
	 * The number of records corosponds with the setting "cachesize"
	 *
	 */
	static class SendHistory implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {

			StringWriter sw = new StringWriter();

			for (SuPoxyDataObject data : SunnyList){
				DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
				String reportDate = df.format(data.getTimestamp());
				sw.write(reportDate + "\t");
				sw.write("PV:" + data.getPV() + "\t");
				sw.write("FI:" + data.getFeedIn() + "\t");
				sw.write("GC:" + data.getGridConsumption() + "\t");
				sw.write("DC:" + data.getDirectConsumption() + "\t");
				sw.write("SC:" + data.getSelfConsumption() + "\t");
				sw.write("SS:" + data.getSelfSupply() + "\t");
				sw.write("TC:" + data.getTotalConsumption() + "\t");
				sw.write("DCQ:" + data.getDirectConsumptionQuote() + "\t");
				sw.write("SCQ:" + data.getSelfConsumptionQuote() + "\t");
				sw.write("AC:" + data.getAutarkyQuote() + "\t");

				sw.write("BI:" + data.getBatteryIn() + "\t");
				sw.write("BO:" + data.getBatteryOut() + "\t");
				sw.write("BCS:" + data.getBatteryChargeStatus() + "\t");
				sw.write("BSH:" + data.getBatteryStateOfHealth() + "\t");

				if (data.getErrorMessages().length > 0)
					sw.write("ERROR:" + data.getErrorMessages()[0] + "\t");
				else
					sw.write("ERROR:");

				sw.write("\n");
			}

			String response = sw.toString();
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}

	/**
	 * 
	 * This function responds only with the last (newest) record in ArrayList
	 */
	static class SendActual implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {

			StringWriter sw = new StringWriter();

			SuPoxyDataObject data = SunnyList.get(SunnyList.size()-1);

			DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			String reportDate = df.format(data.getTimestamp());
			sw.write(reportDate + "\t");
			sw.write("PV:" + data.getPV() + "\t");
			sw.write("FI:" + data.getFeedIn() + "\t");
			sw.write("GC:" + data.getGridConsumption() + "\t");
			sw.write("DC:" + data.getDirectConsumption() + "\t");
			sw.write("SC:" + data.getSelfConsumption() + "\t");
			sw.write("SS:" + data.getSelfSupply() + "\t");
			sw.write("TC:" + data.getTotalConsumption() + "\t");
			sw.write("DCQ:" + data.getDirectConsumptionQuote() + "\t");
			sw.write("SCQ:" + data.getSelfConsumptionQuote() + "\t");
			sw.write("AC:" + data.getAutarkyQuote() + "\t");

			sw.write("BI:" + data.getBatteryIn() + "\t");
			sw.write("BO:" + data.getBatteryOut() + "\t");
			sw.write("BCS:" + data.getBatteryChargeStatus() + "\t");
			sw.write("BSH:" + data.getBatteryStateOfHealth() + "\t");

			if (data.getErrorMessages().length > 0)
				sw.write("ERROR:" + data.getErrorMessages()[0] + "\t");
			else
				sw.write("ERROR:");
			sw.write("\n");

			String response = sw.toString();
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}

}
