package com.lms.sc.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.lms.sc.entity.Lecture;
import com.lms.sc.entity.Note;
import com.lms.sc.entity.Question;
import com.lms.sc.entity.SiteUser;
import com.lms.sc.entity.UserLecture;
import com.lms.sc.entity.UserVideo;
import com.lms.sc.entity.Video;
import com.lms.sc.service.LectureService;
import com.lms.sc.service.NoteService;
import com.lms.sc.service.QuestionService;
import com.lms.sc.service.UserLectureService;
import com.lms.sc.service.UserService;
import com.lms.sc.service.UserVideoService;
import com.lms.sc.service.VideoService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;



@RequestMapping("/video")
@RequiredArgsConstructor
@Controller
public class VideoController {
	private final VideoService videoService;
	private final LectureService lectureService;
	private final NoteService noteService;
	private final UserService userService;
	private final UserVideoService userVideoService;
	private final UserLectureService userLectureService;
	private final QuestionService questionService;
	
	//비디오 하나
	@GetMapping("/viewer/{vidId}")
	public String getVideo(Model model, @PathVariable("vidId") long vidId,
			@RequestParam(value = "n", required = false) Long noteId,
			@RequestParam(value = "time", required = false) Integer savedTime,
			Principal principal) throws Exception {
		
		if(principal == null) {
			return "redirect:/user/login";
		}
		
		Video video = videoService.getVideo(vidId);
		Lecture lecture = lectureService.getLecture(video.getLecture().getId());
		
		model.addAttribute("video", video);
		model.addAttribute("lecture", lecture);
		
		boolean hasPreVideo = true;// 다음 비디오 존재 여부를 확인하는 로직
		boolean hasNextVideo = true;// 다음 비디오 존재 여부를 확인하는 로직
		Video nextVideo = videoService.getNextVideo(vidId);
		if (nextVideo != null) {
			hasNextVideo = false;
		}
		Video preVideo = videoService.getPreVideo(vidId);
		if (preVideo != null) {
			hasPreVideo = false;
		}
	    model.addAttribute("hasNextVideo", hasNextVideo);
	    model.addAttribute("hasPreVideo", hasPreVideo);
		
		SiteUser user = userService.getUserByEmail(principal.getName());
		List<Note> noteList = noteService.getByVideo(vidId, user.getId());
		model.addAttribute("noteList", noteList);
		model.addAttribute("user", user);
		
		UserVideo userVideo = userVideoService.getUserVideoOrNew(user, video);
		model.addAttribute("userVideo", userVideo);
		
		if (savedTime != null) {
	        model.addAttribute("videoTime", savedTime);
	    } else if (noteId != null) {
	    	try {
	            Note note = noteService.getNote(noteId);
	            if (note != null) {
	                model.addAttribute("videoTime", note.getVideoTime());
	            } else {
	                model.addAttribute("videoTime", userVideo.getWatchingTime());
	            }
	        } catch (Exception e) {
	            // 노트를 찾지 못했을 때의 처리
	            model.addAttribute("videoTime", userVideo.getWatchingTime());
	        }
	    } else {
	        model.addAttribute("videoTime", userVideo.getWatchingTime());
	    }
		
		List<UserLecture> userLectureList = userLectureService.getMyList(user);
		
//		Map<UserLecture, Map<Video, UserVideo>> list = new LinkedHashMap<UserLecture, Map<Video, UserVideo>>();
		Map<UserLecture, Map<Integer, Integer>> progressMap = new HashMap<>();
		
		for (UserLecture userLecture: userLectureList) {
			List<Video> videoList = videoService.VideoList(lecture);
			Map<Video, UserVideo> videoMap = new LinkedHashMap<>();
			model.addAttribute("videoMap", videoMap);
			int watched = 0;
			for (Video vid : videoList) {
				UserVideo uv = userVideoService.getUserVideoOrNew(user, vid);
				videoMap.put(vid, uv);
				
				if(uv.isWatched()) {
					watched++;
				}
			}
//			list.put(userLecture, videoMap);
			Map<Integer, Integer> progress = new HashMap<Integer, Integer>();
			progress.put(videoList.size(), watched);
			progressMap.put(userLecture, progress);
		}
		
//		model.addAttribute("list", list);
		model.addAttribute("progressMap", progressMap);
		
//		List<Question> qustList = questionService.getQusetionByUserAndLecture(user, lecture);
		List<Question> qustList = questionService.getQuestionByUserAndLectureSortedAsc(user, lecture);
		model.addAttribute("qustList", qustList);
		
		return "video/viewer4";
	}
	
