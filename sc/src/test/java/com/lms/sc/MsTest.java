package com.lms.sc;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.lms.sc.entity.Lecture;
import com.lms.sc.entity.Note;
import com.lms.sc.entity.SiteUser;
import com.lms.sc.entity.Video;
import com.lms.sc.repository.UserRepository;
import com.lms.sc.repository.VideoRepository;
import com.lms.sc.service.LectureService;
import com.lms.sc.service.NoteService;
import com.lms.sc.service.UserService;
import com.lms.sc.service.VideoService;

@SpringBootTest
public class MsTest {
	@Autowired
	private UserRepository ur;
	
	@Autowired
	private VideoRepository vr;
	
	@Autowired
	private NoteService ns;
	
	@Autowired
	private UserService us;
	
	@Autowired
	private LectureService ls;
	
	@Autowired
	private VideoService vs;
	
	@Test
	void tellNumberCheck() {
		String result = us.checkPhone("01051567115");
		System.out.println(result);
	}
	
//	@Test
	void minseok() {
		SiteUser minseok = us.getUserByEmail("minseok@test.com");
		us.modify(minseok, "김민석", "1234", "0105156");
	}
	
//	@Test
	void adminUser() {
		us.create("admin", "admin", "1234", "01012340101", null);
	}
	
//	@Test
	void youtubeApi() {
		vs.regVideo("비오는날 듣기좋은 노래 모음", "MEpoa_afo0U", 9);
	}
	
//	@Test
	void startLearn(){
		SiteUser user = us.getUserByEmail("minseok@test.com");
		try {
			Lecture lec = ls.getLecture(1L);
			ls.studentAdd(lec, user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	@Test
	void getUser() {
		SiteUser user = us.getUserByEmail("minseok@test.com");
		System.out.println(user.getEmail());
		System.out.println(user.getId());
		System.out.println(user.getName());
	}
	
	
//	@Test
	void noteList() {
		List<Note> list = ns.getByLecture(1, 1);
		list.forEach(note -> System.out.println(note.getContent()));
	}
	
//	@Test
	void noteOfVideo() {
		List<Note> list = ns.getByVideo(1, 1);
		list.forEach(note -> System.out.println(note.getContent()));
	}
	
//	@Test
	void createNote() {
		String content = "";
		long videoTime = 100;
		SiteUser author = ur.findById(1).get();
		Video video = vr.findById(1).get();
		ns.createNote(content, videoTime, author, video);
	}
	
//	@Test
	void createUser() {
		SiteUser user = new SiteUser();
		user.setName("민석");
		user.setEmail("minseok@naver.com");
		user.setPassword("1234");
		user.setTellNumber("01051567115");
		ur.save(user);
	}
	
//	@Test
//	void createNote() {
//		String email = "minseok@naver.com";
//		SiteUser user = ur.findByEmail(email);
//		Note note = new Note();
//		note.setAuthor(user);
//		note.setContent("민석 노트 인설트 테스트");
//		nr.save(note);
//	}
}
