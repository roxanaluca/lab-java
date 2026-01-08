package com.example.lab4.dto;

import com.example.lab4.entity.Course;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "course")
public class CourseDetailDto extends CourseDefaultDto {


    String type;
    String abbr;
    String description;
    int groupCount;



    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }



    public String getAbbr() {
        return abbr;
    }

    public void setAbbr(String abbr) {
        this.abbr = abbr;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getGroupCount() {
        return groupCount;
    }

    public void setGroupCount(int groupCount) {
        this.groupCount = groupCount;
    }
}
