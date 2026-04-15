package com.jolly.vacations.model;

import java.time.LocalDate;

import com.jolly.vacations.domain.JollyTrip;

import lombok.Data;

@Data
public class JollyTripDTO {

	String locationName;
	LocalDate fromDate, toDate;
	Boolean status;
	String message;

	public JollyTripDTO() {

	}

	public JollyTripDTO(JollyTrip trip) {

		this.locationName = trip.getLocation().getLocationName();
		this.fromDate = trip.getFromDate();
		this.toDate = trip.getToDate();
		this.status = true;
		this.message = "Success";
	}
}
