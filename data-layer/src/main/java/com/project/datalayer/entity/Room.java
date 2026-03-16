package com.project.datalayer.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    private MotelArea area;

    @Column(name = "room_code", nullable = false, length = 50)
    private String roomCode;

    @Column(name = "base_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal basePrice;

    @ColumnDefault("3500.00")
    @Column(name = "elec_price", precision = 15, scale = 2)
    private BigDecimal elecPrice;

    @ColumnDefault("15000.00")
    @Column(name = "water_price", precision = 15, scale = 2)
    private BigDecimal waterPrice;

    @ColumnDefault("'AVAILABLE'")
    @Lob
    @Column(name = "status")
    private String status;

    @Column(name = "area_size")
    private Float areaSize;

    @Lob
    @Column(name = "images")
    private String images;

    @Lob
    @Column(name = "amenities")
    private String amenities;

    /**
     * Lịch sử thay đổi trạng thái phòng — THÊM MỚI.
     * Được populate tự động khi RoomBusinessService gọi changeRoomStatus().
     */
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("changedAt DESC")
    private Set<RoomStatusHistory> statusHistory = new LinkedHashSet<>();

    @OneToMany(mappedBy = "room")
    private Set<Contract> contracts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "room")
    private Set<Deposit> deposits = new LinkedHashSet<>();

    @OneToMany(mappedBy = "room")
    private Set<Issue> issues = new LinkedHashSet<>();

    @OneToMany(mappedBy = "room")
    private Set<RoomAsset> roomAssets = new LinkedHashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MotelArea getArea() {
        return area;
    }

    public void setArea(MotelArea area) {
        this.area = area;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getElecPrice() {
        return elecPrice;
    }

    public void setElecPrice(BigDecimal elecPrice) {
        this.elecPrice = elecPrice;
    }

    public BigDecimal getWaterPrice() {
        return waterPrice;
    }

    public void setWaterPrice(BigDecimal waterPrice) {
        this.waterPrice = waterPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Float getAreaSize() {
        return areaSize;
    }

    public void setAreaSize(Float areaSize) {
        this.areaSize = areaSize;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getAmenities() {
        return amenities;
    }

    public void setAmenities(String amenities) {
        this.amenities = amenities;
    }

    public Set<Contract> getContracts() {
        return contracts;
    }

    public void setContracts(Set<Contract> contracts) {
        this.contracts = contracts;
    }

    public Set<Deposit> getDeposits() {
        return deposits;
    }

    public void setDeposits(Set<Deposit> deposits) {
        this.deposits = deposits;
    }

    public Set<Issue> getIssues() {
        return issues;
    }

    public void setIssues(Set<Issue> issues) {
        this.issues = issues;
    }

    public Set<RoomAsset> getRoomAssets() {
        return roomAssets;
    }

    public void setRoomAssets(Set<RoomAsset> roomAssets) {
        this.roomAssets = roomAssets;
    }

}