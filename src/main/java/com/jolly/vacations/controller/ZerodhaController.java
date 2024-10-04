package com.jolly.vacations.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

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

	static RestTemplate template = new RestTemplate();
	static HttpHeaders headers = new HttpHeaders();
	static HttpEntity<String> entity = null;
	static List<String> instruments = null;

	{
		headers.set("Authorization",
				"enctoken 5KIYI3gcZ+VJp7l4l2L6JIINxsokaODIoVjMCmJ4C7OW5CNz3zJdowga9vcc+ZlQmv6SZ1LW91oCzyWLQj/MYkijzeMg0UYyV9FeZvPgrnog8fvOefPxPg==");
		headers.set("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		entity = new HttpEntity<String>(headers);
		instruments = getInstruments();
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
		Double lastPrice = getLastPrice(instrument);
		int candlesSize = 200;
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
								+ "/minute?user_id=IO7052&oi=1&from=2024-09-30&to=2024-10-10",
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
				oiData.setOi(data.stream().map(o -> (o.getOi() / 25)).collect(Collectors.toList()));
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

		List<Double> ceSumOis = new ArrayList<>();
		for (int i = 0; i < candlesSize; i++) {
			Double sumOi = 0d;
			for (int j = 0; j < CEOIDataList.size(); j++) {
				sumOi += CEOIDataList.get(j).getOi().get(i);
			}

			ceSumOis.add(sumOi);

		}

		List<Double> peSumOis = new ArrayList<>();
		for (int i = 0; i < candlesSize; i++) {
			Double sumOi = 0d;
			for (int j = 0; j < PEOIDataList.size(); j++) {
				sumOi += PEOIDataList.get(j).getOi().get(i);
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

	@RequestMapping(value = "getStockData")
	public ZOIData getStockData(@RequestParam("instrument") String instrument) {

		ZOIData oiData = new ZOIData();

		String output = template.exchange(
				"https://kite.zerodha.com/oms/instruments/historical/" + instrument
						+ "/day?user_id=IO7052&oi=1&from=2024-09-01&to=2024-10-10",
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
		oiData.setOi(data.stream().map(o -> (o.getOi() / 25)).collect(Collectors.toList()));
		oiData.setVol(data.stream().map(o -> o.getVolume()).collect(Collectors.toList()));
		oiData.setDate(data.stream().map(o -> o.getDate()).collect(Collectors.toList()));

		return oiData;
	}

	@RequestMapping(value = "getLastPrice")
	public Double getLastPrice(@RequestParam("instrument") String instrument) {

		String output = template.exchange(
				"https://kite.zerodha.com/oms/instruments/historical/" + instrument
						+ "/minute?user_id=IO7052&oi=1&from=2024-09-30&to=2024-10-10",
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

		Long price = Math.round(lastPrice / 100) * 100;

		List<Long> prices = new ArrayList<>();
		List<String> data = new ArrayList<>();

		for (int i = 0; i < 12; i++) {
			Long p = price - i * 50;
			prices.add(p);
		}

		for (int i = 0; i < 12; i++) {
			Long p = price + i * 50;
			prices.add(p);
		}

		prices = prices.stream().distinct().sorted().collect(Collectors.toList());

		for (int i = 0; i < prices.size(); i++) {
			String pricestr = prices.get(i) + "";
			String CE = (instruments.stream()
					.filter(o -> o.indexOf("\"NIFTY\"") != -1 && o.indexOf("2024-10-10") != -1
							&& o.indexOf(pricestr) != -1 && o.indexOf("CE") != -1)
					.collect(Collectors.toList()).get(0)).split(",")[0];
			String PE = (instruments.stream()
					.filter(o -> o.indexOf("\"NIFTY\"") != -1 && o.indexOf("2024-10-10") != -1
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
