package com.project.datalayer.repository;

import com.project.datalayer.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository <Room, Long> {
}
