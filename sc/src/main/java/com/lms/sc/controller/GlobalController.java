package com.lms.sc.controller;

import java.security.Principal;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;

import com.lms.sc.entity.SiteUser;
import com.lms.sc.entity.SiteUserDetails;
import com.lms.sc.service.UserService;

import lombok.RequiredArgsConstructor;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalController {
	private final UserService userService;
	
    public void addAttributes(Model model, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof SiteUserDetails) {
            SiteUserDetails userDetails = (SiteUserDetails) authentication.getPrincipal();
            model.addAttribute("userName", userDetails.getName());
        }
    }
    
    public void userInfo(Model model, Principal principal) {
    	SiteUser user = userService.getUserByEmail(principal.getName());
    	model.addAttribute("user", user);
    }
}
