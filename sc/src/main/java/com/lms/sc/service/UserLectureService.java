package com.lms.sc.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lms.sc.entity.Lecture;
import com.lms.sc.entity.SiteUser;
import com.lms.sc.entity.UserLecture;
import com.lms.sc.entity.Video;
import com.lms.sc.repository.UserLectureRepository;
import com.lms.sc.repository.UserVideoRepository;
import com.lms.sc.repository.VideoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserLectureService {
//	private final LectureRepository lectureRepository;
//	private final VideoRepository videoRepository;
//	private final UserRepository userRepository;
	
	private final UserLectureRepository userLectureRepository;
	private final UserVideoRepository userVidRepo;
	private final VideoRepository vidRepo;
	
	// 나의 강의 리스트
	@Transactional(readOnly = true)
	public List<UserLecture> getMyList(SiteUser user){		
		
		List<UserLecture> userLectureList = userLectureRepository.findByUser(user);
		return userLectureList;
	}
	
	// userLecture 하나 가져오기
	public UserLecture getMyUserLec(SiteUser user, Lecture lecture) {
		return userLectureRepository.findByUserAndLecture(user, lecture).get();
	}
	
	// 강의 수강 시작시 UserLecture도 등록되게
	public UserLecture createUserLecture(SiteUser user, Lecture lecture) {
		UserLecture userLec = new UserLecture();
		userLec.setUser(user);
		userLec.setLecture(lecture);
		userLec.setProgress(0);
		userLec.setRegDate(LocalDateTime.now());
		return userLectureRepository.save(userLec);
	}
	
	// 수강중인 학생 수 가져오기
	public Integer getStudents(Lecture lecture) {
		List<UserLecture> list = userLectureRepository.findByLecture(lecture);
		return list.size();
	}
	
	// userLecture의 progress를 업데이트
	public UserLecture updateProgress(SiteUser user, Lecture lecture, double progress) {
		UserLecture userLec = userLectureRepository.findByUserAndLecture(user, lecture).get();
		userLec.setProgress(progress);
		return userLectureRepository.save(userLec);
	}
	
	// 강의 중복 확인
	public String checkLec(String userEmail, long lecId) {
		Optional<UserLecture> userLecture = userLectureRepository.findByUserAndLecture(userEmail, lecId);
		
		return userLecture.isPresent() ? "false" : "true";
	}
	
	// 강의 목록에서 듣고 있는 강의 삭제
	@Transactional
	public void deleteLec(SiteUser user, Lecture lecture) {
		UserLecture userLec = userLectureRepository.findByUserAndLecture(user, lecture).get();
		Lecture lec = userLec.getLecture();
		List<Video> videoList = vidRepo.findAllByLecture(lec);
		for (Video video : videoList) {
			userVidRepo.deleteByVideoAndUser(video, user);
		}
		userLectureRepository.delete(userLec);
	}
	
	// progress가 1인 userLecture 조회
	public List<Object[]> recentProgress(SiteUser user) {
		Pageable pageable = PageRequest.of(0, 3);
		return userLectureRepository.findByUserAndProgressWithLastWatchedAt(user, 1, pageable);
	}
	
}
