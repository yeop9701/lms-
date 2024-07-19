package com.lms.sc.createForm;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeAnswerCreateForm {
	@NotEmpty(message="내용을 입력하세요.")
	private String Content;
}
