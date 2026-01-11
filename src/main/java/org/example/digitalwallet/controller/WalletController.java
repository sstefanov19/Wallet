package org.example.digitalwallet.controller;

import org.example.digitalwallet.dto.WalletRequest;
import org.example.digitalwallet.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createWallet(@RequestBody  WalletRequest request) {
        walletService.createWallet(request);

        return ResponseEntity.status(HttpStatus.CREATED).body("Created new wallet");
    }

}
