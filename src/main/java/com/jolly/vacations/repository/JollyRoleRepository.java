package com.jolly.vacations.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jolly.vacations.domain.JollyRole;

@Repository
public interface JollyRoleRepository extends JpaRepository<JollyRole, Long> {
	Optional<JollyRole> findByRoleName(@Param("roleName") String roleName);
}
