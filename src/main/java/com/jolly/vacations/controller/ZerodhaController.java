package com.jolly.vacations.controller;

import java.net.*;
import java.util.*;
import java.sql.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import javax.annotation.PostConstruct;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.jolly.vacations.model.ZData;
import com.jolly.vacations.model.ZOIData;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "ZERODHA")
public class ZerodhaController {

	@RequestMapping(value = "demo")
	public String demo() {
		return "demo";

	}
	
	
	@Value("${zerodha.password}")
	private String password;
    
	@Value("${zerodha.user}")
	private String user;
    
	@Value("${zerodha.token}")
	private String token;
    
	@Value("${zerodha.today}")
	private String today;
    
	@Value("${zerodha.expiryDate}")
	private String expiryDate;
	
	static HttpURLConnection con=null;

	static RestTemplate template = new RestTemplate();
	private HttpHeaders headers = new HttpHeaders();
	private HttpEntity<String> entity = null;
	private List<String> instruments = null;

	static String stock="\"NIFTY\"";
	static int range=100;
	@PostConstruct
	public void init() {
		System.out.println("Details are :: "+token+"\n"+today+"\n"+expiryDate);
		headers.set("Authorization","enctoken "+token);
		headers.set("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		entity = new HttpEntity<String>(headers);
		instruments = getInstruments();
	}
	
	 private static String extractCookieValue(Map<String, List<String>> headers, String name) {
		    if (headers == null) return null;
		    for (Map.Entry<String, List<String>> e : headers.entrySet()) {
		        if (e.getKey() == null) continue;
		        if (!e.getKey().equalsIgnoreCase("Set-Cookie")) continue;
		        for (String headerVal : e.getValue()) {
		            for (String part : headerVal.split(";")) {
		                part = part.trim();
		                if (part.startsWith(name + "=")) {
		                    return part.substring((name + "=").length());
		                }
		            }
		        }
		    }
		    return null;
		}
	
	@RequestMapping(value = "getToken")
	public String getTokenDetails(@RequestParam("code") String code) throws Exception
	{
		 String baseUrl = "https://kite.zerodha.com/api/login";
		 String postParams = buildPostParams(user, password);
	        String response = postForm(baseUrl, postParams);
	        System.out.println("Final response:\n" + response);
	        
	        // extract request_id
	        String requestId = null;
	        try {
	            JSONObject jsonResponse = new JSONObject(response);
	            if (jsonResponse.has("data")) {
	                requestId = jsonResponse.getJSONObject("data").optString("request_id", null);
	            }
	            System.out.println("Extracted request_id: " + requestId);
	        } catch (Exception e) {
	            System.out.println("Failed to parse JSON response: " + e.getMessage());
	        }

	        // prompt user for 2FA code
	    

	        baseUrl = "https://kite.zerodha.com/api/twofa";
	        try {
	            String twofaPostParams = "user_id=" + java.net.URLEncoder.encode("IO7052", "UTF-8")
	                    + "&request_id=" + java.net.URLEncoder.encode(requestId == null ? "" : requestId, "UTF-8")
	                    + "&twofa_type=app_code"
	                    + "&twofa_value=" + java.net.URLEncoder.encode(code, "UTF-8");
	            String twofaResponse = postForm(baseUrl, twofaPostParams);
	            System.out.println("TwoFA response:\n" + twofaResponse);
	        } catch (Exception e) {
	            System.out.println("Failed to send twofa request: " + e.getMessage());
	        }
	        
	        String enctoken = extractCookieValue(con.getHeaderFields(), "enctoken");
	        System.out.println("enctoken: " + enctoken);
	        
		return enctoken;
	}
	
	  private static String buildPostParams(String userId, String password) {
	        try {
	            return "user_id=" + java.net.URLEncoder.encode(userId, "UTF-8")
	                    + "&password=" + java.net.URLEncoder.encode(password, "UTF-8")
	                    + "&type=phone";
	        } catch (Exception e) {
	            // fallback (shouldn't happen)
	            return "user_id=" + userId + "&password=" + password + "&type=phone";
	        }
	    }
	  
	  private static String postForm(String baseUrl, String postParams) throws IOException {
	        URL url = new URL(baseUrl);
	        con = (HttpURLConnection) url.openConnection();
	        con.setConnectTimeout(CONNECT_TIMEOUT);
	        con.setReadTimeout(READ_TIMEOUT);
	        con.setDoOutput(true);
	        con.setRequestMethod("POST");
	        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
	        con.setRequestProperty("Accept", "application/json, text/plain, */*");
	        con.setRequestProperty("Accept-Encoding", "gzip, deflate");
	        con.setRequestProperty("Accept-Language", "en-US,en;q=0.9");

	        byte[] out = postParams.getBytes(StandardCharsets.UTF_8);
	        con.setRequestProperty("Content-Length", String.valueOf(out.length));

	        try (OutputStream os = con.getOutputStream()) {
	            os.write(out);
	        }

	        int status = con.getResponseCode();
	        System.out.println("HTTP Response Code: " + status);

	        // print key headers for debugging
	        for (Map.Entry<String, List<String>> e : con.getHeaderFields().entrySet()) {
	            System.out.println(e.getKey() + ": " + e.getValue());
	        }

	        InputStream responseStream = status >= 400 ? con.getErrorStream() : con.getInputStream();
	        if (responseStream == null) {
	            con.disconnect();
	            return "";
	        }

	        String contentEncoding = con.getHeaderField("Content-Encoding");
	        if (contentEncoding != null) contentEncoding = contentEncoding.toLowerCase();
	        if ("gzip".equals(contentEncoding)) {
	            responseStream = new GZIPInputStream(responseStream);
	        } else if ("deflate".equals(contentEncoding)) {
	            responseStream = new InflaterInputStream(responseStream);
	        }

	        StringBuilder sb = new StringBuilder();
	        try (BufferedReader br = new BufferedReader(new InputStreamReader(responseStream, StandardCharsets.UTF_8))) {
	            String line;
	            while ((line = br.readLine()) != null) {
	                sb.append(line).append('\n');
	            }
	        } finally {
	            con.disconnect();
	        }

	        return sb.toString();
	    }
	
	



	private List<String> getInstruments() {

		String output = template.exchange("https://api.kite.trade/instruments", HttpMethod.GET, entity, String.class)
				.getBody();

		List<String> instruments = Arrays.asList(output.split("\n"));
		return instruments;
	}

	
	
	@RequestMapping(value = "getData")
	public List<ZOIData> getData(@RequestParam("instrument") String instrument) {
		String call[] = { "CE", "PE" };
		
		  //NIFTY = 256265
		  //SENSEX = 265
		 

		  //RELIANCE=738561
		  //HDFCBANK=341249
		  //ICICIBANK=1270529
		  //SBIN = 779521
		  //INFY = 408065
	
		if(instrument.equals("256265"))
		{
			range=50;
			stock="NIFTY";

			// expiryDate="2026-06-02";

			System.out.println("range is "+range);
		}
		else if(instrument.equals("265"))
		{
			range=100;
			stock="SENSEX";
			expiryDate="2026-02-19";
			System.out.println("range is "+range);
		}
		else if(instrument.equals("779521"))
		{
			range=10;
			stock="SBI";
			expiryDate="2026-02-23";
			System.out.println("range is "+range);
		}
		
		Double lastPrice = getLastPrice(instrument);
		
		System.out.println(lastPrice);
//		
//		if(1==1)
//			return null;
//		
		int candlesSize = 400;
		System.out.println(lastPrice);
		List<String> mapids = getCEPE(lastPrice);
		System.out.println(mapids);
		
		
		
		
		List<ZOIData> OIDataList = new ArrayList<>();

		for (int j = 0; j < mapids.size(); j++) {

			String spiltData[] = mapids.get(j).split(",");

			for (int k = 0; k < 2; k++) {

				ZOIData oiData = new ZOIData();
				oiData.setPrice(Long.valueOf(spiltData[0]));
				oiData.setInstrument(Long.valueOf(spiltData[1 + k]));
				oiData.setCall(call[k]);

				String output = template.exchange(
						"https://kite.zerodha.com/oms/instruments/historical/" + oiData.getInstrument()
								+ "/minute?user_id=IO7052&oi=1&from="+today+"&to="+today,
						HttpMethod.GET, entity, String.class).getBody();

				// System.out.println(output);

				int index = output.indexOf("candles");
				int index1 = output.indexOf("[[", index);
				String dataStr = output.toString().substring(index1 + 2).replace("],[", "\n").replace("]]}}", "");
				String candles[] = dataStr.split("\n");

				int startFrom = candles.length - candlesSize;
				if (startFrom < 0)
					startFrom = 0;

				List<ZData> data = new ArrayList<>();

				for (int i = startFrom; i < candles.length; i++) {
					
					try
					{
					String date1;
					Double open1, high1, close1, low1, volume1, oi1;
					date1 = candles[i].split(",")[0];
					open1 = Double.parseDouble(candles[i].split(",")[1]);
					high1 = Double.parseDouble(candles[i].split(",")[2]);
					low1 = Double.parseDouble(candles[i].split(",")[3]);
					close1 = Double.parseDouble(candles[i].split(",")[4]);
					volume1 = Double.parseDouble(candles[i].split(",")[5]);
					oi1 = Double.parseDouble(candles[i].split(",")[6]);
					data.add(new ZData(date1, open1, high1, low1, close1, volume1, oi1));
					}
					catch(Exception ex)
					{
						continue;
					}
				}

				oiData.setClose(data.stream().map(o -> o.getClose()).collect(Collectors.toList()));
				oiData.setOpen(data.stream().map(o -> o.getOpen()).collect(Collectors.toList()));
				oiData.setHigh(data.stream().map(o -> o.getHigh()).collect(Collectors.toList()));
				oiData.setLow(data.stream().map(o -> o.getLow()).collect(Collectors.toList()));
				oiData.setOi(data.stream().map(o -> (o.getOi())).collect(Collectors.toList()));
				oiData.setVol(data.stream().map(o -> o.getVolume()).collect(Collectors.toList()));
				oiData.setDate(data.stream().map(o -> o.getDate().replaceAll("\"", "").substring(11, 19))
						.collect(Collectors.toList()));

				OIDataList.add(oiData);
			}

		}

		List<ZOIData> CEOIDataList = OIDataList.stream().filter(o -> o.getCall().equals("CE"))
				.collect(Collectors.toList());
		List<ZOIData> PEOIDataList = OIDataList.stream().filter(o -> o.getCall().equals("PE"))
				.collect(Collectors.toList());
		
		System.out.println(CEOIDataList);

		List<Double> ceSumOis = new ArrayList<>();
		for (int i = 0; i < candlesSize; i++) {
			Double sumOi = 0d;
			for (int j = 0; j < CEOIDataList.size(); j++) {
				try
				{
				sumOi += CEOIDataList.get(j).getOi().get(i);
				}
				catch(Exception ex)
				{
					
				}
			}

			ceSumOis.add(sumOi);

		}

		List<Double> peSumOis = new ArrayList<>();
		for (int i = 0; i < candlesSize; i++) {
			Double sumOi = 0d;
			for (int j = 0; j < PEOIDataList.size(); j++) {
				try
				{
				sumOi += PEOIDataList.get(j).getOi().get(i);
				}
				catch(Exception ex)
				{
					
				}
			}

			peSumOis.add(sumOi);

		}

		ZOIData oiData = new ZOIData();
		oiData.setCall("CE");
		oiData.setOi(ceSumOis);
		oiData.setDate(CEOIDataList.get(0).getDate());
		OIDataList.add(oiData);

		oiData = new ZOIData();
		oiData.setCall("PE");
		oiData.setOi(peSumOis);
		oiData.setDate(CEOIDataList.get(0).getDate());
		OIDataList.add(oiData);

		return OIDataList;
	}
	
	

    private static final int CONNECT_TIMEOUT = 15_000;
    private static final int READ_TIMEOUT = 15_000;
	
	

	@RequestMapping(value = "getStockData")
	public ZOIData getStockData(@RequestParam("instrument") String instrument) {

		ZOIData oiData = new ZOIData();

		String output = template.exchange(
				"https://kite.zerodha.com/oms/instruments/historical/" + instrument
						+ "/day?user_id=IO7052&oi=1&from=2024-09-01&to=2024-11-07",
				HttpMethod.GET, entity, String.class).getBody();

		// System.out.println(output);

		int index = output.indexOf("candles");
		int index1 = output.indexOf("[[", index);
		String dataStr = output.toString().substring(index1 + 2).replace("],[", "\n").replace("]]}}", "");
		String candles[] = dataStr.split("\n");

		int startFrom = 0;
		if (startFrom < 0)
			startFrom = 0;

		List<ZData> data = new ArrayList<>();

		for (int i = startFrom; i < candles.length; i++) {

			String date1;
			Double open1, high1, close1, low1, volume1, oi1;
			date1 = candles[i].split(",")[0];
			open1 = Double.parseDouble(candles[i].split(",")[1]);
			high1 = Double.parseDouble(candles[i].split(",")[2]);
			low1 = Double.parseDouble(candles[i].split(",")[3]);
			close1 = Double.parseDouble(candles[i].split(",")[4]);
			volume1 = Double.parseDouble(candles[i].split(",")[5]);
			oi1 = Double.parseDouble(candles[i].split(",")[6]);
			data.add(new ZData(date1, open1, high1, low1, close1, volume1, oi1));
		}

		oiData.setClose(data.stream().map(o -> o.getClose()).collect(Collectors.toList()));
		oiData.setOpen(data.stream().map(o -> o.getOpen()).collect(Collectors.toList()));
		oiData.setHigh(data.stream().map(o -> o.getHigh()).collect(Collectors.toList()));
		oiData.setLow(data.stream().map(o -> o.getLow()).collect(Collectors.toList()));
		oiData.setOi(data.stream().map(o -> (o.getOi()/20)).collect(Collectors.toList()));
		oiData.setVol(data.stream().map(o -> o.getVolume()).collect(Collectors.toList()));
		oiData.setDate(data.stream().map(o -> o.getDate()).collect(Collectors.toList()));

		return oiData;
	}

	@RequestMapping(value = "getLastPrice")
	public Double getLastPrice(@RequestParam("instrument") String instrument) {

		String output = template.exchange(
				"https://kite.zerodha.com/oms/instruments/historical/" + instrument
						+ "/minute?user_id=IO7052&oi=1&from="+today+"&to="+today,
				HttpMethod.GET, entity, String.class).getBody();

		System.out.println(output);

		int index = output.indexOf("candles");
		int index1 = output.indexOf("[[", index);
		String dataStr = output.toString().substring(index1 + 2).replace("],[", "\n").replace("]]}}", "");
		String candles[] = dataStr.split("\n");
		return Double.parseDouble(candles[candles.length - 1].split(",")[4]);
	}

	@RequestMapping(value = "getCEPE")
	public List<String> getCEPE(@RequestParam("lastPrice") Double lastPrice) {

		Long price = Math.round(lastPrice / range) * range;

		List<Long> prices = new ArrayList<>();
		List<String> data = new ArrayList<>();

		for (int i = 0; i < 8; i++) {
			Long p = price - i * range;
			prices.add(p);
		}

		for (int i = 0; i < 8; i++) {
			Long p = price + i * range;
			prices.add(p);
		}

		prices = prices.stream().distinct().sorted().collect(Collectors.toList());
		
//		System.out.println("instruments" + instruments);

		for (int i = 0; i < prices.size(); i++) {
			String pricestr = prices.get(i) + "";
			
System.out.println("Stock is "+ stock+" expiryDate is "+ expiryDate);
			
			String CE = (instruments.stream()
					.filter(o -> o.indexOf(stock) != -1 && o.indexOf(expiryDate) != -1
							&& o.indexOf(pricestr) != -1 && o.indexOf("CE") != -1)
					.collect(Collectors.toList()).get(0)).split(",")[0];
			String PE = (instruments.stream()
					.filter(o -> o.indexOf(stock) != -1 && o.indexOf(expiryDate) != -1
							&& o.indexOf(pricestr) != -1 && o.indexOf("PE") != -1)
					.collect(Collectors.toList()).get(0)).split(",")[0];

			data.add(pricestr + "," + CE + "," + PE);
		}
		return data;
	}

	@RequestMapping(value = "getInstrumentToken")
	public Boolean getInstrumentToken(@RequestParam("sname") String sname) {

		String instrumentToken = instruments.stream()
				.filter(o -> o.indexOf("," + sname + ",") != -1 && o.indexOf("EQ") != -1 && o.indexOf("NSE") != -1)
				.collect(Collectors.toList()).get(0);

		return checkPatter(getStockData(instrumentToken.split(",")[0]));
	}

	public Boolean checkPatter(ZOIData data) {

		List<Double> highs = data.getHigh();
		List<Double> lows = data.getLow();
		List<String> dates = data.getDate();

		List<String> lowTrend = new ArrayList<>();
		List<String> highTrend = new ArrayList<>();
		List<String> noTrend = new ArrayList<>();

		Double low = lows.get(lows.size() - 1), high = highs.get(highs.size() - 1);

		for (int i = lows.size() - 1; i >= 1; i--) {
			if (highs.get(i) >= highs.get(i - 1) && lows.get(i) >= lows.get(i - 1)) {

				highTrend.add(highs.get(i) + " " + highs.get(i - 1) + " " + lows.get(i) + " " + lows.get(i - 1) + " "
						+ dates.get(i) + " " + i);

			}

			else if (highs.get(i) <= highs.get(i-1) && lows.get(i) <= lows.get(i-1)) {
				lowTrend.add(highs.get(i) + " " + highs.get(i - 1) + " " + lows.get(i) + " " + lows.get(i - 1) + " "
						+ dates.get(i) + " " + i);

			}

			else
				noTrend.add(highs.get(i) +" "+ highs.get(i-1) +" "+ lows.get(i) +" "+ lows.get(i-1) +" "+ dates.get(i) + " " + i);

		}

		System.out.println(highTrend + "\n\n" + lowTrend + "\n\n\n" + noTrend);

		return null;

	}

}
