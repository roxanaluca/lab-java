package com.example.lab4.repository;

import com.example.lab4.entity.ScheduleSaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ScheduleSagaRepository extends JpaRepository<ScheduleSaga, String> {

}
