package com.lms.sc.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.lms.sc.entity.Lecture;
import com.lms.sc.entity.SiteUser;
import com.lms.sc.entity.Video;
import com.lms.sc.service.LectureService;
import com.lms.sc.service.UserService;
import com.lms.sc.service.VideoService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
	
	private final UserService userService;
	private final VideoService videoService;
	private final LectureService lectureService;
	
	//유저 정보가기
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/userList")
	public String getUserList(Model model, @RequestParam(value = "kw", defaultValue = "") String kw, @RequestParam(value = "page", defaultValue = "0") int page) {
	    Page<SiteUser> paging = userService.getList(page, kw);
	    model.addAttribute("paging", paging);
	    model.addAttribute("kw", kw);
	    return "admin/userinfo";
	}
	
	//전체 비디오 가지고오기
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/vidList")
	public String allList(Model model, Principal principal) {
		List<Video> vidList = videoService.allList();
		
		model.addAttribute("vidList", vidList);
		
		return "admin/allVidList";
	}
	
	//강의 리스트 이동
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/lecList")
	public String lecList(Model model, Principal principal) {
		List<Lecture> lecture = lectureService.lecList();
		model.addAttribute("lecture", lecture);

		return "admin/lec_list";
	}
}
