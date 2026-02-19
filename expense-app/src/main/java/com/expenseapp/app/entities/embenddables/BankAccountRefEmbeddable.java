package com.expenseapp.app.entities.embenddables;

import com.expensecore.enums.CurrencyCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class BankAccountRefEmbeddable {

    @Column(name = "account_id")
    private String accountId;

    @Column
    private String label;

    @Column(length = 4)
    private String last4;

    @Enumerated(EnumType.STRING)
    @Column(length = 3)
    private CurrencyCode currency;
}
