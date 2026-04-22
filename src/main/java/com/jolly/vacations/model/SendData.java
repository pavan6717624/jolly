package com.jolly.vacations.model;

import java.util.List;

import lombok.Data;

@Data
public class SendData {

	String symbol;
	List<Double> coi;
	List<Double> cchoi;
	List<Double> poi;
	List<Double> pchoi;
	List<String> date;
	List<Double> price;
}
