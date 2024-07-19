package com.lms.sc.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Question {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(length = 100)
	String title;
	
	@Column(columnDefinition = "TEXT")
	private String content;
	
	private Integer likeCnt;
	
	@CreatedDate
	private LocalDateTime createDate;
	
	private LocalDateTime modifyDate;
	
	private boolean result;
	
//	@OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE)
	@OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Answer> answerList;
	
	@ManyToOne
	@JoinColumn(name = "author_id")
	private SiteUser author;
	
	@ManyToOne
	@JoinColumn(name = "video_id", nullable = true)
	private Video video;
}