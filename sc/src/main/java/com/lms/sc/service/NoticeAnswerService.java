package com.lms.sc.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.lms.sc.entity.Notice;
import com.lms.sc.entity.NoticeAnswer;
import com.lms.sc.entity.SiteUser;
import com.lms.sc.exception.DataNotFoundException;
import com.lms.sc.repository.NoticeAnswerRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class NoticeAnswerService {
	
	private final NoticeAnswerRepository noticeAnswerRepository;
	
//	등록
	public void create(Notice notice, String content, SiteUser author) {
		NoticeAnswer noticeAnswer = new NoticeAnswer();
		noticeAnswer.setContent(content);
		noticeAnswer.setAuthor(author);
		noticeAnswer.setCreateDate(LocalDateTime.now());
		noticeAnswer.setNotice(notice);
		this.noticeAnswerRepository.save(noticeAnswer);
	}
	
	public List<NoticeAnswer> getNoticeAnswerList(Notice notice){
		return noticeAnswerRepository.findAllByNotice(notice);
	}
	

	public NoticeAnswer getNoticeAnswer(Integer id) {
		Optional<NoticeAnswer> noticeAnswer= this.noticeAnswerRepository.findById(id);
		if(noticeAnswer.isPresent()) {
			return noticeAnswer.get();
		}else {
			throw new DataNotFoundException("noticeAnswer not found");
		}
	}
	
	public void modify(NoticeAnswer noticeAnswer, String content) {
		noticeAnswer.setContent(content);
		noticeAnswer.setModifyDate(LocalDateTime.now());
		this.noticeAnswerRepository.save(noticeAnswer);
	}
	
	public void delete(NoticeAnswer noticeAnswer) {
		this.noticeAnswerRepository.delete(noticeAnswer);
	}
	
	
}
