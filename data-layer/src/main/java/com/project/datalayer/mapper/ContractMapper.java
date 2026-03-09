package com.project.datalayer.mapper;

import com.project.cruddata.entity.Contract;
import com.project.cruddata.dto.ContractRequestDTO;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

/**
 * ContractMapper: Chuyển đổi dữ liệu hợp đồng.
 */
@Component
public class ContractMapper {

    public ContractRequestDTO toRequestDTO(Contract entity) {
        if (entity == null) return null;

        return ContractRequestDTO.builder()
                .contractCode(entity.getContractCode())
                .roomId(entity.getRoom() != null ? entity.getRoom().getId() : null)
                .tenantId(entity.getTenant() != null ? entity.getTenant().getId() : null)
                .depositId(entity.getDeposit() != null ? entity.getDeposit().getId() : null)
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .actualRentPrice(entity.getActualRentPrice())
                .serviceIds(entity.getContractServices() != null ?
                        entity.getContractServices().stream()
                                .map(cs -> cs.getService().getId())
                                .collect(Collectors.toList()) : null)
                .build();
    }
}