package com.lms.sc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.lms.sc.entity.Notice;
import com.lms.sc.entity.SiteUser;

public interface NoticeRepository extends JpaRepository<Notice, Integer>{
	@Query("SELECT n FROM Notice n ORDER BY n.createDate DESC")
	List<Notice> findAll();
	
	List<Notice> findByAuthor(SiteUser author);
}
