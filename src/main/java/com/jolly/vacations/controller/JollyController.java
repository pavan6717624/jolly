package com.jolly.vacations.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jolly.vacations.domain.JollyUser;
import com.jolly.vacations.model.JollyLoginDTO;
import com.jolly.vacations.model.JollyLoginStatusDTO;
import com.jolly.vacations.model.JollySignupDTO;
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
	
	@RequestMapping(value = "signup")
	public JollyLoginStatusDTO signup(@RequestBody JollySignupDTO signup) throws Exception {
		return service.signup(signup);
	}
	
	@RequestMapping(value = "/getLoginDetails")
	public JollyLoginStatusDTO getLoginDetails() throws Exception {
		return service.getLoginDetails();
		
	}


}
