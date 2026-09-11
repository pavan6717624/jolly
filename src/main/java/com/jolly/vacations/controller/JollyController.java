package com.jolly.vacations.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jolly.vacations.model.DistributeRewardsDTO;
import com.jolly.vacations.model.DropDown;
import com.jolly.vacations.model.JollyCalendarDTO;
import com.jolly.vacations.model.JollyCustomerDTO;
import com.jolly.vacations.model.JollyLocationDTO;
import com.jolly.vacations.model.JollyLoginDTO;
import com.jolly.vacations.model.JollyLoginStatusDTO;
import com.jolly.vacations.model.JollyScheduleDTO;
import com.jolly.vacations.model.JollySignupDTO;
import com.jolly.vacations.model.JollyTripDTO;
import com.jolly.vacations.service.JollyServiceClass;

@RestController
@CrossOrigin(origins = "*")
public class JollyController {
	
	@Autowired
	JollyServiceClass service;
	
	@RequestMapping(value = "login")
	public JollyLoginStatusDTO login(@RequestBody JollyLoginDTO login) {

		return service.login(login);

	}

	@RequestMapping(value = "sendOTP")
	public Boolean sendOTP(String mobile) throws Exception {

		return service.sendOTP(mobile);

	}

	@RequestMapping(value = "verifyOTP")
	public JollyLoginStatusDTO verifyOTP(String mail, String mobile, String password) throws Exception {

		return service.verifyOTP(mail, password, mobile);

	}

	@RequestMapping(value = "removeFromTrip")
	public Boolean removeFromTrip(String locationName, String trip, String customer) throws Exception {

		return service.removeFromTrip(locationName, trip, customer);

	}

	@RequestMapping(value = "signup")
	public JollyLoginStatusDTO signup(@RequestBody JollySignupDTO signup) throws Exception {
		return service.signup(signup);
	}

	@RequestMapping(value = "/getLoginDetails")
	public JollyLoginStatusDTO getLoginDetails() throws Exception {
		return service.getLoginDetails();

	}

	@RequestMapping(value = "/addLocation")
	public JollyLocationDTO addLocation(@RequestBody JollyLocationDTO locationDTO) throws Exception {
		return service.addLocation(locationDTO);

	}

	@RequestMapping(value = "/addCustomer")
	public JollyCustomerDTO addCustomer(@RequestBody JollyCustomerDTO customerDTO) throws Exception {
		return service.addCustomer(customerDTO);

	}

	@RequestMapping(value = "/addSchedule")
	public JollyScheduleDTO addSchedule(@RequestBody JollyScheduleDTO scheduleDTO) throws Exception {
		return service.addSchedule(scheduleDTO);

	}

	@RequestMapping(value = "/getSchedules")
	public List<JollyCalendarDTO> getSchedules() throws Exception {
		return service.getSchedules();

	}

	@RequestMapping(value = "/getCustomerTrips")
	public List<JollyCalendarDTO> getCustomerTrips() throws Exception {
		
		String mobile=service.getUserName();
		
//		System.out.println(mobile+" reached");
		
		List<JollyCalendarDTO> trips=service.getSchedules();
//		
//		for(int i=0;i<trips.size();i++)
//			if(trips.get(i).getCustomerDetails()!=null)
//			System.out.println(i+" :: "+trips.get(i).getCustomerDetails()+" :: "+trips.get(i).getCustomerDetails().split("-")[3].trim()+" done");
		
		
		
		return trips.stream().filter(o -> o != null && o.getCustomerDetails() != null
				&& o.getCustomerDetails().contains("-"+mobile)).collect(Collectors.toList());

	}

	@RequestMapping(value = "/editCustomer")
	public JollyCustomerDTO editCustomer(@RequestBody JollyCustomerDTO customerDTO) throws Exception {
		return service.editCustomer(customerDTO);

	}

	@RequestMapping(value = "/deleteCustomer")
	public JollyCustomerDTO deleteCustomer(@RequestBody JollyCustomerDTO customerDTO) throws Exception {
		return service.deleteCustomer(customerDTO);

	}

	@RequestMapping(value = "/addTrip")
	public JollyTripDTO addTrip(@RequestBody JollyTripDTO tripDTO) throws Exception {
		return service.addTrip(tripDTO);

	}

	@RequestMapping(value = "/deleteTrip")
	public JollyTripDTO deleteTrip(@RequestBody JollyTripDTO tripDTO) throws Exception {
		return service.deleteTrip(tripDTO);

	}

	@RequestMapping(value = "/editLocation")
	public JollyLocationDTO editLocation(@RequestBody JollyLocationDTO locationDTO) throws Exception {
		return service.editLocation(locationDTO);

	}

	@RequestMapping(value = "/getLocations")
	public List<JollyLocationDTO> getLocations() throws Exception {
		return service.getLocations();

	}

	@RequestMapping(value = "/getTrips")
	public List<JollyTripDTO> getTrips() throws Exception {
		return service.getTrips();

	}

	@RequestMapping(value = "/getCustomers")
	public List<JollyCustomerDTO> getCustomers() throws Exception {
		List<JollyCustomerDTO> dto= service.getCustomers();
		System.out.println(dto);
		return dto;

	}

	@RequestMapping(value = "/distributeRewards")
	public JollyCustomerDTO distributeRewards(@RequestBody DistributeRewardsDTO payload) throws Exception {
		System.out.println("distributeRewards payload :: "+payload);
		return service.distributeRewards(payload);
	}

	@RequestMapping(value = "/getCustomersDropDown")
	public List<DropDown> getCustomersDropDown() throws Exception {
		return service.getCustomersDropDown();

	}

	@RequestMapping(value = "/getLocationDropDown")
	public List<DropDown> getLocationDropDown() throws Exception {
		return service.getLocationDropDown();

	}

	@RequestMapping(value = "/deleteLocation")
	public Boolean deleteLocation(String locationName) throws Exception {
		return service.deleteLocation(locationName);

	}

}
