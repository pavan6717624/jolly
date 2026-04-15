package com.jolly.vacations.model;

import com.jolly.vacations.domain.JollyLocation;

import lombok.Data;

@Data
public class JollyLocationDTO {

	String locationName;
	String oldLocationName;
	Double price;
	Boolean status;
	String message;

	public JollyLocationDTO() {

	}

	public JollyLocationDTO(JollyLocation location) {

		this.locationName = location.getLocationName();
		this.price = location.getPrice();
		this.status = true;
		this.message = "Success";
	}
}
