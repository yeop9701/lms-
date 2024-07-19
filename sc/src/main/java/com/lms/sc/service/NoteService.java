package com.lms.sc.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.lms.sc.entity.Lecture;
import com.lms.sc.entity.Note;
import com.lms.sc.entity.SiteUser;
import com.lms.sc.entity.Video;
import com.lms.sc.repository.LectureRepository;
import com.lms.sc.repository.NoteRepository;
import com.lms.sc.repository.UserRepository;
import com.lms.sc.repository.VideoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoteService {
	private final NoteRepository noteRepository;
	private final UserRepository userRepository;
	private final LectureRepository lectureRepository;
	private final VideoRepository videoRepository;
	// 노트생성
	public Note createNote(String content, long videoTime, SiteUser author, Video video) {
		Note note = new Note();
		note.setContent(content);
		note.setVideoTime(videoTime);
		note.setAuthor(author);
		note.setVideo(video);
		note.setCreateDate(LocalDateTime.now());
		return noteRepository.save(note);
	}
	
	// 노트하나 가져오기
	public Note getNote(long noteId) {
		return noteRepository.findById(noteId).get();
	}
	
	// 노트삭제
	public void delNote(long id) {
		Note note = noteRepository.findById(id).get();
		noteRepository.delete(note);
	}
	
	// 노트 수정
	public void updateNote(Note note, String content) {
		note.setContent(content);
		noteRepository.save(note);
	}
	
	// 노트 리스트
	public List<Note> getNoteList(long userId){
		Optional<SiteUser> user = userRepository.findById(userId);
		List<Note> noteList = noteRepository.findByAuthor(user.get());
		return noteList;
	}
	
	// 노트 강의 리스트
	public List<Lecture> getNoteLecture(long userId) {
		SiteUser user = userRepository.findById(userId).get();
		return noteRepository.findLecturesByAuthor(user);
	}
	
	// 노트 영상 리스트
	public List<Video> getVideosByLecture(long lecId, long userId){
		SiteUser author = userRepository.findById(userId).get();
		Lecture lecture = lectureRepository.findById(lecId).get();
		return noteRepository.findVideosByAuthorAndLecture(lecture, author);
	}
	
	// 영상으로 찾는 노트 리스트
	public List<Note> getByVideo(long videoId, long userId){
		SiteUser author = userRepository.findById(userId).get();
		Video video = videoRepository.findById(videoId).get();
		return noteRepository.findByVideoAndAuthorOrderByVideoTime(video, author);
	}
	
	// 강의로 찾는 노트 리스트
	public List<Note> getByLecture(long lecId, long userId) {
		SiteUser author = userRepository.findById(userId).get();
		Lecture lecture = lectureRepository.findById(lecId).get();
		return noteRepository.findByLectureAndAuthor(lecture, author);
	}
	
	public List<Note> getRecentNotes(SiteUser author) {
		Pageable pageable = PageRequest.of(0, 3);
		return noteRepository.findTop3ByAuthor(author, pageable);
	}
}
