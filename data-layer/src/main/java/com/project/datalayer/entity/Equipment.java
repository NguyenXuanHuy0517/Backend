package com.project.datalayer.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "equipments")
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipment_id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "serial_number", length = 50)
    private String serialNumber;

    @ColumnDefault("'GOOD'")
    @Lob
    @Column(name = "status")
    private String status;

    @OneToMany(mappedBy = "equipment")
    private Set<RoomAsset> roomAssets = new LinkedHashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Set<RoomAsset> getRoomAssets() {
        return roomAssets;
    }

    public void setRoomAssets(Set<RoomAsset> roomAssets) {
        this.roomAssets = roomAssets;
    }

}