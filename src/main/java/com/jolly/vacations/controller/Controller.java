package com.jolly.vacations.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jolly.vacations.domain.User;
import com.jolly.vacations.model.LoginDTO;
import com.jolly.vacations.model.LoginStatusDTO;
import com.jolly.vacations.model.SignupDTO;
import com.jolly.vacations.service.ServiceClass;

@RestController
@CrossOrigin(origins = "*")
public class Controller {
	
	@Autowired
	ServiceClass service;

	@RequestMapping(value = "login")
	public LoginStatusDTO login(@RequestBody LoginDTO login) {

		return service.login(login);

	}
	
	@RequestMapping(value = "sendOTP")
	public Boolean sendOTP(String mobile) throws Exception {

		return service.sendOTP(mobile);

	}
	
	@RequestMapping(value = "verifyOTP")
	public LoginStatusDTO verifyOTP(String mail, String mobile, String password) throws Exception {

		return service.verifyOTP(mail, password, mobile);

	}
	
	@RequestMapping(value = "signup")
	public LoginStatusDTO signup(@RequestBody SignupDTO signup) throws Exception {
		return service.signup(signup);
	}
	
	@RequestMapping(value = "/getLoginDetails")
	public LoginStatusDTO getLoginDetails() throws Exception {
		return service.getLoginDetails();
		
	}


}
