package com.effective.mobile.bank_card_manager.service.impl;

import com.effective.mobile.bank_card_manager.dto.CardDto;
import com.effective.mobile.bank_card_manager.entity.Card;
import com.effective.mobile.bank_card_manager.service.CardMapper;
import com.effective.mobile.bank_card_manager.service.EncryptionService;
import org.springframework.stereotype.Service;

@Service
public class CardMapperImpl implements CardMapper {
    private EncryptionService encryptionService;

    @Override
    public CardDto toDto(Card card) {
        // Расшифровать номер карты
        String fullCardNumber = encryptionService.decrypt(card.getCardNumberEncrypted());
        // Замаскировать номер для отображения
        String maskedCardNumber = encryptionService.maskCardNumber(fullCardNumber);

        return new CardDto(
                card.getId(),
                maskedCardNumber,
                card.getCardHolder(),
                card.getExpiryDate(),
                card.getStatus(),
                card.getBalance()
        );
    }
}