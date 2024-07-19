package com.lms.sc.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lms.sc.entity.Lecture;
import com.lms.sc.entity.Note;
import com.lms.sc.entity.SiteUser;
import com.lms.sc.entity.Video;


public interface NoteRepository extends JpaRepository<Note, Long> {
	List<Note> findByAuthor(SiteUser author);
	List<Note> findByVideo(Video video);
	void deleteAllByAuthor(SiteUser user);
	
	// 비디오로 노트를 삭제하는 것
	void deleteAllByVideo(Video video);
	
    @Query("SELECT DISTINCT v "
            + "FROM Note n "
            + "JOIN n.video v "
            + "WHERE n.author = :author AND v.lecture = :lecture")
	List<Video> findVideosByAuthorAndLecture(@Param("lecture") Lecture lecture, 
			@Param("author") SiteUser author);
	
	List<Note> findByVideoAndAuthor(Video video, SiteUser author);
	
	@Query("SELECT n FROM Note n WHERE n.video = :video AND n.author = :author ORDER BY n.videoTime ASC")
	List<Note> findByVideoAndAuthorOrderByVideoTime(@Param("video") Video video, @Param("author") SiteUser author);
	
	@Query("SELECT DISTINCT l "
		       + "FROM Note n "
		       + "JOIN n.video v "
		       + "JOIN v.lecture l "
		       + "WHERE n.author = :author")
	List<Lecture> findLecturesByAuthor(@Param("author") SiteUser author);

	@Query("SELECT n "
		       + "FROM Note n "
		       + "JOIN n.video v "
		       + "WHERE v.lecture = :lecture AND n.author = :author")
	List<Note> findByLectureAndAuthor(@Param("lecture") Lecture lecture, 
			@Param("author") SiteUser author);

	@Query("SELECT n FROM Note n WHERE n.author = :author ORDER BY n.createDate DESC")
    List<Note> findTop3ByAuthor(@Param("author") SiteUser author, Pageable pageable);
	
	
	@Modifying
    @Query("UPDATE Note n SET n.video = null WHERE n.video = :video")
    void nullifyVideoReference(@Param("video") Video video);
}
