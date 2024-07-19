package com.lms.sc.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncService {
	
	@Async
	public void executeAsyncTask() {
		System.out.println("비동기 작업 시작");
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("비동기 작업 완료");
	}
}
