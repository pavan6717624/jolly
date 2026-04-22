package com.jolly.vacations.model;

import java.util.List;

import lombok.Data;
@Data
public class FuturesData {
	
	String name;
	Futures data;
	
	public FuturesData(List<MapData> mapData)
	{
		name=mapData.get(0).getSymbol();
		//data=new Futues(mapData);
	}

}
