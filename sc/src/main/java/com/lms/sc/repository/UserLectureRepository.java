package com.lms.sc.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lms.sc.entity.Lecture;
import com.lms.sc.entity.SiteUser;
import com.lms.sc.entity.UserLecture;

public interface UserLectureRepository extends JpaRepository<UserLecture, Long> {
	List<UserLecture> findByUser(SiteUser user);
	
	void deleteAllByUser(SiteUser user);
	
	Optional<UserLecture> findByUserAndLecture(SiteUser user, Lecture lecture);
	
	void deleteAllByLecture(Lecture lecture);
	
	List<UserLecture> findAllByUser(SiteUser user);
//	@Query("SELECT ul.video FROM UserLecture ul WHERE ul.user = :user")
//	List<Video> findVideosByUser(@Param("user") SiteUser user);
	
	@Query("SELECT ul FROM UserLecture ul WHERE ul.user.email = :userEmail AND ul.lecture.id = :lecId")
    Optional<UserLecture> findByUserAndLecture(@Param("userEmail") String userEmail, @Param("lecId") long lecId);
//	Optional<UserLecture> findByUserLecture(String userEmail, long lecId);
	
	boolean existsByUserAndLecture(SiteUser user, Lecture lecture);
	
	List<UserLecture> findByLecture(Lecture lecture);
	
	@Query("SELECT ul, MAX(uv.watchedAt) as lastWatchedAt " +
		       "FROM UserLecture ul " +
		       "LEFT JOIN UserVideo uv ON uv.video.lecture = ul.lecture AND uv.user = ul.user " +
		       "WHERE ul.user = :user AND ul.progress = :progress " +
		       "GROUP BY ul " +
		       "ORDER BY lastWatchedAt DESC")
		List<Object[]> findByUserAndProgressWithLastWatchedAt(@Param("user") SiteUser user, 
		                                                      @Param("progress") double progress, 
		                                                      Pageable pageable);
}
