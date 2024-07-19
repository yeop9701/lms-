package com.lms.sc.repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lms.sc.entity.Lecture;
import com.lms.sc.entity.SiteUser;
import com.lms.sc.entity.UserVideo;
import com.lms.sc.entity.Video;


public interface UserVideoRepository extends JpaRepository<UserVideo, Long> {
	
	Optional<UserVideo> findByUserAndVideo(SiteUser user, Video video);
	
	long countByUserAndWatchedTrue(SiteUser user);
	
	void deleteAllByVideo(Video video);
	
	void deleteByVideoAndUser(Video video, SiteUser user);

	void deleteAllByUser(SiteUser user);
	
	@Query("SELECT uv FROM UserVideo uv JOIN uv.video v JOIN v.lecture l WHERE uv.user = :user AND l = :lecture AND uv.watched = :watched")
    List<UserVideo> findByUserAndLectureAndWatched(
            @Param("user") SiteUser user,
            @Param("lecture") Lecture lecture,
            @Param("watched") boolean watched);

	List<UserVideo> findByUserAndWatched(SiteUser user, boolean watched);
	
	@Query("SELECT uv FROM UserVideo uv JOIN FETCH uv.video WHERE uv.user = :user AND uv.watched = :watched")
	List<UserVideo> findByUserAndWatchedWithVideo(@Param("user") SiteUser user, @Param("watched") boolean watched);
	
	@Query("SELECT uv FROM UserVideo uv JOIN FETCH uv.video v JOIN FETCH v.lecture WHERE uv.user = :user AND uv.watched = true")
	List<UserVideo> findByUserAndWatchedWithVideoAndLecture(@Param("user") SiteUser user);
	
	List<UserVideo> findByUserAndWatchedAtBetween(SiteUser user, Date startDate, Date endDate);
	
//	@Query("SELECT uv FROM UserVideo uv WHERE uv.user = :user ORDER BY uv.watchedAt ASC")
//	List<UserVideo> findTop3ByAuthorUserVideos(@Param("user") SiteUser user);
	
//	@Query("SELECT uv FROM UserVideo uv JOIN FETCH uv.video WHERE uv.user = :user ORDER BY uv.watchedAt ASC")
//	List<UserVideo> findTop3ByAuthorUserVideos(@Param("user") SiteUser user);
	
	@Query("SELECT uv FROM UserVideo uv JOIN FETCH uv.video v JOIN FETCH v.lecture WHERE uv.user = :user AND uv.watchingTime > 0 ORDER BY uv.watchedAt DESC")
	List<UserVideo> findTop3ByAuthorUserVideos(@Param("user") SiteUser user, Pageable pageable);

	// 성장 로그
//	@Query("SELECT l.title FROM Lecture l WHERE l.id IN " +
//	           "(SELECT DISTINCT v.lecture.id FROM Video v WHERE v.lecture.id IN " +
//	           "(SELECT DISTINCT uv.video.lecture.id FROM UserVideo uv WHERE uv.user.id = :userId) " +
//	           "GROUP BY v.lecture.id " +
//	           "HAVING COUNT(v) = " +
//	           "(SELECT COUNT(uv) FROM UserVideo uv WHERE uv.user.id = :userId AND uv.video.lecture.id = v.lecture.id AND uv.watched = true))" +
//	           "ORDER BY l.id DESC")
//	 List<String> findRecentlyCompletedLectureTitles(@Param("userId") Long userId, Pageable pageable);
	
	@Query("SELECT DISTINCT v.lecture.title, MAX(uv.watchedAt) " +
		       "FROM UserVideo uv JOIN uv.video v " +
		       "WHERE uv.user.id = :userId AND uv.watched = true " +
		       "GROUP BY v.lecture.title " +
		       "ORDER BY MAX(uv.watchedAt) DESC")
	List<Object[]> findRecentlyCompletedLectureTitlesAndDates(@Param("userId") long userId, Pageable pageable);
	
	//일별 학습 현황 쿼리문
    @Query("SELECT DATE(uv.watchedAt) as date, COUNT(uv) as count FROM UserVideo uv WHERE uv.user = :user AND uv.watchedAt BETWEEN :startDate AND :endDate GROUP BY DATE(uv.watchedAt)")
    List<Object[]> getDailyWatchCount(@Param("user") SiteUser user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    //비디오 삭제 시 유저 관련
    @Modifying
    @Query("DELETE FROM UserVideo uv WHERE uv.video = :video")
    void deleteByVideo(@Param("video") Video video);
}
