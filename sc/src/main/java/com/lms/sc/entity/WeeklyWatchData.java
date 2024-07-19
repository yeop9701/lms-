package com.lms.sc.entity;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeeklyWatchData {
	
	private Map<String, Long> watchCount;
	
	private String dateRange;
}