	// 다음 비디오
	@GetMapping("viewer/{vidId}/next")
	public String viewNextVideo(@PathVariable("vidId") long vidId) {
		Video nextVideo = videoService.getNextVideo(vidId);
		if (nextVideo != null) {
			return "redirect:/video/viewer/" + nextVideo.getId();
		}
		// 다음 영상이 없을 경우 갈 페이지 수정해야함
		// 지금은 현재 영상으로 다시 로드하게 되어있음
		return "redirect:/video/viewer/" + vidId;
	}
	
	// 이전 비디오
	@GetMapping("viewer/{vidId}/pre")
	public String viewPreVideo(@PathVariable("vidId") long vidId) {
		Video preVideo = videoService.getPreVideo(vidId);
		if (preVideo != null) {
			return "redirect:/video/viewer/" + preVideo.getId();
		}
		// 이전 영상이 없을 경우 갈 페이지 수정해야함
		return "redirect:/video/viewer/" + vidId;
	}
	
	// 학습 목록에서 강의 클릭시 강의 영상 리스트화면 이동
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/study/list/{lecId}")
	public String studyVideos(Principal principal, Model model, @PathVariable("lecId") long lecId) throws Exception {
		if(principal == null) {
			return "redirect:/user/login";
		}
		SiteUser user = userService.getUserByEmail(principal.getName());
		Lecture lecture = lectureService.getLecture(lecId);
		List<Video> videoList = videoService.VideoList(lecture);
		Map<Video, UserVideo> userVideoList = new LinkedHashMap<>();
		
		model.addAttribute("videoList", videoList);
		for (Video video : videoList) {
			UserVideo userVid = userVideoService.getUserVideoOrNew(user, video);
			userVideoList.put(video, userVid);
		}
		
		model.addAttribute("userVideoList", userVideoList);
		return "mypage/my_lec_videos";
	}
	
	
	//강의 마다 영상 등록 페이지로 이동
	@GetMapping("/addVideo/{id}")
	public String addVideo(Model model, @PathVariable("id") long id) throws Exception {
		Lecture lecture = lectureService.getLecture(id);
		
		model.addAttribute("lecture", lecture);
		
		return "admin/video_form";
	}
	
	//비디오 등록
	@PostMapping("/addVideo/{lec_id}")
	public String regVideo(@PathVariable("lec_id") long lec_id,
							@RequestParam(name = "title[]") String[] title, @RequestParam(name = "url[]") String[] url) throws Exception {

		//VideoService.regVideo(title, url, lec_id);
		for (int i = 0; i < title.length; i++) {
	        videoService.regVideo(title[i], url[i], lec_id);
	    }
		
		return String.format("redirect:/video/list/%s", lec_id);
	}
	
	//등록된 영상 리스트
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/list/{lec_id}")
	public String videoList(Model model, @PathVariable("lec_id") long lec_id) throws Exception {
		Lecture lecture = lectureService.getLecture(lec_id);		
		List<Video> video = videoService.VideoList(lecture);
		model.addAttribute("lecture", lecture);
		model.addAttribute("video", video);
		return "admin/video_list";
	}
	
	// 등록된 비디오 삭제
//	@PreAuthorize("isAuthenticated()")
//	@GetMapping("/delete/{vid_id}")
//	@Transactional
//	public String deleteVideo(@PathVariable("vid_id") long vidId) throws Exception {
//		Video video = videoService.getVideo(vidId);
//		videoService.delVideo(video);
//		return "redirect:/video/list/" + video.getLecture().getId();
//	}
	
	//비디오 삭제
	@PreAuthorize("hasRole('ADMIN')") // 관리자만 삭제할 수 있도록 설정
	@GetMapping("/delete/{id}")
    public String deleteVideo(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) throws Exception {
		Video video = videoService.getVideo(id);
		long lecId = video.getLecture().getId();
		try {
            videoService.deleteVideo(id);
            
            System.out.println(redirectAttributes.addFlashAttribute("message", "비디오가 성공적으로 삭제되었습니다."));
            
        } catch (EntityNotFoundException e) {
        	System.out.println(redirectAttributes.addFlashAttribute("error", e.getMessage()));
        } catch (Exception e) {
        	System.out.println(redirectAttributes.addFlashAttribute("error", "비디오 삭제 중 오류가 발생했습니다."));
        }
        return "redirect:/video/list/" + lecId;  // 비디오 목록 페이지로 리다이렉트
    }
}
