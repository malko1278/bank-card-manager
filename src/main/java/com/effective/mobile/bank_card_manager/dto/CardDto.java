package com.effective.mobile.bank_card_manager.dto;

import com.effective.mobile.bank_card_manager.entity.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CardDto {

    private Long id;
    private String cardNumberMasked; // Формат: "**** **** **** 1234"
    private String cardHolder;
    private LocalDate expiryDate;
    private CardStatus status;
    private BigDecimal balance;

    // Конструкторы
    public CardDto() {}

    public CardDto(Long id, String cardNumberMasked, String cardHolder, LocalDate expiryDate, CardStatus status, BigDecimal balance) {
        this.id = id;
        this.cardNumberMasked = cardNumberMasked;
        this.cardHolder = cardHolder;
        this.expiryDate = expiryDate;
        this.status = status;
        this.balance = balance;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCardNumberMasked() { return cardNumberMasked; }
    public void setCardNumberMasked(String cardNumberMasked) { this.cardNumberMasked = cardNumberMasked; }

    public String getCardHolder() { return cardHolder; }
    public void setCardHolder(String cardHolder) { this.cardHolder = cardHolder; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public CardStatus getStatus() { return status; }
    public void setStatus(CardStatus status) { this.status = status; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}