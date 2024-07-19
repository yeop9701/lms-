package com.lms.sc.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.lms.sc.entity.Answer;
import com.lms.sc.entity.Question;
import com.lms.sc.entity.SiteUser;
import com.lms.sc.exception.DataNotFoundException;
import com.lms.sc.repository.AnswerRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AnswerService {
	
	private final AnswerRepository answerRepository;
	
//	등록
	public void create(Question question, String content, SiteUser author) {
		Answer answer = new Answer();
		answer.setContent(content);
		answer.setAuthor(author);
		answer.setCreateDate(LocalDateTime.now());
		answer.setQuestion(question);
		this.answerRepository.save(answer);
	}
	
	public List<Answer> getAnswerList(Question question){
		return answerRepository.findAllByQuestion(question);
	}
	

	public Answer getAnswer(Integer id) {
		Optional<Answer> answer= this.answerRepository.findById(id);
		if(answer.isPresent()) {
			return answer.get();
		}else {
			throw new DataNotFoundException("answer not found");
		}
	}
	
	public void modify(Answer answer, String content) {
		answer.setContent(content);
		answer.setModifyDate(LocalDateTime.now());
		this.answerRepository.save(answer);
	}
	
	public void delete(Answer answer) {
		this.answerRepository.delete(answer);
	}
	
	
}
