package com.example.lab4.service;

import com.example.lab4.dta.StudentChangeDetailsDta;
import com.example.lab4.dta.StudentRegisterDta;
import com.example.lab4.dto.CourseDefaultDto;
import com.example.lab4.dto.PackDetailDto;
import com.example.lab4.dto.StudentDefaultDto;
import com.example.lab4.dto.StudentDetailDto;
import com.example.lab4.entity.Course;
import com.example.lab4.entity.Pack;
import com.example.lab4.entity.Student;
import com.example.lab4.entity.StudentCourse;
import com.example.lab4.errors.CourseDuplicate;
import com.example.lab4.errors.CourseNotFound;
import com.example.lab4.errors.CourseOutOfLimit;
import com.example.lab4.errors.StudentNotFound;
import com.example.lab4.repository.CourseRepository;
import com.example.lab4.repository.CourseStudentRepository;
import com.example.lab4.repository.PackRepository;
import com.example.lab4.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StudentService {
    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseStudentRepository courseStudentRepository;

    @Autowired
    private
    PackRepository packRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public StudentDetailDto getDetails(String code) throws Exception {
        Student student = studentRepository.findByCode(code);
        if (student == null || student.getId() == null)
            throw new StudentNotFound(code);
        StudentDetailDto studentDetailDto = new StudentDetailDto();
        studentDetailDto.setStudentId(student.getId());
        studentDetailDto.setName(student.getName());
        studentDetailDto.setYear(student.getYear());
        studentDetailDto.setEmail(student.getEmail());
        studentDetailDto.setCode(student.getCode());
        return studentDetailDto;
    }

    public StudentDefaultDto register(@NonNull StudentRegisterDta studentRegisterDta) throws Exception {
        Student student = new Student();
        student.setName(studentRegisterDta.name());
        student.setCode(studentRegisterDta.code());
        student.setYear(studentRegisterDta.year());
        student.setEmail(studentRegisterDta.email());
        student.setCode(studentRegisterDta.code());
        student.setPassword(passwordEncoder.encode(studentRegisterDta.password()));
        StudentDefaultDto studentDefaultDto = new StudentDefaultDto();
        studentRepository.save(student);
        studentDefaultDto.setStudentId(student.getId());
        return studentDefaultDto;
    }

    public StudentDefaultDto changeDetailsAndUpdateEnrollCourses(@NonNull String code, @NonNull StudentChangeDetailsDta studentChangeDetailsDta) throws Exception {
        StudentDefaultDto studentDefaultDto = new StudentDefaultDto();
        Student student = studentRepository.findByCode(code);
        if (student == null || student.getId() == null)
            throw new StudentNotFound(code);
        if (studentChangeDetailsDta.year() != null )
            student.setYear(studentChangeDetailsDta.year());
        studentRepository.save(student);
        String[] courseCodes = studentChangeDetailsDta.courseCodes();
        Set<String> courses = courseRepository.findCoursesByStudent(student.getId()).stream().map(Course::getCode).collect(Collectors.toSet());

        List<Course> updatedCourses = new ArrayList<>();
        for (String courseCode : courseCodes) {
            if (!courses.contains(courseCode)) {

                Course course = courseRepository.findByCode(courseCode);
                if (course == null) {
                    throw new CourseNotFound(courseCode);
                } else if (course.getGroupCount() > courseStudentRepository.countStudentForCourse(course.getId()) + 1) {
                   updatedCourses.add(course);
                } else {
                    throw new CourseOutOfLimit(courseCode);
                }
            } else {
                throw new CourseDuplicate(courseCode);
            }
        }

        for (Course course : updatedCourses) {
            StudentCourse studentCourse = new StudentCourse();
            studentCourse.setCourse(course);
            studentCourse.setStudent(student);
            courseStudentRepository.save(studentCourse);
        }

        studentDefaultDto.setStudentId(student.getId());
        return studentDefaultDto;
    }

    public PackDetailDto[] getEnrolledCourses(String studentCode) throws Exception {
        Student student = studentRepository.findByCode(studentCode);
        if (student == null || student.getId() == null)
            throw new StudentNotFound(studentCode);

        List<Pack> packs = packRepository.findByStudentId(student.getId());
        List<PackDetailDto> packDetailDtos = new ArrayList<>();
        for (Pack pack : packs) {
            PackDetailDto packDetailDto = new PackDetailDto();
            packDetailDto.setPackId(pack.getId());
            packDetailDto.setName(pack.getName());
            ArrayList<CourseDefaultDto> courseDetailDtos = new ArrayList<>();
            for (Course course : courseStudentRepository.findByCourseByPackAndStudentId(pack.getId(), student.getId())) {
                CourseDefaultDto courseDetailDto = new CourseDefaultDto();
                courseDetailDto.setCourseId(course.getId());
                courseDetailDto.setCode(course.getCode());
                courseDetailDto.setName(course.getName());
                courseDetailDtos.add(courseDetailDto);
            }
            packDetailDto.setCourses(courseDetailDtos.toArray(new CourseDefaultDto[0]));
            packDetailDtos.add(packDetailDto);
        }

        PackDetailDto packDetailDto = new PackDetailDto();
        packDetailDto.setPackId(0L);
        packDetailDto.setName("compulsory");
        ArrayList<CourseDefaultDto> courseDetailDtos = new ArrayList<>();
        for (Course course : courseRepository.findByStudentIdAndNotPack(student.getId())) {
            CourseDefaultDto courseDetailDto = new CourseDefaultDto();
            courseDetailDto.setCourseId(course.getId());
            courseDetailDto.setCode(course.getCode());
            courseDetailDto.setName(course.getName());
            courseDetailDtos.add(courseDetailDto);
        }
        packDetailDto.setCourses(courseDetailDtos.toArray(new CourseDefaultDto[0]));
        packDetailDtos.add(packDetailDto);

        return packDetailDtos.toArray(new PackDetailDto[0]);
    }

    public StudentDefaultDto[] getAll() {
        List<Student> students = studentRepository.findAll();
        StudentDefaultDto[] studentDefaultDtos = new StudentDefaultDto[students.size()];
        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            StudentDetailDto studentDefaultDto = new StudentDetailDto();
            studentDefaultDto.setStudentId(student.getId());
            studentDefaultDto.setEmail(student.getEmail());
            studentDefaultDto.setYear(student.getYear());
            studentDefaultDto.setName(student.getName());
            studentDefaultDto.setCode(student.getCode());
            studentDefaultDtos[i] = studentDefaultDto;
        }
        return studentDefaultDtos;
    }

    public void deleteOne(String studentCode) throws StudentNotFound {
        Student student = studentRepository.findByCode(studentCode);
        if (student == null || student.getId() == null)
            throw new StudentNotFound(studentCode);
        courseStudentRepository.deleteByStudentId(student.getId());
        studentRepository.deleteById(student.getId());
    }
}
