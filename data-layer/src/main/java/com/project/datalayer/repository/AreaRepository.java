package com.project.datalayer.repository;

import com.project.cruddata.entity.MotelArea;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AreaRepository extends JpaRepository<MotelArea, Long> {
}
