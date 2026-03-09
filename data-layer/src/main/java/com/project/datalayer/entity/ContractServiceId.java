package com.project.datalayer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ContractServiceId implements Serializable {
    private static final long serialVersionUID = -2939161589141233397L;
    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContractServiceId entity = (ContractServiceId) o;
        return Objects.equals(this.contractId, entity.contractId) &&
                Objects.equals(this.serviceId, entity.serviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contractId, serviceId);
    }
}