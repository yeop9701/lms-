package com.lms.sc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lms.sc.entity.Answer;
import com.lms.sc.entity.Question;
import com.lms.sc.entity.SiteUser;


public interface AnswerRepository extends JpaRepository<Answer, Integer>{
	List<Answer> findAllByQuestion(Question question);
	
	void deleteAllByAuthor(SiteUser user);
}
