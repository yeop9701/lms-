package com.lms.sc.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class NoticeAnswer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(columnDefinition = "TEXT")
	private String content;
	
	private int likeCnt;
	
	@CreatedDate
	private LocalDateTime createDate;
	
	private LocalDateTime modifyDate;
	
	
	@ManyToOne
	private Notice notice;
	
	@ManyToOne
	private SiteUser author;
	
}
