package com.lms.sc.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lms.sc.entity.Lecture;
import com.lms.sc.entity.SiteUser;
import com.lms.sc.entity.Video;



public interface VideoRepository extends JpaRepository<Video, Long> {
	Optional<Video> findById(long id);
	Optional<Video> findByUrl(String url);
	List<Video> findAllByLecture(Lecture lecture);
	
	void deleteAllByLecture(Lecture lecture);
}
