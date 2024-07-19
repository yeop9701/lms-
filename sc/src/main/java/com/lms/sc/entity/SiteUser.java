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

@Getter
@Setter
@Entity
public class SiteUser {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(length = 20)
	private String name;
	
	@Column(length = 50, unique=true)
	private String email;
	
	@Column(length = 100)
	private String password;
	
	@Column(length = 20, unique=true)
	private String tellNumber;
	
	@Column(columnDefinition = "TEXT")
	private String profileImage;
	
	@CreatedDate
	@Column
	private LocalDateTime createDate;

}
