package com.project.datalayer.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "services")
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    private MotelArea area;

    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @ColumnDefault("'Tháng'")
    @Column(name = "unit_name", length = 20)
    private String unitName;

    @ColumnDefault("true")
    @Column(name = "is_active")
    private Boolean isActive;

    @Lob
    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "service")
    private Set<ContractService> contractServices = new LinkedHashSet<>();

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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public Boolean isActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<ContractService> getContractServices() {
        return contractServices;
    }

    public void setContractServices(Set<ContractService> contractServices) {
        this.contractServices = contractServices;
    }

}