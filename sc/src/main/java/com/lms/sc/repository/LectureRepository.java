package com.lms.sc.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lms.sc.entity.Lecture;
import com.lms.sc.entity.SiteUser;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
	Optional<Lecture> findById(long id);
	
//	@Query("SELECT l FROM Lecture l LEFT JOIN FETCH l.students WHERE l.id = :lecId")
//	Optional<Lecture> findByIdWithStudents(@Param("lecId") long lecId);
	
}
