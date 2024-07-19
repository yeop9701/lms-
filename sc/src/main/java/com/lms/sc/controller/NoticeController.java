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
import com.lms.sc.createForm.NoticeCreateForm;
import com.lms.sc.entity.Notice;
import com.lms.sc.entity.NoticeAnswer;
import com.lms.sc.entity.SiteUser;
import com.lms.sc.exception.DataNotFoundException;
import com.lms.sc.service.NoticeAnswerService;
import com.lms.sc.service.NoticeService;
import com.lms.sc.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Controller
@RequestMapping("/notice")
public class NoticeController {
	
	private final NoticeService noticeService;
	private final UserService userService;
	private final NoticeAnswerService noticeAnswerService;
	
	@GetMapping("list")
	public String list(Model model) {
		List<Notice> list = this.noticeService.getList();
		model.addAttribute("list", list);
		return "notice/notice_list";
	}
	
	@GetMapping("/detail/{id}")
	public String detail(Model model, @PathVariable("id") Integer id, NoticeAnswerCreateForm noticeAnswerCreateForm) {
		Notice notice = this.noticeService.getNotice(id);
		if (notice == null) {
			throw new DataNotFoundException("Notice not found");
		}
		List<NoticeAnswer> noticeAnswerList = noticeAnswerService.getNoticeAnswerList(notice);
		model.addAttribute("noticeAnswerList", noticeAnswerList);
		model.addAttribute("notice", notice);
		return "notice/notice_detail";
	}
	
	@GetMapping("/create")
	public String noticeCreate(NoticeCreateForm noticeCreateForm) {
		return "notice/notice_form";
	}
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create") 
	public String noticeCreate(@Valid NoticeCreateForm noticeCreateForm, BindingResult bindingResult, Principal principal) {
		if (bindingResult.hasErrors()) {
			return "notice/notice_form";
		}
		this.noticeService.create(noticeCreateForm.getTitle(), noticeCreateForm.getContent(), userService.getUserByEmail(principal.getName()));
		return "redirect:/notice/list";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String noticeDelete(Model model, @PathVariable("id") Integer id, Principal principal) {
		Notice notice = this.noticeService.getNotice(id);
		if (notice == null) {
			throw new DataNotFoundException("Notice not found");
		}
		
		if(!notice.getAuthor().getName().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제 권한이 없습니다.");
		}
		
		noticeService.delete(notice);
		
		return "redirect:/notice/list";
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/modify/{id}")
	public String noticeModify(NoticeCreateForm noticeCreateForm, @PathVariable("id") Integer id, Principal principal) {
		Notice notice = this.noticeService.getNotice(id);
		SiteUser author = userService.getUserByEmail(principal.getName());
	    
	    if (notice.getAuthor().getId() != author.getId()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
		}
	    noticeCreateForm.setTitle(notice.getTitle());
	    noticeCreateForm.setContent(notice.getContent());
		return "notice/notice_form";
		
	}
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modify/{id}")
	public String noticeModify(@Valid NoticeCreateForm noticeCreateForm, BindingResult bindingResult, Principal principal, @PathVariable("id") Integer id) {
		if(bindingResult.hasErrors()) {
			return "notice_form";
		}
		Notice notice = this.noticeService.getNotice(id);
		SiteUser author = userService.getUserByEmail(principal.getName());
	    
	    if (notice.getAuthor().getId() != author.getId()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
		}
		this.noticeService.modify(notice, noticeCreateForm.getTitle(), noticeCreateForm.getContent());
		return String.format("redirect:/notice/detail/%s", id);
	}
	
}
