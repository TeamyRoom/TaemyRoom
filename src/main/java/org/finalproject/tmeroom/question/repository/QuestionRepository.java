package org.finalproject.tmeroom.question.repository;

import org.finalproject.tmeroom.lecture.data.entity.Lecture;
import org.finalproject.tmeroom.member.data.entity.Member;
import org.finalproject.tmeroom.question.data.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    Page<Question> findByLecture(Lecture lecture, Pageable pageable);

    @Query("SELECT q FROM Question q WHERE q.author = :author OR (q.lecture = :lecture AND q.isPublic = :isPublic)")
    Page<Question> findByAuthorOrLectureAndIsPublicTrue(Member author, Lecture lecture, Boolean isPublic, Pageable pageable);
}
