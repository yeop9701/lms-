package com.lms.sc.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import com.lms.sc.entity.Lecture;
import com.lms.sc.entity.Note;
import com.lms.sc.entity.SiteUser;
import com.lms.sc.entity.Video;
import com.lms.sc.service.NoteService;
import com.lms.sc.service.UserService;
import com.lms.sc.service.VideoService;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
@RequestMapping("/note")
public class NoteController {
	
	private final NoteService noteService;
	private final UserService userService;
	private final VideoService videoService;
	
	// 강의 별 노트 갯수 리스트
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/list")
	public String getNoteList(Principal principal, Model model) {
		if (principal == null) {
			return "user/login";
		}
		SiteUser user = userService.getUserByEmail(principal.getName());
		long userId = user.getId();
		List<Lecture> noteLecture = noteService.getNoteLecture(userId);
		
		Map<Lecture, Integer> noteList = new LinkedHashMap<>();
		noteLecture.forEach(lecture -> 
			noteList.put(lecture, noteService.getByLecture(lecture.getId(), userId).size()));
		model.addAttribute("userId", userId);
		model.addAttribute("noteList", noteList);
		return "note/note_list";
	}
	
	// 강의의 영상별 노트 리스트
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/list/{lecId}")
	public String notesInLecture(Principal principal,
			@PathVariable("lecId") long lecId, Model model) {
		if (principal == null) {
			return "user/login";
		}
		SiteUser user = userService.getUserByEmail(principal.getName());
		List<Video> videoList = noteService.getVideosByLecture(lecId, user.getId());
		Map<Video, List<Note>> noteList = new LinkedHashMap<>();
		
		videoList.forEach(video -> noteList.put(video, noteService.getByVideo(video.getId(), user.getId())));
		
		model.addAttribute("noteList", noteList);
		
		return "note/note_lecture";
	}
	
	// 노트 등록
//	@PreAuthorize("isAuthenticated()")
//	@PostMapping("/create/{videoId}")
//	@ResponseBody
//	public ResponseEntity<?> createNote(@PathVariable("videoId") long videoId, @RequestBody Map<String, Object> payload, Principal principal) throws Exception {
//		asyncService.executeAsyncTask();
//		SiteUser author = userService.getUserByEmail(principal.getName());
//		Video video = videoService.getVideo(videoId);
//		
//		String content = (String) payload.get("content");
//		long videoTime = 0;
//		
//		noteService.createNote(content, videoTime, author, video);
//		return ResponseEntity.ok("노트가 생성 되었습니다.");
//	}
	
	// 노트 등록
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create/{videoId}")
	@ResponseBody
	public ResponseEntity<?> createNote(@PathVariable("videoId") long videoId, @RequestBody Map<String, Object> payload, Principal principal) throws Exception {
//	    asyncService.executeAsyncTask();
	    SiteUser author = userService.getUserByEmail(principal.getName());
	    Video video = videoService.getVideo(videoId);
	    
	    String content = (String) payload.get("content");
	    long videoTime = Math.round((double) payload.get("videoTime"));
	    Note newNote = noteService.createNote(content, videoTime, author, video);

	    // 새로운 노트 정보를 JSON으로 반환
	    Map<String, Object> response = new HashMap<>();
	    response.put("content", newNote.getContent());
	    response.put("id", newNote.getId());
	    response.put("videoTime", newNote.getVideoTime());
	    // 필요에 따라 다른 필드도 추가할 수 있습니다.

	    return ResponseEntity.ok(response);
	}
	
	// 노트 삭제
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/delete/{noteId}")
	@ResponseBody
	public ResponseEntity<Void> delNoteInViewer(@PathVariable("noteId") long noteId, Principal principal) {
		Note note = noteService.getNote(noteId);
		if (!note.getAuthor().getEmail().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제 권한이 없습니다.");
		}
		noteService.delNote(noteId);
		return ResponseEntity.ok().build();
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{noteId}")
	public String delNoteInMypage(@PathVariable("noteId") long noteId, Principal principal) {
		Note note = noteService.getNote(noteId);
		if (!note.getAuthor().getEmail().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제 권한이 없습니다.");
		}
		noteService.delNote(noteId);
		return "redirect:/note/list/" + note.getVideo().getLecture().getId();
	}
}
