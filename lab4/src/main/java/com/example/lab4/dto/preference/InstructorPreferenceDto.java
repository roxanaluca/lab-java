package com.example.lab4.dto.preference;

import java.util.List;

public record InstructorPreferenceDto(List<InstructorPreferenceItemDto> list, String name, Long id) {

}
