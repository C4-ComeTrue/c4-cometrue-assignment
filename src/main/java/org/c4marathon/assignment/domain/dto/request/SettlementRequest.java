package org.c4marathon.assignment.domain.dto.request;

import java.util.List;

import org.c4marathon.assignment.domain.type.SettlementType;

public record SettlementRequest(List<Long> userIds, long money, SettlementType settlementType) {
}
