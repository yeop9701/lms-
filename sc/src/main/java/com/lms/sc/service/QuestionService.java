package com.lms.sc.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lms.sc.entity.Lecture;
import com.lms.sc.entity.Question;
import com.lms.sc.entity.SiteUser;
import com.lms.sc.entity.Video;
import com.lms.sc.exception.DataNotFoundException;
import com.lms.sc.repository.QuestionRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class QuestionService {
	private final QuestionRepository questionRepository;
	
	@Transactional(readOnly = true)
	public Page<Question> getList(int page){
		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("createDate"));
		Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
		Page<Question> questions = questionRepository.findAll(pageable);
		questions.forEach(question -> Hibernate.initialize(question.getAnswerList()));
        return questions;
	}
	
	// result 결과 리스트
	public Page<Question> getListByResult(boolean result, int page) {
		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("createDate"));
		Pageable pagealbe = PageRequest.of(page, 10, Sort.by(sorts));
		Page<Question> questions = questionRepository.findByResultWithAnswers(result, pagealbe);
		return questions;
	}
	
	 public List<Question> getList() { 
		 return this.questionRepository.findAll(); 
	 	}
	 
	 public Page<Question> getListByAuthor(SiteUser author, int page) {
		 List<Sort.Order> sorts = new ArrayList<>();
		 sorts.add(Sort.Order.desc("createDate"));
		 Pageable pageable = PageRequest.of(page, 10);
		 return this.questionRepository.findByAuthor(author, pageable);
	 }

	 public List<Question> getRecentQuestions(SiteUser author) {
		 Pageable pageable = PageRequest.of(0, 3);
		 return questionRepository.findTop3ByAuthor(author, pageable);
	 }

	public Question getQuestion(Integer id) {
		Optional<Question> question = this.questionRepository.findById(id);
		if(question.isPresent()) {
			return question.get();
		}else {
			throw new DataNotFoundException("question not found");
		}
	}
	
	public Question updateResolve(Question question) {
		boolean resolved = true;
		question.setResult(resolved);
		return questionRepository.save(question);
	}
	
	public void create(String title, String content, SiteUser author) {
		Question q = new Question();
		q.setTitle(title);
		q.setContent(content);
		q.setAuthor(author);
		q.setCreateDate(LocalDateTime.now());
		this.questionRepository.save(q);
	}
	
	public void delete(Question question) {
		this.questionRepository.delete(question);
	}
	
	public void modify(Question question, String title, String content) {
		question.setTitle(title);
		question.setContent(content);
		question.setModifyDate(LocalDateTime.now());
		this.questionRepository.save(question);
	}
	
	//비디오뷰에서 질문을 저장
	public Question createQuestion(String title, String content, SiteUser author, Video video) {
        Question question = new Question();
        question.setTitle(title);
        question.setContent(content);
        question.setAuthor(author);
        question.setVideo(video);
        question.setCreateDate(LocalDateTime.now());
//        question.setLikeCnt(0);
//        question.setResult(false);
        return questionRepository.save(question);
    }
	
	//유저마다 강의 가져오기
	public List<Question> getQusetionByUserAndLecture(SiteUser user, Lecture lecture){
		return questionRepository.findByAuthorAndVideo_Lecture(user, lecture);
	}
	
	//강의 질문 가져오기
	public List<Question> getQuestionByLecture(Lecture lecture){
		return questionRepository.findByVideo_Lecture(lecture);
	}
	
	public Page<Question> searchQuestions(int page, String keyword) {
	    Pageable pageable = PageRequest.of(page, 10, Sort.by("createDate").descending());
	    return questionRepository.findByKeywordWithAnswers(keyword, pageable);
	}
	
//	public List<Question> getQuestionByLecture(Lecture lecture){
//		return questionRepository.findByVideo_Lecture(lecture);
//	}
	
	public List<Question> getQuestionByUserAndLectureSortedAsc(SiteUser user, Lecture lecture) {
        return questionRepository.findByAuthorAndLectureOrderByCreateDateAsc(user, lecture);
    }
}
