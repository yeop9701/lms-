package com.lms.sc.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lms.sc.entity.Lecture;
import com.lms.sc.entity.Note;
import com.lms.sc.entity.Question;
import com.lms.sc.entity.SiteUser;
import com.lms.sc.entity.UserLecture;
import com.lms.sc.entity.UserVideo;
import com.lms.sc.entity.Video;
import com.lms.sc.entity.WeeklyWatchData;
import com.lms.sc.service.NoteService;
import com.lms.sc.service.QuestionService;
import com.lms.sc.service.UserLectureService;
import com.lms.sc.service.UserService;
import com.lms.sc.service.UserVideoService;
import com.lms.sc.service.VideoService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/my")
public class UserLectureController {
	private final UserLectureService userLectureService;
	private final UserService userService;
//	private final LectureService lectureService;
	private final VideoService vidService;
	private final UserVideoService userVidService;
	private final QuestionService questService;
	private final NoteService noteService;
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("list")
	public String getMyList(Principal principal, Model model) throws Exception {
	    if (principal == null) {
	        return "user/login";
	    }
	    
	    SiteUser user = userService.getUserByEmail(principal.getName());
	    List<UserLecture> userLectureList = userLectureService.getMyList(user);
	    model.addAttribute("userLectureList", userLectureList);
	    
	    Map<UserLecture, Map<Integer, Integer>> list = new LinkedHashMap<>();
	    Map<UserLecture, Boolean> lectureVideoPresence = new HashMap<>();
	    for (UserLecture userLecture : userLectureList) {
	        List<Video> videoList = vidService.VideoList(userLecture.getLecture());
	        int watched = 0;
	        
	        for (Video video : videoList) {
	            UserVideo userVideo = userVidService.getUserVideoOrNew(user, video);
	            if (userVideo.isWatched())
	                watched++;
	        }
	        
	        Map<Integer, Integer> progress = new HashMap<Integer, Integer>();
	        progress.put(videoList.size(), watched);
	        
	        double userLecProgress;
	        if(videoList.size() > 0) {
	        	userLecProgress = (double) watched / videoList.size(); 	        	
	        }else {
	        	userLecProgress = 0.0;
	        }
	        userLecture = userLectureService.updateProgress(user, userLecture.getLecture(), userLecProgress);
	        
	        list.put(userLecture, progress);
	        lectureVideoPresence.put(userLecture, !videoList.isEmpty());
	    }
	    List<UserLecture> updateList = userLectureService.getMyList(user);
	    model.addAttribute("userLecList", updateList);
	    
	    model.addAttribute("list", list);
	    model.addAttribute("lectureVideoPresence", lectureVideoPresence);
	    return "mypage/my_list";
	}
	
	//강의 중복 확인
	@PreAuthorize("isAuthenticated()")
	@GetMapping("lecCheck")
	@ResponseBody
	public String lecChk(Principal principal, @RequestParam("lecId") Long lecId) throws Exception {
		String user = principal.getName();
		return userLectureService.checkLec(user, lecId);
	}
	
	// 내 질문 리스트
	@PreAuthorize("isAuthenticated()")
	@GetMapping("question")
	public String myQuestion(Principal principal, Model model, @RequestParam(value ="page", defaultValue = "0") int page) {
		if (principal == null) {
			return "user/login";
		}
		SiteUser user = userService.getUserByEmail(principal.getName());
		Page<Question> questionList = questService.getListByAuthor(user, page);
		
		model.addAttribute("questionList", questionList);
		
		return "mypage/my_question";
	}
	
	// 대시보드
	@PreAuthorize("isAuthenticated()")
	@GetMapping("dashboard")
	public String dashboard(Principal principal, Model model,
				@RequestParam(name = "weekOffset", required = false, defaultValue = "0") Integer weekOffset) {
		if (principal == null) {
			return "user/login";
		}
		// 질문 리스트 가져오기
		SiteUser user = userService.getUserByEmail(principal.getName());
		List<Question> questionList = questService.getRecentQuestions(user);
		model.addAttribute("questionList", questionList);
		
		// 노트 리스트 가져오기
		List<Note> noteList = noteService.getRecentNotes(user);
		model.addAttribute("noteList", noteList);
		
		// 최근 학습한 강의 가져오기
		List<UserVideo> userVideoList = userVidService.getTop3UserVideo(user);
		model.addAttribute("userVideoList", userVideoList);
		
//		List<UserLecture> userLectureList = userLectureService.getMyList(user);
//		model.addAttribute("userLectureList", userLectureList);
		
		// 주간 학습 현황
		WeeklyWatchData weeklyData = userVidService.getWeeklyWatchCount(user, weekOffset);
	    model.addAttribute("weeklyWatchCount", weeklyData.getWatchCount());
	    model.addAttribute("weekDateRange", weeklyData.getDateRange());
	    model.addAttribute("weekOffset", weekOffset != null ? weekOffset : 0);
	    
	    // 완료한 강의가 몇개인지 파이그래프 그리기
	    // 강의 완료 여부에 따라 UserVideo 리스트를 필터링합니다.
	    List<UserVideo> pieUserVideo = userVidService.getUserVideoByWatched(user);
	    List<UserLecture> pieUserLec = userLectureService.getMyList(user);
	    Map<String, Integer> pieGraph = new HashMap<>();

	    // UserLecture 리스트를 순회합니다.
	    for (UserLecture userLecture : pieUserLec) {
	        Lecture lecture = userLecture.getLecture(); // Lecture 객체 가져오기
	        pieGraph.put(lecture.getTitle(), 0); // 초기 값 0으로 설정
	    }

	    // UserVideo 리스트를 순회합니다.
	    for (UserVideo userVideo : pieUserVideo) {
	        Lecture lecture = userVideo.getVideo().getLecture(); // Video의 Lecture 객체 가져오기
	        if (pieGraph.containsKey(lecture.getTitle())) {
	            pieGraph.put(lecture.getTitle(), pieGraph.get(lecture.getTitle()) + 1); // 값 증가
	        }
	    }
	    
	    model.addAttribute("pieGraph", pieGraph);

		// 성장로그
//        List<String> recentlyCompletedLectures = userVidService.getRecentlyCompletedLectures(user.getId());
//        model.addAttribute("recentlyCompletedLectures", recentlyCompletedLectures);
        
//        List<Object[]> recentlyCompletedLectures = userVidService.getRecentlyCompletedLectures(user.getId());
//        model.addAttribute("recentlyCompletedLectures", recentlyCompletedLectures);
	    List<Object[]> recentlyCompletedLectures = userLectureService.recentProgress(user);
	    model.addAttribute("recentlyCompletedLectures", recentlyCompletedLectures);
	    
	    // 일별 학습 현황 데이터 추가
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(6);
        Map<String, Integer> dailyWatchCount = userVidService.getDailyWatchCount(user, startDate, endDate);
        model.addAttribute("dailyWatchCount", dailyWatchCount);

        
		return "mypage/dashboard";
	}
	
}
