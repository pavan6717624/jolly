package com.jolly.vacations.model;

import lombok.Data;

@Data
public class DropDown {

	String name, code;

	public DropDown(String name, String code) {
		this.name = name;
		this.code = code;
	}

}
