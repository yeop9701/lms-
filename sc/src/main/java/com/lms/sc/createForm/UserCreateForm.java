package com.lms.sc.createForm;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateForm {
	@Size(min=3, max=25)
	@NotEmpty(message="ID 입력")
	private String name;
	
	@NotEmpty(message="비밀번호 입력")
	private String password1;
	
	@NotEmpty(message="비밀번호 확인")
	private String password2;
	
	@NotEmpty(message="이메일 입력")
	@Email
	private String email;
	
	@NotEmpty(message = "전화번호 입력")
	private String tellNumber;
	
	private String profileImg;
}
