package com.jolly.vacations.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jolly.vacations.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByMobileAndPassword(String mobile, String password);

	@Query(nativeQuery = true, value = "select image from image_details order by rand() limit 10")
	List<String> getRandomImages();

	Optional<User> findByMobile(@Param("mobile") String mobile);

	Optional<User> findByEmail(@Param("email") String email);
	
	Optional<User> findByEmailOrMobile(@Param("email") String email, @Param("mobile") String mobile);

}
