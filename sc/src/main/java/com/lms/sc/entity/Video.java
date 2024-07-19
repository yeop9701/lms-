package com.lms.sc.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Video {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(columnDefinition = "TEXT")
	private String url;
	
	@Column(length = 200)
	private String title;
	
	//지연 로딩(Lazy Loading) 전략을 사용하여 Lecture 엔티티를 필요할 때만 로드
	@ManyToOne(fetch = FetchType.LAZY)
	//LecVideo 테이블의 lecId 컬럼이 Lecture 테이블의 기본 키를 참조하는 외래 키라는 것을 의미
	private Lecture lecture;
	
	// 영상길이 초단위
	@Column
	private Integer duration;
	
//	@ManyToOne
//	private UserLecture userlecture;
	
	
}
