package com.lms.sc.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.lms.sc.entity.Lecture;
import com.lms.sc.entity.Question;
import com.lms.sc.entity.SiteUser;
import com.lms.sc.entity.Video;



public interface QuestionRepository extends JpaRepository<Question, Integer>{
//	@EntityGraph(attributePaths = {"answerList"})
	Page<Question> findAll(Pageable pageable);
	
	void deleteAllByAuthor(SiteUser user);
	
	@Query(value = "SELECT q FROM Question q LEFT JOIN FETCH q.answerList WHERE q.result = :result",
		       countQuery = "SELECT COUNT(q) FROM Question q WHERE q.result = :result")
	Page<Question> findByResultWithAnswers(@Param("result") boolean result, Pageable pageable);
	
	void deleteAllByVideo(Video video);
	
	@Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.answerList WHERE q.author = :author ORDER BY q.createDate DESC")
	Page<Question> findByAuthor(@Param("author") SiteUser author, Pageable pageable);
	
	List<Question> findByAuthorAndVideo_Lecture(SiteUser author, Lecture lecture);
	List<Question> findByVideo_Lecture(Lecture lecture);
	
	@Query("SELECT q FROM Question q WHERE q.author = :author ORDER BY q.createDate DESC")
	List<Question> findTop3ByAuthor(@Param("author") SiteUser author, Pageable pageable);
  
	List<Question> findByTitleContainingAndContentContainingAndAuthor(String title, String content, SiteUser author);
	
	@Query("SELECT q FROM Question q WHERE q.title LIKE %:keyword% OR q.content LIKE %:keyword% OR q.author.name LIKE %:keyword%")
    Page<Question> searchQuestions(@Param("keyword") String keyword, Pageable pageable);
	
	@Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.answerList WHERE q.title LIKE %:keyword% OR q.content LIKE %:keyword%")
	Page<Question> findByKeywordWithAnswers(@Param("keyword") String keyword, Pageable pageable);
  
	@Query("SELECT q FROM Question q WHERE q.author = :author AND q.video.lecture = :lecture ORDER BY q.createDate DESC")
	List<Question> findByAuthorAndLectureOrderByCreateDateAsc(@Param("author") SiteUser author, @Param("lecture") Lecture lecture);

	List<Question> findAllByVideo(Video video);
	
	@Modifying
    @Query("UPDATE Question q SET q.video = null WHERE q.video = :video")
    void nullifyVideoReference(@Param("video") Video video);
}
