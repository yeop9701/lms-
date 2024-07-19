package com.lms.sc.controller;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import com.lms.sc.createForm.AnswerCreateForm;
import com.lms.sc.createForm.QuestionCreateForm;
import com.lms.sc.entity.Answer;
import com.lms.sc.entity.Question;
import com.lms.sc.entity.SiteUser;
import com.lms.sc.entity.Video;
import com.lms.sc.exception.DataNotFoundException;
import com.lms.sc.service.AnswerService;
import com.lms.sc.service.QuestionService;
import com.lms.sc.service.UserService;
import com.lms.sc.service.VideoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/question")
public class QuestionController {

	private final QuestionService questionService;
	private final AnswerService answerService;
	private final UserService userService;
	private final VideoService videoService;
	
	@GetMapping("/list") 
	public String list(Model model, @RequestParam(value ="page", defaultValue = "0") int page) {
		Page<Question> paging =	this.questionService.getList(page);
		model.addAttribute("paging", paging);
		return "question/question_list";
	}
	
	// 해결된 리스트
	@GetMapping("/list/resolved")
	public String unresolvedList(Model model, @RequestParam(value ="page", defaultValue = "0") int page) {
		Page<Question> paging = questionService.getListByResult(true, page);
		model.addAttribute("paging", paging);
		return "question/question_list";
	}
	
	// 미해결 리스트
	@GetMapping("/list/unresolved")
	public String resolvedList(Model model, @RequestParam(value ="page", defaultValue = "0") int page) {
		Page<Question> paging = questionService.getListByResult(false, page);
		model.addAttribute("paging", paging);
		return "question/question_list";
	}

	@GetMapping("/detail/{id}")
	public String detail(Model model, @PathVariable("id") Integer id, AnswerCreateForm answerCreateForm) {
		Question question = this.questionService.getQuestion(id);
		if (question == null) {
			throw new DataNotFoundException("Question not found");
		}
		List<Answer> answerList = answerService.getAnswerList(question);
		model.addAttribute("answerList", answerList);
		model.addAttribute("question", question);
		return "question/question_detail";
	}
	
	@GetMapping("/create")
	public String questionCreate(QuestionCreateForm questionCreateForm) {
		return "question/question_form";
	}
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create") 
	public String questionCreate(@Valid QuestionCreateForm questionCreateForm, BindingResult bindingResult, Principal principal) {
		if (bindingResult.hasErrors()) {
			return "question/question_form";
		}
		this.questionService.create(questionCreateForm.getTitle(), questionCreateForm.getContent(), userService.getUserByEmail(principal.getName()));
		return "redirect:/question/list"; //질문 저장 후 질문 목록으로 이동 }
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String questionDelete(Model model, @PathVariable("id") Integer id, Principal principal) {
		Question question = this.questionService.getQuestion(id);
		if (question == null) {
			throw new DataNotFoundException("Question not found");
		}
		
		if(!question.getAuthor().getEmail().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제 권한이 없습니다.");
		}
		
		questionService.delete(question);
		
		return "redirect:/question/list";
	}
	
	// 질문 해결
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/resolve/{questId}")
	public String questionResolved(@PathVariable("questId") Integer id, Principal principal) {
	    Question question = questionService.getQuestion(id);
	    
	    if (question == null) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found");
	    }
	    
	    SiteUser author = userService.getUserByEmail(principal.getName());
	    
	    if (question.getAuthor().getId() != author.getId()) {
	    	throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이 질문에 대한 권한이 없습니다.");
	    }
	    
	    questionService.updateResolve(question);
	    
	    return "redirect:/question/detail/" + question.getId();
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/modify/{id}")
	public String questionModify(QuestionCreateForm questionCreateForm, @PathVariable("id") Integer id, Principal principal) {
		Question question = this.questionService.getQuestion(id);
		SiteUser author = userService.getUserByEmail(principal.getName());
	    
	    if (question.getAuthor().getId() != author.getId()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
		}
		questionCreateForm.setTitle(question.getTitle());
		questionCreateForm.setContent(question.getContent());
		return "question/question_form";
		
	}
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modify/{id}")
	public String questionModify(@Valid QuestionCreateForm questionCreateForm, BindingResult bindingResult, Principal principal, @PathVariable("id") Integer id) {
		if(bindingResult.hasErrors()) {
			return "question_form";
		}
		Question question = this.questionService.getQuestion(id);
		SiteUser author = userService.getUserByEmail(principal.getName());
	    
	    if (question.getAuthor().getId() != author.getId()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
		}
		this.questionService.modify(question, questionCreateForm.getTitle(), questionCreateForm.getContent());
		return String.format("redirect:/question/detail/%s", id);
	}
	
	//비디오 질문 등록
	@PostMapping("/create/{videoId}")
	@ResponseBody
	public ResponseEntity<?> createQuestion(@PathVariable("videoId") long videoId, @RequestParam("title") String title, @RequestParam("content") String content, Principal principal) {
		try {
	        SiteUser user = userService.getUserByEmail(principal.getName());
	        Video video = videoService.getVideo(videoId);
	        
	        Question question = questionService.createQuestion(title, content, user, video);
	        
	     // 생성된 질문의 정보를 반환
            Map<String, Object> response = new HashMap<>();
            response.put("id", question.getId());
            response.put("title", question.getTitle());
            response.put("content", question.getContent());
            response.put("author", question.getAuthor().getName());
            response.put("createDate", question.getCreateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            
//	        return "redirect:/video/viewer/" + video.getId();
	        return ResponseEntity.ok(response);
	        
	    } catch (Exception e) {
	        // 로그 기록
	        e.printStackTrace();
	        // 에러 페이지로 리다이렉트 또는 에러 메시지와 함께 폼으로 돌아가기
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("질문 생성 중 오류가 발생했습니다.");

	    }
	}
	
	@GetMapping("/search")
	public String search(Model model, @RequestParam(value = "keyword", defaultValue = "") String keyword, @RequestParam(value = "page", defaultValue = "0") int page) {
	    Page<Question> questions = questionService.searchQuestions(page, keyword);
	    
	    questions.getContent().forEach(question -> {
	        if (question.getAnswerList() == null) {
	            question.setAnswerList(new ArrayList<>());
	        }
	    });
	    
	    model.addAttribute("paging", questions);
	    model.addAttribute("keyword", keyword);
	    return "question/question_list";
	}
	
	
}
