package com.effective.mobile.bank_card_manager.service;

import com.effective.mobile.bank_card_manager.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface CardService {
    /*** Методы для АДМИНИСТРАТОРА ***/
    Card createCard(Card card);
    Card blockCard(Long cardId);
    Card activateCard(Long cardId);
    void deleteCard(Long cardId);
    Page<Card> getAllCards(Pageable pageable);

    /*** Методы для ПОЛЬЗОВАТЕЛЯ ***/
    Page<Card> getUserCards(Long userId, Pageable pageable);
    Card requestBlockCard(Long cardId, Long userId);
    void transferBetweenCards(Long fromCardId, Long toCardId, BigDecimal amount, Long userId);
    BigDecimal getCardBalance(Long cardId, Long userId);
}