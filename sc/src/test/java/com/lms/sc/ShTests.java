package com.lms.sc;



import java.time.Instant;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.lms.sc.entity.Lecture;
import com.lms.sc.entity.Question;
import com.lms.sc.entity.SiteUser;
import com.lms.sc.entity.UserVideo;
import com.lms.sc.entity.Video;
import com.lms.sc.repository.LectureRepository;
import com.lms.sc.repository.QuestionRepository;
import com.lms.sc.repository.UserRepository;
import com.lms.sc.repository.UserVideoRepository;
import com.lms.sc.repository.VideoRepository;
import com.lms.sc.service.LectureService;
import com.lms.sc.service.UserVideoService;


@SpringBootTest
class ShTests {

	@Autowired
	private LectureRepository lr;
	
	@Autowired
	private VideoRepository lvr;
	
	@Autowired
	private UserRepository ur;
	
	@Autowired
	private LectureService ls;
	
	@Autowired
	private UserVideoService uvs;
	
	@Autowired
	private UserVideoRepository uvr;
	
	@Autowired
	private QuestionRepository qs;
	
	@Autowired
	private VideoRepository vr;
	
	@Test
	public void createTest() {
        Question question = new Question();
        SiteUser user = ur.getById(5L);
        Video video = vr.getById(68L);
        question.setTitle("제목");
        question.setContent("내용");
        question.setAuthor(user);
        question.setVideo(video);
        question.setCreateDate(LocalDateTime.now());
        qs.save(question);
    }
	
	
//	@Test
//	void userVideoTest() {
//		SiteUser user = ur.findById(4L).orElse(null);
//        Video video = lvr.findById(4L).orElse(null);
//        boolean watched = true;
//        Instant watchedAt = Instant.now();
//        Integer watchingTime = 0;
//        uvs.saveUserVideo(user, video, watched, watchedAt, watchingTime);
//	}
	
	//@Test
	void uvTest() {
		SiteUser user = ur.findById(4L).orElse(null);
		Video video = lvr.findById(3L).orElse(null);
		UserVideo uv = new UserVideo();
		uv.setId(1L);
		uv.setUser(user);
		uv.setVideo(video);
		uv.setWatched(true);
//		uv.setWatchedAt(Instant.now());
		
		uvr.save(uv);
	}
	
//	@Test
//	@Transactional
	void startLearn() {
		Lecture lec = lr.findById(1L).get();
		SiteUser stu = ur.findById(1).get();
//		ls.learnStart(lec, stu);
	}
	
	//@Test
	void lectureTest() {
		Lecture lec = new Lecture();
		lec.setTitle("python 강의");
		lec.setContent("python을 잘 배워볼까요~?");
		lec.setCreateDate(LocalDateTime.now());
		
		
        System.out.println("@@@@@@@@@@@Saved Lecture: " + lr.save(lec));
	}
	
//	@Test
	void lecVideoTest() {
		Lecture lecture = lr.findById(1L).orElse(null);
		Video lecVideo = new Video();
		lecVideo.setTitle("자바");
		lecVideo.setUrl("123123123");
		lecVideo.setLecture(lecture);
		
		lvr.save(lecVideo);
	}

}
