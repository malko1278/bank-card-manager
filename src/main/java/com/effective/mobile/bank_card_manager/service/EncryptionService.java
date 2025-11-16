package com.effective.mobile.bank_card_manager.service;

public interface EncryptionService {
    /**
     * Шифрует строку данных. */
    String encrypt(String data);
    /**
     * Расшифровывает зашифрованную строку данных. */
    String decrypt(String encryptedData);
    /**
     * Маскирует номер карты для отображения (например: "**** **** **** 1234"). */
    String maskCardNumber(String fullCardNumber);
}