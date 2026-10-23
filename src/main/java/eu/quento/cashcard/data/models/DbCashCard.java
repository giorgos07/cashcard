package eu.quento.cashcard.data.models;

import org.springframework.data.annotation.Id;

public record DbCashCard(@Id Long id, Double amount, String owner) {
}
