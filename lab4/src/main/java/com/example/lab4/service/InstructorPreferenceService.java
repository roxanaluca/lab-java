package com.example.lab4.service;

import com.example.lab4.dta.InstructorPreferenceRegisterDta;
import com.example.lab4.dto.preference.InstructorPreferenceDto;
import com.example.lab4.dto.preference.InstructorPreferenceItemDto;
import com.example.lab4.entity.InstructorPreference;
import com.example.lab4.entity.InstructorPreferenceItem;
import com.example.lab4.errors.CourseNotFound;
import com.example.lab4.errors.PreferenceNotExist;
import com.example.lab4.repository.CourseRepository;
import com.example.lab4.repository.InstructorPreferenceItemRepository;
import com.example.lab4.repository.InstructorPreferenceRepository;
import com.example.lab4.repository.PackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class InstructorPreferenceService {
    @Autowired
    private InstructorPreferenceRepository preferenceRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private InstructorPreferenceItemRepository preferenceItemRepository;
    @Autowired
    private PackRepository packRepository;

    public List<InstructorPreferenceDto> getAll() {
        List<InstructorPreference> preferenceItems = preferenceRepository.findAll();

        List<InstructorPreferenceDto> instructorPreferences = new ArrayList<>();
        for (InstructorPreference preferenceItem : preferenceItems) {
            List<InstructorPreferenceItemDto> list = preferenceItem.getInstructorPreferenceItems().stream().map(pt -> new InstructorPreferenceItemDto(pt.getCourse().getCode(), pt.getWeightPercent())).toList();

            instructorPreferences.add(new InstructorPreferenceDto(list, preferenceItem.getCourseOptionalName(), preferenceItem.getId()));
        }
        return instructorPreferences;
    }

    public List<InstructorPreferenceDto> getByPack(Long id) {
        List<InstructorPreference> preferenceItems = preferenceRepository.findAllByPackId(id);
        List<InstructorPreferenceDto> instructorPreferences = new ArrayList<>();

        for (InstructorPreference preferenceItem : preferenceItems) {
            List<InstructorPreferenceItemDto> list = preferenceItem.getInstructorPreferenceItems().stream().map(pt -> (pt.getCourse().getPack().getId().compareTo(id) == 0) ? new InstructorPreferenceItemDto(pt.getCourse().getCode(), pt.getWeightPercent()) : null).filter(Objects::nonNull).toList();
            instructorPreferences.add(new InstructorPreferenceDto(list, preferenceItem.getCourseOptionalName(), preferenceItem.getId()));
        }
        return instructorPreferences;
    }

    public void savePreference(InstructorPreferenceRegisterDta[] instructorRequestPreference) throws CourseNotFound {
        for (InstructorPreferenceRegisterDta instructorRequest : instructorRequestPreference) {
            changePreference(instructorRequest);
        }
    }

    public void changePreference(InstructorPreferenceRegisterDta instructorRequest) throws CourseNotFound {
        InstructorPreference instructorPreference = preferenceRepository.findByCourseCode(instructorRequest.name());
        if (instructorPreference != null) {
            preferenceRepository.delete(instructorPreference);
        }

        instructorPreference = new InstructorPreference();
        if (!courseRepository.existsByCode(instructorRequest.name()) || packRepository.findByCourseCode(instructorRequest.name()) == null) {
            throw new CourseNotFound(instructorRequest.name());
        }
        instructorPreference.setCourseOptionalName(instructorRequest.name());
        preferenceRepository.save(instructorPreference);
        for (Map.Entry<String, Long> preference : instructorRequest.instructorPreference().entrySet()) {
            if (!courseRepository.existsByCode(preference.getKey()) || packRepository.findByCourseCode(preference.getKey()) != null) {
                preferenceRepository.delete(instructorPreference);
                throw new CourseNotFound(preference.getKey());
            }
            InstructorPreferenceItem instructorPreferenceItem = new InstructorPreferenceItem();
            instructorPreferenceItem.setInstructorPreference(instructorPreference);
            instructorPreferenceItem.setWeightPercent(preference.getValue());
            instructorPreferenceItem.setCourse(courseRepository.findByCode(preference.getKey()));
            preferenceItemRepository.save(instructorPreferenceItem);
        }
    }

    public void changeWeight(String code, Long value) {
        InstructorPreferenceItem item = preferenceItemRepository.findAllByCourseCode(code);
        if (item == null) {
            throw new PreferenceNotExist(value.toString());
        }
        item.setWeightPercent(value);

        preferenceItemRepository.save(item);
    }

    public void deletePreferenceItem(String code) {
        InstructorPreferenceItem item = preferenceItemRepository.findAllByCourseCode(code);

        if (item == null) {
            throw new PreferenceNotExist("0L");
        }
        preferenceItemRepository.delete(item);
    }


    public void deletePreference(Long id) {
        Optional<InstructorPreference> item = preferenceRepository.findById(id);
        if (item.isEmpty()) {
            throw new PreferenceNotExist("0L");
        }
        preferenceRepository.delete(item.get());
    }
}
