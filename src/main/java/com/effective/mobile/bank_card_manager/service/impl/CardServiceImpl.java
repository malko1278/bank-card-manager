package com.effective.mobile.bank_card_manager.service.impl;

import com.effective.mobile.bank_card_manager.entity.Card;
import com.effective.mobile.bank_card_manager.entity.CardStatus;
import com.effective.mobile.bank_card_manager.exception.CardNotFoundException;
import com.effective.mobile.bank_card_manager.exception.InsufficientFundsException;
import com.effective.mobile.bank_card_manager.exception.UnauthorizedAccessException;
import com.effective.mobile.bank_card_manager.repository.CardRepository;
import com.effective.mobile.bank_card_manager.service.CardService;
import com.effective.mobile.bank_card_manager.service.EncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class CardServiceImpl implements CardService {
    @Autowired
    private final CardRepository cardRepository;
    @Autowired
    private final EncryptionService encryptionService;

    // Constructor Injection
    public CardServiceImpl(CardRepository cardRepository, EncryptionService encryptionService) {
        this.cardRepository = cardRepository;
        this.encryptionService = encryptionService;
    }

    @Override
    public Card createCard(Card card) {
        // Зашифровать номер карты перед сохранением
        card.setCardNumberEncrypted(encryptionService.encrypt(card.getCardNumberEncrypted()));
        return cardRepository.save(card);
    }

    @Override
    public Card blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена с ID: " + cardId));
        card.setStatus(CardStatus.BLOCKED);
        return cardRepository.save(card);
    }

    @Override
    public Card activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена с ID: " + cardId));

        if (card.getExpiryDate().isBefore(LocalDate.now())) {
            card.setStatus(CardStatus.EXPIRED);
        } else {
            card.setStatus(CardStatus.ACTIVE);
        }
        return cardRepository.save(card);
    }

    @Override
    public void deleteCard(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new CardNotFoundException("Карта не найдена с ID: " + cardId);
        }
        cardRepository.deleteById(cardId);
    }

    @Override
    public Page<Card> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable);
    }

    @Override
    public Page<Card> getUserCards(Long userId, Pageable pageable) {
        return cardRepository.findByUserId(userId, pageable);
    }

    @Override
    public Card requestBlockCard(Long cardId, Long userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена с ID: " + cardId));
        if (!card.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Вы не авторизованы для блокировки этой карты.");
        }
        card.setStatus(CardStatus.BLOCKED);
        return cardRepository.save(card);
    }

    @Transactional
    @Override
    public void transferBetweenCards(Long fromCardId, Long toCardId, BigDecimal amount, Long userId) {
        // Проверить, что обе карты существуют
        Card fromCard = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new CardNotFoundException("Исходная карта не найдена с ID: " + fromCardId));
        Card toCard = cardRepository.findById(toCardId)
                .orElseThrow(() -> new CardNotFoundException("Карта назначения не найдена с ID: " + toCardId));

        // Валидационные проверки
        if (!fromCard.getUser().getId().equals(userId) || !toCard.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Перевод возможен только между вашими собственными картами.");
        }
        if (!CardStatus.ACTIVE.equals(fromCard.getStatus()) || !CardStatus.ACTIVE.equals(toCard.getStatus())) {
            throw new IllegalStateException("Обе карты должны быть активны для выполнения перевода.");
        }
        // Проверить, что сумма положительна
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма перевода должна быть больше нуля.");
        }
        // Проверить, что баланс исходной карты достаточен
        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Недостаточно средств на исходной карте.");
        }
        // Выполнить перевод
        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));
        // Сохранить изменения
        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }

    @Override
    public BigDecimal getCardBalance(Long cardId, Long userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена с ID: " + cardId));

        // Проверить, что карта принадлежит пользователю
        if (!card.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Вы не авторизованы для просмотра баланса этой карты.");
        }
        return card.getBalance();
    }
}