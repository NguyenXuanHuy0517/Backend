package com.project.datalayer.repository;

import com.project.datalayer.entity.MotelArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AreaRepository extends JpaRepository<MotelArea, Long> {
    List<MotelArea> findByHostId(Long hostId);
}
