package com.lms.sc.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.lms.sc.entity.Lecture;
import com.lms.sc.entity.SiteUser;
import com.lms.sc.entity.Video;
import com.lms.sc.repository.LectureRepository;
import com.lms.sc.repository.NoteRepository;
import com.lms.sc.repository.QuestionRepository;
import com.lms.sc.repository.UserLectureRepository;
import com.lms.sc.repository.UserVideoRepository;
import com.lms.sc.repository.VideoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LectureService {
	private final LectureRepository lecRepo;
	private final VideoRepository videoRepo;
	private final NoteRepository noteRepo;
	private final UserLectureRepository userLecRepo;
	private final UserVideoRepository userVidRepo;
	private final QuestionRepository questRepo;
	
	@Value("${file.upload-dir-lecture}")
	private String uploadDir;
	
	//강의 아이디 가져오기
	@Transactional
	public Lecture getLecture(long id) throws Exception {
		Optional<Lecture> op = lecRepo.findById(id);
	    if (op.isPresent()) {
	        Lecture lecture = op.get();
	        return lecture;
	    } else {
	        throw new NoSuchElementException("해당 ID의 강의를 찾을 수 없습니다.");
	    }
	}
	
	//강의 등록
	public Lecture regLecture(String title, String content) {
		Lecture lecture = new Lecture();
		lecture.setTitle(title);
		lecture.setContent(content);
		lecture.setCreateDate(LocalDateTime.now());
		return lecRepo.save(lecture);
	}
	
	// 썸네일 업데이트
	public Lecture updatethumnail(Long lecId, MultipartFile file) throws IOException {
        Lecture lecture = lecRepo.findById(lecId).get();
        
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + fileExtension;
        
        // file: 접두사 제거
        String cleanUploadDir = uploadDir.startsWith("file:") ? uploadDir.substring(5) : uploadDir;
        
        Path uploadPath = Paths.get(cleanUploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Path targetLocation = uploadPath.resolve(newFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        lecture.setThumnailUrl("/images/lecture/" + newFileName);
        return lecRepo.save(lecture);
    }
	
	//강의 리스트
	public List<Lecture> lecList(){
		return lecRepo.findAll();
	}
	
	// 강의를 유저도 함께 가져오기
//	@Transactional(readOnly = true)
//	public Lecture getLectureWithStu(long id) {
//		return lecRepo.findByIdWithStudents(id).orElseThrow(() -> new EntityNotFoundException("강의가 없습니다."));
//		
//	}
	
	// 강의 시작
	@Transactional
	public void studentAdd(Lecture lecture, SiteUser student) {
//		lecture.getStudents().add(student);
		lecRepo.save(lecture);
	}
	
	// 강의 유저 수정사항 저장
	public void lectureSave(Lecture lecture) {
		lecRepo.save(lecture);
	}
	
	
	//강의 수정
	public void modify(Lecture lecture, String title, String content) {
		lecture.setTitle(title);
		lecture.setContent(content);
		
		lecRepo.save(lecture);
	}
	
	// 강의 삭제
	@Transactional
	public void remove(Lecture lecture) {
		List<Video> videoList = videoRepo.findAllByLecture(lecture);
		for (Video video : videoList) {
			noteRepo.deleteAllByVideo(video);
			userVidRepo.deleteAllByVideo(video);
			questRepo.deleteAllByVideo(video);
		}
		videoRepo.deleteAllByLecture(lecture);
		userLecRepo.deleteAllByLecture(lecture);
		// video 삭제와 마찬가지로 lecture도 lectureId를 외래키로 사용하는 video를 모두 삭제후 강의 삭제
		lecRepo.delete(lecture);
	}
	
	// 강의 중복신청 방지
	public boolean isAlreadyRegistered(SiteUser user, Lecture lecture) {
	    // user와 lecture 조합으로 UserLecture 엔티티를 검색하여 존재 여부 확인
	    return userLecRepo.existsByUserAndLecture(user, lecture);
	}
}
