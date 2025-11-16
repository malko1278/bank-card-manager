package com.effective.mobile.bank_card_manager.controller;

import com.effective.mobile.bank_card_manager.dto.CardDto;
import com.effective.mobile.bank_card_manager.entity.Card;
import com.effective.mobile.bank_card_manager.service.CardMapper;
import com.effective.mobile.bank_card_manager.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final CardService cardService;
    private final CardMapper cardMapper;

    // Constructor Injection
    public AdminController(CardService cardService, CardMapper cardMapper) {
        this.cardService = cardService;
        this.cardMapper = cardMapper;
    }

    @PostMapping("/cards")
    public ResponseEntity<CardDto> createCard(@RequestBody Card card) {
        Card createdCard = cardService.createCard(card);
        CardDto cardDto = cardMapper.toDto(createdCard);
        return ResponseEntity.ok(cardDto);
    }

    @PutMapping("/cards/{cardId}/block")
    public ResponseEntity<Card> blockCard(@PathVariable Long cardId) {
        Card blockedCard = cardService.blockCard(cardId);
        return ResponseEntity.ok(blockedCard);
    }

    @PutMapping("/cards/{cardId}/activate")
    public ResponseEntity<Card> activateCard(@PathVariable Long cardId) {
        Card activatedCard = cardService.activateCard(cardId);
        return ResponseEntity.ok(activatedCard);
    }

    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cards")
    public ResponseEntity<Page<CardDto>> getAllCards(Pageable pageable) {
        Page<Card> cards = cardService.getAllCards(pageable);
        Page<CardDto> cardDtos = cards.map(cardMapper::toDto);
        return ResponseEntity.ok(cardDtos);
    }
}