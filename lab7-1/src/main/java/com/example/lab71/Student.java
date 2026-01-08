package com.example.lab71;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="students")
public class Student {
    @Id
    Long id;
    @Column(name = "code", unique = true, nullable = false)
    String code;
    @Column(name = "year")
    int year;
    @Column(nullable = false)
    String name;

    public String getCode() {
        return code;
    }

    public Long getId() {
        return id;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }


}

