package com.jolly.vacations.service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.jolly.vacations.domain.User;
import com.jolly.vacations.jwt.JwtTokenUtil;
import com.jolly.vacations.model.LoginDTO;
import com.jolly.vacations.model.LoginStatusDTO;
import com.jolly.vacations.model.SignupDTO;
import com.jolly.vacations.repository.RoleRepository;
import com.jolly.vacations.repository.UserRepository;

@Service
public class ServiceClass {

	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	UserRepository userRepository;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtUserDetailsService userDetailsService;

	@Autowired
	RoleRepository roleRepository;

	public Boolean sendOTP(String mobile) throws Exception {
		try {

			User user = userRepository.findByMobile(mobile).get();

			String password = generateOTP(4);

			user.setPassword(password);
			userRepository.save(user);
			return sendMessage(user.getEmail(), "Jolly Vacations - OTP", "Your OTP is " + password);
		} catch (Exception ex) {
			return false;
		}

	}
	
	public LoginStatusDTO getLoginDetails() throws Exception {

		LoginStatusDTO loginStatus = new LoginStatusDTO();

		if (SecurityContextHolder.getContext().getAuthentication() == null) {
			loginStatus.setUserId("");

			loginStatus.setLoginStatus(false);

			loginStatus.setUserType("");
		} else {

			UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();

			User user=userRepository.findByMobile(userDetails.getUsername()).get();
			
			loginStatus.setUserId(userDetails.getUsername());
			
			loginStatus.setName(user.getName());
			
			loginStatus.setEmail(user.getEmail());
			
			loginStatus.setMobile(user.getMobile());
		
			loginStatus.setLoginStatus(true);

			System.out.println(Long.valueOf(userDetails.getUsername()));

			loginStatus.setUserType(userDetails.getAuthorities().toArray()[0].toString());
		}
		

		return loginStatus;
	}

	public LoginStatusDTO verifyOTP(String mail, String password, String mobile) throws Exception {

		try {

			User user = userRepository.findByEmailOrMobile(mail, mobile).get();
			Boolean status = user.getPassword().equals(password);

			if (user.getIsDisabled()) {
				user.setIsDisabled(!status);
				userRepository.save(user);
			}
			LoginDTO loginDTO = new LoginDTO();
			loginDTO.setMobile(user.getMobile() + "");
			loginDTO.setPassword(password);

			return login(loginDTO);

		} catch (Exception ex) {

			System.out.println(ex);
			LoginStatusDTO loginStatus = new LoginStatusDTO();
			loginStatus.setLoginStatus(false);
			loginStatus.setMessage("Invalid Credientails..");

			return loginStatus;
		}
	}

	public Boolean sendMessage(String mailTo, String subject, String text) {
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setTo(mailTo);
		msg.setSubject(subject);
		msg.setText(text);
		try {

			javaMailSender.send(msg);
		} catch (Exception ex) {
			try {
				javaMailSender.send(msg);
			} catch (Exception ex1) {
				System.out.println("Error in Sending Mail for " + mailTo + "\n" + ex);
			}

		}

		return true;
	}

	public LoginStatusDTO login(LoginDTO login) {
		LoginStatusDTO loginStatus = new LoginStatusDTO();
		String username = login.getMobile();
		String password = login.getPassword();
		try {

			Optional<User> userOpt = userRepository.findByMobile(username);

			if (userOpt.isPresent()) {

				authenticate(username, password);

				final UserDetails userDetails = userDetailsService.loadUserByUsername(username);

				final String token = jwtTokenUtil.generateToken(userDetails);

				loginStatus.setUserId(userDetails.getUsername());

				loginStatus.setLoginStatus(true);

				loginStatus.setJwt(token);

				loginStatus.setUserType(userDetails.getAuthorities().toArray()[0].toString());
			} else {
				System.out.println("He is not user");

				loginStatus.setLoginStatus(false);
				loginStatus.setMessage("Invalid Credientails..");

				return loginStatus;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Error Occured while logging in " + ex);
			loginStatus.setLoginStatus(false);
			loginStatus.setMessage("Invalid Credientails..");
		}

		return loginStatus;

	}

	private void authenticate(String username, String password) throws Exception {
		// System.out.println("entered in authenticate sub function...");
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		} catch (DisabledException e) {
			throw new Exception("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			throw new Exception("INVALID_CREDENTIALS", e);
		}

	}

	public String generateOTP(int length) throws NoSuchAlgorithmException {

		String numbers = "123456789123456789123456789123456789";
		SecureRandom random = new SecureRandom();
		char[] password = new char[length];

		for (int i = 0; i < length; i++) {
			password[i] = numbers.charAt(random.nextInt(numbers.length()));
		}
		return new String(password);
	}

	public LoginStatusDTO signup(SignupDTO signup) throws Exception {

		LoginStatusDTO loginStatus = new LoginStatusDTO();

		Optional<User> userOpt = userRepository.findByMobile(signup.getMobile());

		Optional<User> userOpt1 = userRepository.findByEmail(signup.getEmail());

		System.out.println("in singup");

		if (!userOpt.isPresent() && !userOpt1.isPresent()) {
			System.out.println("in singup2");
			User user = new User();
			user.setEmail(signup.getEmail());
			user.setMobile(signup.getMobile());
			user.setName(signup.getName());
			String otp = generateOTP(4);
			user.setPassword(otp);
			user.setMessage("User Signup");
			user.setRole(roleRepository.findByRoleName("User").get());
			user.setJoinDate(Timestamp.valueOf(LocalDateTime.now()));
			user.setIsDeleted(false);
			user.setIsDisabled(true);
//			if (categoryRepository.findByCname(signup.getCategory()).isPresent())
//				user.setCategory(categoryRepository.findByCname(signup.getCategory()).get());
			userRepository.save(user);
			loginStatus.setLoginStatus(
					userRepository.findByMobileAndPassword(user.getMobile(), user.getPassword()).isPresent());
			loginStatus.setMessage("Login Successful");
			sendMessage(signup.getEmail(), "Jolly Vacations - OTP", "Your OTP is " + otp);

		} else {
			System.out.println("in singup3 " + userOpt.isPresent() + " " + userOpt1.isPresent());
			loginStatus.setLoginStatus(false);
			if (userOpt.isPresent())
				loginStatus.setMessage(" Mobile number already Exists...");

			if (userOpt1.isPresent())
				loginStatus.setMessage(loginStatus.getMessage() + " Email already Exists...");
		}

		return loginStatus;

	}

}
