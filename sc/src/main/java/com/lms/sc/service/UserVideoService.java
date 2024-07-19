package com.lms.sc.service;


import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lms.sc.entity.Lecture;
import com.lms.sc.entity.SiteUser;
import com.lms.sc.entity.UserVideo;
import com.lms.sc.entity.Video;
import com.lms.sc.entity.WeeklyWatchData;
import com.lms.sc.repository.UserRepository;
import com.lms.sc.repository.UserVideoRepository;
import com.lms.sc.repository.VideoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserVideoService {
	private final UserVideoRepository userVideoRepository;
	private final UserRepository userRepository;
	private final VideoRepository videoRepository;
	
	//유저 비디오 하나 가져오기
	public UserVideo getUserVideo(long vidId, long userId) {
		SiteUser user = userRepository.findById(userId).get();
		Video video = videoRepository.findById(vidId).get();
        return userVideoRepository.findByUserAndVideo(user, video).orElse(null);
    }
	
	public void saveUserVideo(UserVideo userVideo) {
		userVideoRepository.save(userVideo);
	}
	
	//유저 비디오 정보 저장
	public void saveUserVideo(UserVideo userVideo, boolean watched, Date watchedAt, Integer watchingTime) {
		userVideo.setWatched(watched);
		userVideo.setWatchedAt(watchedAt);
		userVideo.setWatchingTime(watchingTime);
		userVideoRepository.save(userVideo);
	}
	
	//유저 비디오 있으면 강의 진행 없으면 새로만들어서 진행
	public UserVideo getUserVideoOrNew(SiteUser user, Video video) {
	    Optional<UserVideo> optionalUserVideo = userVideoRepository.findByUserAndVideo(user, video);
	    if (optionalUserVideo.isPresent()) {
	        return optionalUserVideo.get();
	    } else {
	        UserVideo userVideo = new UserVideo();
	        userVideo.setUser(user);
	        userVideo.setVideo(video);
	        userVideo.setWatchingTime(0);
	        return userVideoRepository.save(userVideo);
	    }
	}
	
	// 최근 본 영상 3개 가져오기
	public List<UserVideo> getTop3UserVideo(SiteUser user) {
		Pageable pageable = PageRequest.of(0, 3);
		return userVideoRepository.findTop3ByAuthorUserVideos(user, pageable);
	}
	
	// UserVideo 중 lecture 중 watched가 true인 리스트를 가져오기
	public List<UserVideo> getUserVideoByWatched(SiteUser user, Lecture lecture, boolean watched){
		return userVideoRepository.findByUserAndLectureAndWatched(user, lecture, watched);
	}
	
	// UserVideo 중 watched가 true인 리스트 가져오기
	@Transactional(readOnly = true)
	public List<UserVideo> getUserVideoByWatched(SiteUser user) {
		return userVideoRepository.findByUserAndWatchedWithVideoAndLecture(user);
	}
	
	public WeeklyWatchData getWeeklyWatchCount(SiteUser user, Integer weekOffset) {
	    Calendar cal = Calendar.getInstance();
	    if (weekOffset != null) {
	        cal.add(Calendar.WEEK_OF_YEAR, -weekOffset);
	    }
	    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
	    Date startDate = cal.getTime();

	    cal.add(Calendar.DATE, 6);
	    Date endDate = cal.getTime();

	    // 기존의 watchedVideos 처리 코드...
	    List<UserVideo> watchedVideos = userVideoRepository.findByUserAndWatchedAtBetween(user, startDate, endDate);

        String[] daysOfWeek = {"월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일"};
        Map<String, Long> orderedDailyCount = new LinkedHashMap<>();
        for (String day : daysOfWeek) {
            orderedDailyCount.put(day, 0L);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        for (UserVideo video : watchedVideos) {
            String dayOfWeek = sdf.format(video.getWatchedAt());
            orderedDailyCount.put(dayOfWeek, orderedDailyCount.get(dayOfWeek) + 1);
        }
	    
	    // 날짜 범위 문자열 생성
	    SimpleDateFormat dateFormat = new SimpleDateFormat("M/d");
	    String dateRange = dateFormat.format(startDate) + " ~ " + dateFormat.format(endDate);

	    WeeklyWatchData result = new WeeklyWatchData();
	    result.setWatchCount(orderedDailyCount);
	    result.setDateRange(dateRange);

	    return result;
	}
	
	//최근 학습 완료한 강의(성장로그)
//	public List<String> getRecentlyCompletedLectures(long userId) {
//	    List<String> watchedLectures = userVideoRepository.findRecentlyCompletedLectureTitles(userId, PageRequest.of(0, 3));
//	    return watchedLectures;
//	}
	
	public List<Object[]> getRecentlyCompletedLectures(long userId) {
	    return userVideoRepository.findRecentlyCompletedLectureTitlesAndDates(userId, PageRequest.of(0, 3));
	}
	
	//일별 학습율 그래프
	public Map<String, Integer> getDailyWatchCount(SiteUser user, LocalDateTime startDate, LocalDateTime endDate) {
	    List<Object[]> results = userVideoRepository.getDailyWatchCount(user, startDate, endDate);
	    Map<String, Integer> dailyWatchCount = new TreeMap<>();

	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

	    for (Object[] result : results) {
	        Date sqlDate = (Date) result[0];
	        LocalDate date = LocalDate.parse(sqlDate.toString());
	        String formattedDate = date.format(formatter);
	        Integer count = ((Number) result[1]).intValue();
	        dailyWatchCount.put(formattedDate, count);
	    }

	    return dailyWatchCount;
	}
}




















