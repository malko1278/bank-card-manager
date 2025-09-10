package com.effective.mobile.bank_card_manager.controller;

import com.effective.mobile.bank_card_manager.service.EncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestEncryptionController {

    @Autowired
    private EncryptionService encryptionService;

    @GetMapping("/encrypt")
    public String encrypt(@RequestParam String data) {
        return encryptionService.encrypt(data);
    }

    @GetMapping("/decrypt")
    public String decrypt(@RequestParam String encrypted) {
        return encryptionService.decrypt(encrypted);
    }

    @GetMapping("/mask")
    public String mask(@RequestParam String cardNumber) {
        return encryptionService.maskCardNumber(cardNumber);
    }
}