package com.lms.sc.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.lms.sc.createForm.NoticeAnswerCreateForm;
import com.lms.sc.entity.Notice;
import com.lms.sc.entity.NoticeAnswer;
import com.lms.sc.entity.SiteUser;
import com.lms.sc.exception.DataNotFoundException;
import com.lms.sc.service.NoticeAnswerService;
import com.lms.sc.service.NoticeService;
import com.lms.sc.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;



@RequestMapping("/noticeAnswer")
@RequiredArgsConstructor
@Controller
public class NoticeAnswerController {
	
	private final NoticeService noticeService;
	private final NoticeAnswerService noticeAnswerService;
	private final UserService userService;	
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create/{id}")
	public String createNoticeAnswer(Model model, @PathVariable("id") Integer id,
	                                 @Valid NoticeAnswerCreateForm noticeAnswerCreateForm, 
	                                 BindingResult bindingResult, Principal principal) {
	    SiteUser user = userService.getUserByEmail(principal.getName());
	    Notice notice = this.noticeService.getNotice(id);
	    
	    if (bindingResult.hasErrors()) {
	        model.addAttribute("notice", notice);
	        List<NoticeAnswer> noticeAnswerList = noticeAnswerService.getNoticeAnswerList(notice);
	        model.addAttribute("noticeAnswerList", noticeAnswerList);
	        return "notice/notice_detail";
	    }
	    
	    this.noticeAnswerService.create(notice, noticeAnswerCreateForm.getContent(), user);
	    return String.format("redirect:/notice/detail/%s", id);
	}
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modify/{noticeAnswerId}")
	public String noticeAnswerModify(@Valid NoticeAnswerCreateForm noticeAnswerCreateForm, 
	                                 BindingResult bindingResult, 
	                                 Principal principal, 
	                                 @PathVariable("noticeAnswerId") Integer noticeAnswerId) {
	    if(bindingResult.hasErrors()) {
	        return "notice/notice_detail";
	    }
	    NoticeAnswer noticeAnswer = this.noticeAnswerService.getNoticeAnswer(noticeAnswerId);
	    if(!noticeAnswer.getAuthor().getEmail().equals(principal.getName())) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
	    }
	    this.noticeAnswerService.modify(noticeAnswer, noticeAnswerCreateForm.getContent());
	    return String.format("redirect:/notice/detail/%s", noticeAnswer.getNotice().getId());
	}
	
	 @GetMapping("/delete/{id}") 
	 public String noticeAnswerDelete(Model model, @PathVariable("id") Integer id) {
		NoticeAnswer noticeAnswer = this.noticeAnswerService.getNoticeAnswer(id); 
		if (noticeAnswer == null) {
			throw new DataNotFoundException("NoticeAnswer not found"); 
		} 
		noticeAnswerService.delete(noticeAnswer);
		return String.format("redirect:/notice/detail/%s", noticeAnswer.getNotice().getId());
	}
	 
}
