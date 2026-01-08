package com.example.lab4.service;

import com.example.lab4.dto.CourseDefaultDto;
import com.example.lab4.dto.CourseDetailDto;
import com.example.lab4.dto.PackDefaultDto;
import com.example.lab4.dto.PackDetailDto;
import com.example.lab4.entity.Course;
import com.example.lab4.entity.Pack;
import com.example.lab4.errors.PackNotFound;
import com.example.lab4.repository.CourseRepository;
import com.example.lab4.repository.PackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PackService {
    @Autowired
    private PackRepository packRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Cacheable("packs")
    public List<Pack> getPacks() {
        return packRepository.findAll();
    }

    public Long getTimeStamp(Long packId) throws PackNotFound {
        Optional<Pack> pack = packRepository.findById(packId);
        if (pack.isEmpty())
            throw new PackNotFound(packId);
        if (pack.get().getLastUpdated() == null || pack.get().getLastUpdated().isAfter(Instant.now())) {
            Instant timePlus5m =  Instant.now().plus(5, ChronoUnit.MINUTES);
            packRepository.updateLastUpdatedById(packId,timePlus5m);
            return timePlus5m.toEpochMilli();
        }
        return pack.get().getLastUpdated().toEpochMilli();
    }

    public PackDefaultDto getPackById(Long id) {
        Pack pack = packRepository.findById(id).get();
        PackDetailDto packDefaultDto = new PackDetailDto();
        packDefaultDto.setPackId(pack.getId());
        packDefaultDto.setName(pack.getName());
        List<CourseDefaultDto> coursesDefaultDto = new ArrayList<>();
        for(Course course : pack.getCourses()) {
            CourseDetailDto courseDetailDto = new CourseDetailDto();
            courseDetailDto.setCourseId(course.getId());
            courseDetailDto.setName(course.getName());
            courseDetailDto.setAbbr(course.getAbbr());
            courseDetailDto.setCode(course.getCode());
            courseDetailDto.setDescription(course.getDescription());
            courseDetailDto.setCode(course.getCode());
            coursesDefaultDto.add(courseDetailDto);
        }
        packDefaultDto.setCourses(coursesDefaultDto.toArray(new CourseDefaultDto[0]));
        return packDefaultDto;
    }

    public PackDefaultDto[] getAllPacks() {
        List<PackDefaultDto> packDefaultDtos = new ArrayList<>();
        for(Pack pack : packRepository.findAll()) {
            PackDetailDto packDetailDto = new PackDetailDto();
            packDetailDto.setPackId(pack.getId());
            packDetailDto.setName(pack.getName());
            packDefaultDtos.add(packDetailDto);
        }

        return packDefaultDtos.toArray(new PackDefaultDto[0]);
    }
}
