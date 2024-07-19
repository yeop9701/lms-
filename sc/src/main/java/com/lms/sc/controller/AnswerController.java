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

import com.lms.sc.createForm.AnswerCreateForm;
import com.lms.sc.entity.Answer;
import com.lms.sc.entity.Question;
import com.lms.sc.entity.SiteUser;
import com.lms.sc.exception.DataNotFoundException;
import com.lms.sc.service.AnswerService;
import com.lms.sc.service.QuestionService;
import com.lms.sc.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/answer")
@RequiredArgsConstructor
@Controller
public class AnswerController {
	
	private final QuestionService questionService;
	private final AnswerService answerService;
	private final UserService userService;	
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create/{id}")
	public String createAnswer(Model model, @PathVariable("id") Integer id,
	@Valid AnswerCreateForm answerCreateForm, BindingResult bindingResult, Principal principal) {
		SiteUser user = userService.getUserByEmail(principal.getName());
		Question question = this.questionService.getQuestion(id);
		if (bindingResult.hasErrors()) {
			model.addAttribute("question", question);
			List<Answer> answerList = answerService.getAnswerList(question);
			model.addAttribute("answerList", answerList);
			return "question/question_detail";
		}
		this.answerService.create(question, answerCreateForm.getContent(), user);
		return String.format("redirect:/question/detail/%s", id);
	}
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modify/{answerId}")
	public String answerModify(@Valid AnswerCreateForm answerCreateForm, BindingResult bindingResult, 
			Principal principal, @PathVariable("answerId") Integer answerId) {
		if(bindingResult.hasErrors()) {
			return "question_detail{id}";
		}
		Answer answer = this.answerService.getAnswer(answerId);
		if(!answer.getAuthor().getEmail().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
		}
		this.answerService.modify(answer, answerCreateForm.getContent());
		return String.format("redirect:/question/detail/%s", answer.getQuestion().getId());
	}
	
	
	 @GetMapping("/delete/{id}") 
	 public String answerDelete(Model model, @PathVariable("id") Integer id) {
		Answer answer = this.answerService.getAnswer(id); 
		if (answer == null) {
			throw new DataNotFoundException("Answer not found"); 
		} 
		answerService.delete(answer);
	 
		return String.format("redirect:/question/detail/%s", answer.getQuestion().getId());
	}
	 
}
