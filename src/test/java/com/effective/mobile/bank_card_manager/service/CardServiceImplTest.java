package com.effective.mobile.bank_card_manager.service;

import com.effective.mobile.bank_card_manager.entity.Card;
import com.effective.mobile.bank_card_manager.entity.CardStatus;
import com.effective.mobile.bank_card_manager.entity.User;
import com.effective.mobile.bank_card_manager.exception.InsufficientFundsException;
import com.effective.mobile.bank_card_manager.exception.UnauthorizedAccessException;
import com.effective.mobile.bank_card_manager.repository.CardRepository;
import com.effective.mobile.bank_card_manager.repository.UserRepository;
import com.effective.mobile.bank_card_manager.service.impl.CardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Модульный тест для {@link CardServiceImpl}.
 * Покрывает основные функции: перевод, блокировка, баланс, валидации.
 */
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private CardServiceImpl cardService;

    private User user;
    private Card sourceCard;
    private Card targetCard;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRole("USER");

        sourceCard = new Card();
        sourceCard.setId(100L);
        sourceCard.setUser(user);
        sourceCard.setCardNumberEncrypted("encrypted1234");
        sourceCard.setCardHolder("John Doe");
        sourceCard.setExpiryDate(LocalDate.now().plusYears(2));
        sourceCard.setStatus(CardStatus.ACTIVE);
        sourceCard.setBalance(new BigDecimal("1000.00"));

        targetCard = new Card();
        targetCard.setId(101L);
        targetCard.setUser(user);
        targetCard.setCardNumberEncrypted("encrypted5678");
        targetCard.setCardHolder("Jane Doe");
        targetCard.setExpiryDate(LocalDate.now().plusYears(1));
        targetCard.setStatus(CardStatus.ACTIVE);
        targetCard.setBalance(new BigDecimal("500.00"));
    }

    /**
     * ПЕРЕВОД МЕЖДУ КАРТАМИ
     */

    @Test
    void transferBetweenCards_Success_ShouldUpdateBalances() {
        // Arrange
        when(cardRepository.findById(100L)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(101L)).thenReturn(Optional.of(targetCard));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.save(any(Card.class))).thenReturn(sourceCard, targetCard); // 👈 MOCK AJOUTÉ

        // Act
        cardService.transferBetweenCards(100L, 101L, new BigDecimal("200.00"), 1L);

        // Assert
        assertEquals(new BigDecimal("800.00"), sourceCard.getBalance());
        assertEquals(new BigDecimal("700.00"), targetCard.getBalance());
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void transferBetweenCards_InsufficientFunds_ShouldThrowException() {
        // Arrange
        when(cardRepository.findById(100L)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(101L)).thenReturn(Optional.of(targetCard));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act & Assert
        InsufficientFundsException exception = assertThrows(
                InsufficientFundsException.class,
                () -> cardService.transferBetweenCards(100L, 101L, new BigDecimal("1500.00"), 1L)
        );

        assertEquals("Недостаточно средств на исходной карте", exception.getMessage());
    }

    @Test
    void transferBetweenCards_UnauthorizedTarget_ShouldThrowException() {
        // Arrange
        User anotherUser = new User();
        anotherUser.setId(2L);

        Card cardOfAnotherUser = new Card();
        cardOfAnotherUser.setId(200L);
        cardOfAnotherUser.setUser(anotherUser);
        cardOfAnotherUser.setBalance(new BigDecimal("1000.00"));
        cardOfAnotherUser.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(100L)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(200L)).thenReturn(Optional.of(cardOfAnotherUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act & Assert
        UnauthorizedAccessException exception = assertThrows(
                UnauthorizedAccessException.class,
                () -> cardService.transferBetweenCards(100L, 200L, new BigDecimal("100.00"), 1L)
        );

        assertEquals("Исходная карта должна быть активна для выполнения перевода.", exception.getMessage());
    }

    @Test
    void transferBetweenCards_InactiveSourceCard_ShouldThrowException() {
        // Arrange
        sourceCard.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(100L)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(101L)).thenReturn(Optional.of(targetCard));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> cardService.transferBetweenCards(100L, 101L, new BigDecimal("100.00"), 1L)
        );

        assertEquals("La carte source doit être active pour effectuer un virement.", exception.getMessage());
    }

    @Test
    void transferBetweenCards_NegativeAmount_ShouldThrowException() {
        // Arrange
        when(cardRepository.findById(100L)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(101L)).thenReturn(Optional.of(targetCard));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cardService.transferBetweenCards(100L, 101L, new BigDecimal("-50.00"), 1L)
        );

        assertEquals("Сумма перевода должна быть больше нуля.", exception.getMessage());
    }

    /**
     * БЛОКИРОВКА КАРТЫ
     */

    @Test
    void requestBlockCard_Success_ShouldSetStatusToBlocked() {
        // Arrange
        when(cardRepository.findById(100L)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.save(any(Card.class))).thenReturn(sourceCard); // 👈 MOCK AJOUTÉ

        // Act
        Card blockedCard = cardService.requestBlockCard(100L, 1L);

        // Assert
        assertEquals(CardStatus.BLOCKED, blockedCard.getStatus());
        verify(cardRepository, times(1)).save(blockedCard);
    }

    @Test
    void requestBlockCard_Unauthorized_ShouldThrowException() {
        // Arrange
        User anotherUser = new User();
        anotherUser.setId(2L);

        Card cardOfAnotherUser = new Card();
        cardOfAnotherUser.setId(200L);
        cardOfAnotherUser.setUser(anotherUser);

        when(cardRepository.findById(200L)).thenReturn(Optional.of(cardOfAnotherUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act & Assert
        UnauthorizedAccessException exception = assertThrows(
                UnauthorizedAccessException.class,
                () -> cardService.requestBlockCard(200L, 1L)
        );
        assertEquals("Вы не авторизованы для блокировки этой карты.", exception.getMessage());
    }

    // ================
    // CONSULTATION DE SOLDE
    // ================

    @Test
    void getCardBalance_Success_ShouldReturnBalance() {
        // Arrange
        when(cardRepository.findById(100L)).thenReturn(Optional.of(sourceCard));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        // Act
        BigDecimal balance = cardService.getCardBalance(100L, 1L);
        // Assert
        assertEquals(new BigDecimal("1000.00"), balance);
    }

    @Test
    void getCardBalance_Unauthorized_ShouldThrowException() {
        // Arrange
        User anotherUser = new User();
        anotherUser.setId(2L);

        Card cardOfAnotherUser = new Card();
        cardOfAnotherUser.setId(200L);
        cardOfAnotherUser.setUser(anotherUser);

        when(cardRepository.findById(200L)).thenReturn(Optional.of(cardOfAnotherUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act & Assert
        UnauthorizedAccessException exception = assertThrows(
                UnauthorizedAccessException.class,
                () -> cardService.getCardBalance(200L, 1L)
        );

        assertEquals("Вы не авторизованы для просмотра баланса этой карты.", exception.getMessage());
    }

    @Test
    void transferBetweenCards_ZeroAmount_ShouldThrowException() {
        // Arrange
        when(cardRepository.findById(100L)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(101L)).thenReturn(Optional.of(targetCard));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cardService.transferBetweenCards(100L, 101L, BigDecimal.ZERO, 1L)
        );

        assertEquals("Сумма перевода должна быть больше нуля.", exception.getMessage());
    }
}
