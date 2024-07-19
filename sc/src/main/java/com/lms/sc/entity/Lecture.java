package com.lms.sc.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class Lecture {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(length = 200)
	private String title;
	
	@Column(columnDefinition = "TEXT")
	private String content;
	
	@CreatedDate
	private LocalDateTime createDate;
	
	private String thumnailUrl;
	
//	@ManyToMany
//	@JoinTable(
//	    name = "lecture_student",
//	    joinColumns = @JoinColumn(name = "lecture_id"),
//	    inverseJoinColumns = @JoinColumn(name = "student_id"),
//	    uniqueConstraints = @UniqueConstraint(columnNames = {"lecture_id", "student_id"})
//	)
//	private Set<SiteUser> students;
	
//	@ManyToMany
//	Set<Review> lecReviewCnt;
	 
}
