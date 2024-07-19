package com.lms.sc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lms.sc.entity.Notice;
import com.lms.sc.entity.NoticeAnswer;


public interface NoticeAnswerRepository extends JpaRepository<NoticeAnswer, Integer>{
	List<NoticeAnswer> findAllByNotice(Notice notice);
}
