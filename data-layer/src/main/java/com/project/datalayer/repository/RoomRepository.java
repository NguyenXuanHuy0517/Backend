package com.project.datalayer.repository;

import com.project.datalayer.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository <Room, Long> {
    List<Room> findByRoomId(Long areaId);
}
