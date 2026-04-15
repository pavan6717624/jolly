package com.jolly.vacations.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.jolly.vacations.domain.JollyLocation;

public interface JollyLocationRepository extends JpaRepository<JollyLocation, Long> {

	Optional<JollyLocation> findByLocationNameIgnoreCaseOrderByLocationIdDesc(
			@Param("locationName") String locationName);

}
