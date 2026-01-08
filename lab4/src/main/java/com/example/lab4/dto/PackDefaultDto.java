package com.example.lab4.dto;

import org.springframework.hateoas.RepresentationModel;

public class PackDefaultDto extends RepresentationModel<PackDefaultDto> {
    Long packId;

    String name;

    public Long getPackId() {
        return packId;
    }

    public void setPackId(Long packId) {
        this.packId = packId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}


