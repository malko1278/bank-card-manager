package com.effective.mobile.bank_card_manager.service;

import com.effective.mobile.bank_card_manager.dto.CardDto;
import com.effective.mobile.bank_card_manager.entity.Card;

public interface CardMapper {
    CardDto toDto(Card card);
}