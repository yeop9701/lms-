package com.lms.sc.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lms.sc.entity.SiteUser;



public interface UserRepository extends JpaRepository<SiteUser, Long> {
	Optional<SiteUser> findByEmail(String email);
	Optional<SiteUser> findById(long id);
	Optional<SiteUser> findByName(String name);
	Optional<SiteUser> findByPassword(String password);
	
	Page<SiteUser> findAll(Pageable pageable);
	
	@Query("SELECT DISTINCT u FROM SiteUser u " +
		       "WHERE (:kw IS NULL OR u.name LIKE %:kw% " +
		       "OR u.email LIKE %:kw% " +
		       "OR u.tellNumber LIKE %:kw%)")
	Page<SiteUser> findAllByKeyword(@Param("kw") String kw, Pageable pageable);
	
	@Query("SELECT u FROM SiteUser u WHERE u.tellNumber = :tellNumber")
	Optional<SiteUser> findByTellNumber(@Param("tellNumber") String tellNumber);
	
//	void deleteAllByUser(SiteUser user);
}
