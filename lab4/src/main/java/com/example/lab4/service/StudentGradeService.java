package com.example.lab4.service;

import com.example.lab4.dto.GradeEvent;
import com.example.lab4.entity.Grade;
import com.example.lab4.repository.CourseRepository;
import com.example.lab4.repository.CourseStudentRepository;
import com.example.lab4.repository.GradeRepository;
import com.example.lab4.repository.PackRepository;
import com.example.lab4.repository.StudentRepository;
import io.micrometer.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentGradeService {

    Logger logger = LoggerFactory.getLogger(StudentGradeService.class);

    @Transactional
    public void handleGradeEvent(GradeEvent event) {
        saveGradeIfCompulsoryWithIds(event.getStudentId(), event.getCourseId(), event.getGrade(), "EVENT");
    }

    @Transactional
    public void saveFromCsvRow(String studentCode, String courseCode, BigDecimal grade) {
        saveGradeIfCompulsory(studentCode, courseCode, grade, "CSV");
    }

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    GradeRepository gradeRepository;

    @Autowired
    PackRepository packRepository;
    @Autowired
    CourseStudentRepository courseStudentRepository;

    private void saveGradeIfCompulsory(String studentCode,
                                       String courseCode,
                                       BigDecimal gradeValue,
                                       String source) {
        if (StringUtils.isEmpty(studentCode) || !studentRepository.existsByCode(studentCode)) {
            logger.info( "Unknown student " + courseCode + " for grade from " + source);
            return;
        }
        if (StringUtils.isEmpty(courseCode) || !courseRepository.existsByCode(courseCode)) {
            logger.info( "Unknown course " + courseCode + " for grade from " + source);
            return;
        }
        if (packRepository.findByCourseCode(courseCode) != null) {
            logger.info( "Not compulsory " + courseCode + " course " + source);
            return;
        }
        if (gradeRepository.findByStudentCodeAndCourseCode(courseCode, studentCode) != null) {
            logger.info( "Grade is already added for student  " + courseCode + "on  course " + courseCode);
        }

        Grade grade = new Grade();
        grade.setGrade(gradeValue);
        grade.setCourse(courseRepository.findByCode(courseCode));
        grade.setStudent(studentRepository.findByCode(studentCode));

        gradeRepository.save(grade);
    }


    private void saveGradeIfCompulsoryWithIds(Long studentId,
                                       Long courseId,
                                       BigDecimal gradeValue,
                                       String source) {
        if (courseId == null) {
            logger.info( "Unknown student " + courseId + " for grade from " + source);
            return;
        }
        if (studentId == null) {
            logger.info( "Not compulsory " + studentId + " course " + source);
            return;
        }

        Grade grade = new Grade();
        grade.setGrade(gradeValue);
        grade.setCourse(courseRepository.findById(courseId).get());
        grade.setStudent(studentRepository.findById(studentId).get());

        gradeRepository.save(grade);
    }


    @Transactional(readOnly = true)
    public List<GradeEvent> findGrades(String studentCode, String courseCode) {
        List<Grade> grades =  gradeRepository.findByStudentCodeAndCourseCode(studentCode, courseCode);
        return grades.stream().map(grade -> new GradeEvent(grade.getStudent().getCode(), grade.getCourse().getCode(), grade.getGrade())).collect(Collectors.toList());
    }
}
