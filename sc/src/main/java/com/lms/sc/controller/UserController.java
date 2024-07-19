package com.lms.sc.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.lms.sc.createForm.TempPasswordForm;
import com.lms.sc.createForm.UserCreateForm;
import com.lms.sc.entity.Lecture;
import com.lms.sc.entity.SiteUser;
import com.lms.sc.exception.DataNotFoundException;
import com.lms.sc.exception.EmailException;
import com.lms.sc.repository.UserRepository;
import com.lms.sc.service.LectureService;
import com.lms.sc.service.UserLectureService;
import com.lms.sc.service.UserService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
	
	private final UserService userService;
	private final UserLectureService userLecService;
	private final LectureService lecService;
	private final PasswordEncoder passwordEncoder;
	
	// 회원 삭제
    @DeleteMapping("/delete/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<String> deleteUser(@PathVariable("userId") Long userId, HttpSession session) {
        try {
            userService.deleteUser(userId);
            session.invalidate();
            return ResponseEntity.ok("User successfully deleted");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred while deleting the user");
        }
    }
	
	// 회원가입 이동
	@GetMapping("/signup")
	public String signup(UserCreateForm userCreateForm) {
		return "user/sign_up";
	}
	
	// 회원가입
	@PostMapping("/signup")
	public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
	    if (bindingResult.hasErrors()) {
	        System.out.println(bindingResult.getErrorCount());
	        System.out.println(bindingResult.getObjectName());
	        bindingResult.getAllErrors().forEach(error -> {
	            System.out.println("Error: " + error.getObjectName() + " - " + error.getDefaultMessage());
	        });
	        return "user/sign_up";
	    }
	    
	    if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
	        bindingResult.rejectValue("password2", "passwordInCorrect", "2개의 비밀번호가 일치하지 않습니다.");
	        return "user/sign_up";
	    }

	    // 전화번호에서 하이픈 제거
	    String cleanedTellNumber = userCreateForm.getTellNumber().replaceAll("-", "");
	    userCreateForm.setTellNumber(cleanedTellNumber);

	    userService.create(userCreateForm.getName(), 
	                       userCreateForm.getEmail(), 
	                       userCreateForm.getPassword1(), 
	                       cleanedTellNumber,  // 정제된 전화번호 사용
	                       userCreateForm.getProfileImg());
	    
	    return "redirect:/user/login";
	}
	
	// 로그인 이동
	@GetMapping("/login")
	public String login() {
		return "user/login";
	}
	
	//이메일 중복 체크
	@PostMapping("/emailCheck")
	@ResponseBody
	public String CheckEamil(@RequestParam("email") String email) {
		return userService.checkEmail(email);
	}
	
	@GetMapping("/phoneCheck")
	@ResponseBody
	public String phoneCheck(@RequestParam("tellNumber") String tellNumber) {
	    return userService.checkPhone(tellNumber);
	}
	
//	@GetMapping("/getUser")
//	public SiteUser getUser(Principal principal) {
//		SiteUser user = userService.getUser(principal.getName());
//		return user;
//	}
	
	@GetMapping("/getUser")
    public ResponseEntity<SiteUser> getCurrentUser(Principal principal) {
        SiteUser user = userService.getUserByEmail(principal.getName());
        return ResponseEntity.ok(user);
    }
	
	// 강의 목록 삭제
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/lec/delete/{lecId}")
	public String delLecture(Principal principal, @PathVariable("lecId") long lecId) throws Exception {
		SiteUser user = userService.getUserByEmail(principal.getName());
		Lecture lecture = lecService.getLecture(lecId);
		lecService.lectureSave(lecture);
		userLecService.deleteLec(user, lecture);
		return "redirect:/my/list";
	}
	
	// 마이페이지 이동
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/mypage")
	public String myPage(Model model, Principal principal) {
		if(principal == null) {
			return "main/main";
		}
		
		SiteUser user = userService.getUserByEmail(principal.getName());
		model.addAttribute("user", user);
		
		return "mypage/mypage";
	}
	
	//유저 정보 수정
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modify")
	public String modify(@AuthenticationPrincipal UserDetails userDetails,
	                     @RequestParam("id") long id,
	                     @RequestParam("name") String name,
	                     @RequestParam("currentPassword") String currentPassword,
	                     @RequestParam(value = "newPassword", required = false) String newPassword,
	                     @RequestParam("tellNumber") String tellNumber,
	                     @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
	                     RedirectAttributes redirectAttributes) throws IOException {
	    SiteUser user = userService.getUserById(id);
	    
	    // 현재 비밀번호 확인
	    if (!passwordEncoder.matches(currentPassword, userDetails.getPassword())) {
	    	redirectAttributes.addFlashAttribute("error", "현재 비밀번호가 일치하지 않습니다.");
	        return "redirect:/user/mypage?error=currentPassword";
	    }
	    
	    // 새 비밀번호가 제공된 경우에만 변경
	    String passwordToUpdate = null;
	    if (newPassword != null && !newPassword.isEmpty()) {
	        passwordToUpdate = passwordEncoder.encode(newPassword);
	    }
	    userService.modify(user, name, passwordToUpdate, tellNumber);

	    if (profileImage != null && !profileImage.isEmpty()) {
	        userService.updateProfileImage(user.getId(), profileImage);
	    }
	    
	    redirectAttributes.addFlashAttribute("message", "수정이 완료되었습니다.");

	    return "redirect:/user/mypage";
	}
	
	// 현재 비밀번호 확인
	@PostMapping("/checkPassword")
	@ResponseBody
	public Map<String, Boolean> checkPassword(@AuthenticationPrincipal UserDetails userDetails,
	        @RequestBody Map<String, String> payload) {
	    String password = payload.get("password");
	    boolean isPasswordCorrect = passwordEncoder.matches(password, userDetails.getPassword());
	    return Collections.singletonMap("passwordCorrect", isPasswordCorrect);
	}
	
	
	//임시비밀번호 관련
	@GetMapping("/tempPassword")
	public String showTempPasswordForm(Model model) {
		//임시비밀번호 html 페이지로 넘겨준다.
	    model.addAttribute("tempPasswordForm", new TempPasswordForm());
	    return "user/find_temp_password";
	}

	//임시비밀번호 메일 전송
	@PostMapping("/tempPassword")
	public String sendTempPassword(@Valid TempPasswordForm tpf) {
		try {
			//사용자의 이메일로 임시 비밀번호를 생성하고 전송
			userService.modifyPassword(tpf.getEmail());
		}catch(DataNotFoundException e) {
			e.printStackTrace();
//			br.reject("emailNotFound", e.getMessage());
			return "user/find_temp_password";
		}catch (EmailException e) {
            e.printStackTrace();
//            br.reject("sendEmailFail", e.getMessage());
            return "user/find_temp_password";
        }
		
		return "redirect:/user/login?success=true";
	}
	
	//아이디 찾기
	@GetMapping("/find-id")
    public String findIdForm() {
        return "user/find_id_form";
    }

    @PostMapping("/find-id")
    public String findId(@RequestParam("tellNumber") String tellNumber, Model model) {
        String maskedEmail = userService.findEmailByTellNumber(tellNumber);
        if (maskedEmail != null) {
            model.addAttribute("maskedEmail", maskedEmail);
            return "user/find_id_result";
        } else {
            model.addAttribute("error", "일치하는 사용자를 찾을 수 없습니다.");
            return "error/404";
        }
    }
    
    //아이디 찾기 결과 페이지
//    @GetMapping("/find_id_result")
//    public String resultId() {
//        return "user/find_id_result";
//    }
}
