package com.example.test.repository;

import com.example.test.entity.Actual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActualRepository extends JpaRepository<Actual, Long> {

}