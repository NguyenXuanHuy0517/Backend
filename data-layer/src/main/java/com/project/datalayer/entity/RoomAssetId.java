package com.project.datalayer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RoomAssetId implements Serializable {
    private static final long serialVersionUID = 5255054894969682878L;
    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomAssetId entity = (RoomAssetId) o;
        return Objects.equals(this.roomId, entity.roomId) &&
                Objects.equals(this.equipmentId, entity.equipmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId, equipmentId);
    }
}