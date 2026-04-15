package com.jolly.vacations.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jolly.vacations.domain.JollyCustomer;

public interface JollyCustomerRepository extends JpaRepository<JollyCustomer, Long> {

	List<JollyCustomer> findByMobile(String mobile);

}
