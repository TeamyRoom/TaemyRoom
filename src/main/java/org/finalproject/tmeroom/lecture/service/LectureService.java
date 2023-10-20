package org.finalproject.tmeroom.lecture.service;

import lombok.RequiredArgsConstructor;
import org.finalproject.tmeroom.common.exception.ApplicationException;
import org.finalproject.tmeroom.common.exception.ErrorCode;
import org.finalproject.tmeroom.lecture.data.dto.request.LectureCreateRequestDto;
import org.finalproject.tmeroom.lecture.data.dto.request.LectureUpdateRequestDto;
import org.finalproject.tmeroom.lecture.data.dto.response.LectureAccessResponseDTO;
import org.finalproject.tmeroom.lecture.data.dto.response.LectureCreateResponseDto;
import org.finalproject.tmeroom.lecture.data.entity.Lecture;
import org.finalproject.tmeroom.lecture.repository.LectureRepository;
import org.finalproject.tmeroom.lecture.repository.StudentRepository;
import org.finalproject.tmeroom.lecture.repository.TeacherRepository;
import org.finalproject.tmeroom.member.data.dto.MemberDto;
import org.finalproject.tmeroom.member.data.entity.Member;
import org.finalproject.tmeroom.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class LectureService extends LectureCommon {
    private final LectureRepository lectureRepository;
    private final MemberRepository memberRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    //강의 생성
    public LectureCreateResponseDto createLecture(LectureCreateRequestDto requestDTO) {
        Member manager = memberRepository.findById(requestDTO.getMemberDTO().getId()).orElseThrow();

        Lecture lecture = requestDTO.toEntity(makeHashCode(), manager);
        lectureRepository.save(lecture);

        return LectureCreateResponseDto.from(lecture);
    }

    //강의 삭제
    public void deleteLecture(String lectureCode, MemberDto memberDTO) {
        Lecture lecture = lectureRepository.getReferenceById(lectureCode);

        checkPermission(lecture, memberDTO);

        lectureRepository.delete(lecture);
    }

    //강의 정보 수정
    public void updateLecture(LectureUpdateRequestDto requestDTO) {
        Lecture lecture = lectureRepository.findById(requestDTO.getLectureCode())
                .orElseThrow(() -> new ApplicationException(ErrorCode.INVALID_LECTURE_CODE));

        checkPermission(lecture, requestDTO.getMemberDTO());

        lecture.update(requestDTO);
    }

    //서비스 함수
    private String makeHashCode() {
        String hashCode;
        do {
            hashCode = UUID.randomUUID().toString().substring(0, 8);
        } while (isDuplicateLectureCode(hashCode));
        return hashCode;
    }

    private boolean isDuplicateLectureCode(String hashCode) {
        List<String> lectureCodes = lectureRepository.findAllLectureCode();
        return lectureCodes.contains(hashCode);
    }

    public LectureAccessResponseDTO accessLecture(String lectureCode, MemberDto memberDto) {
        Lecture lecture = lectureRepository.findById(lectureCode)
                .orElseThrow(() -> new ApplicationException(ErrorCode.INVALID_LECTURE_CODE));

        Member member = memberRepository.findById(memberDto.getId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        Role userRole = getUserRole(lecture, member);

        return LectureAccessResponseDTO.builder()
                .lectureName(lecture.getLectureName())
                .nickName(member.getNickname())
                .role(userRole.toString().toLowerCase())
                .build();
    }

    private Role getUserRole(Lecture lecture, Member member) {
        if (lecture.getManager().isIdMatch(member.getId())) {
            return Role.MANAGER;
        } else if (teacherRepository.existsByMemberAndLecture(member, lecture)) {
            return Role.TEACHER;
        } else if (studentRepository.existsByMemberAndLecture(member, lecture)) {
            return Role.STUDENT;
        }

        throw new ApplicationException(ErrorCode.INVALID_ACCESS_PERMISSION);
    }

    public enum Role {
        MANAGER,
        TEACHER,
        STUDENT
    }
}
