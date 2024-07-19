package com.lms.sc.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.lms.sc.entity.Notice;
import com.lms.sc.entity.Question;
import com.lms.sc.entity.SiteUser;
import com.lms.sc.exception.DataNotFoundException;
import com.lms.sc.repository.NoticeRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class NoticeService {
	private final NoticeRepository noticeRepository;
	
	public List<Notice> getList() {
		return this.noticeRepository.findAll();
	}
	public List<Notice> getListByAuthor(SiteUser author) {
		 return this.noticeRepository.findByAuthor(author);
	}
	public void create(String title, String content, SiteUser author) {
		Notice n = new Notice();
		n.setTitle(title);
		n.setContent(content);
		n.setAuthor(author);
		n.setCreateDate(LocalDateTime.now());
		this.noticeRepository.save(n);
	}
	
	public Notice getNotice(Integer id) {
		Optional<Notice> notice = this.noticeRepository.findById(id);
		if(notice.isPresent()) {
			return notice.get();
		}else {
			throw new DataNotFoundException("notice not found");
		}
	}
	public void delete(Notice notice) {
		this.noticeRepository.delete(notice);
	}
	
	public void modify(Notice notice, String title, String content) {
		notice.setTitle(title);
		notice.setContent(content);
		notice.setModifyDate(LocalDateTime.now());
		this.noticeRepository.save(notice);
	}

}
