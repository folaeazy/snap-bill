package com.expenseapp.app.entities.embenddables;

import com.domain.valueObjects.CurrencyCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class MoneyEmbeddable {

    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(length = 3)
    private CurrencyCode currency;
}
