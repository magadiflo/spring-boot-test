package com.magadiflo.app.models.dto;

import java.math.BigDecimal;

public record TransactionDTO(Long bankId, Long accountIdOrigin, Long accountIdDestination, BigDecimal amount) {
}
