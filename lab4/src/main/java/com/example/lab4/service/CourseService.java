package com.example.lab4.service;

import com.example.lab4.dto.CourseDetailDto;
import com.example.lab4.entity.Course;
import com.example.lab4.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;


    public CourseDetailDto[] getList() {
        List<CourseDetailDto> resp = new ArrayList<>();
        List<Course> courses = courseRepository.findAll();
        for (Course course : courses) {
            CourseDetailDto courseDetailDto = new CourseDetailDto();
            courseDetailDto.setCode(course.getCode());
            courseDetailDto.setName(course.getName());
            courseDetailDto.setDescription(course.getDescription());
            courseDetailDto.setAbbr(course.getAbbr());
            courseDetailDto.setType(course.getType());
            courseDetailDto.setGroupCount(course.getGroupCount());
            courseDetailDto.setCourseId(course.getId());
            resp.add(courseDetailDto);
        }
        return resp.toArray(new CourseDetailDto[0]);
    }
}
