package com.jolly.vacations.model;

import java.util.List;

import lombok.Data;

@Data
public class ZOIData {

	Long price;
	Long instrument;
	String call;
	
	List<Double> close, open, high, low, oi, vol;
	List<String> date;
	
	
}
