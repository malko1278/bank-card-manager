package com.effective.mobile.bank_card_manager.controller;

import com.effective.mobile.bank_card_manager.entity.Card;
import com.effective.mobile.bank_card_manager.service.CardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER')")
public class UserController {
    private final CardService cardService;

    public UserController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/cards")
    public ResponseEntity<Page<Card>> getUserCards(Authentication authentication, Pageable pageable) {
        // Предполагаем, что имя пользователя это ID, или можно использовать UserDetailsImpl
        Long userId = Long.valueOf(authentication.getName());
        Page<Card> cards = cardService.getUserCards(userId, pageable);
        return ResponseEntity.ok(cards);
    }

    @PutMapping("/cards/{cardId}/request-block")
    public ResponseEntity<Card> requestBlockCard(@PathVariable Long cardId, Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        Card blockedCard = cardService.requestBlockCard(cardId, userId);
        return ResponseEntity.ok(blockedCard);
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transferBetweenCards(
            @RequestParam Long fromCardId,
            @RequestParam Long toCardId,
            @RequestParam BigDecimal amount,
            Authentication authentication) {

        Long userId = Long.valueOf(authentication.getName());
        cardService.transferBetweenCards(fromCardId, toCardId, amount, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/cards/{cardId}/balance")
    public ResponseEntity<BigDecimal> getCardBalance(@PathVariable Long cardId, Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        BigDecimal balance = cardService.getCardBalance(cardId, userId);
        return ResponseEntity.ok(balance);
    }
}