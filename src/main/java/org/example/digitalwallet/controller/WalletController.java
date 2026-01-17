package org.example.digitalwallet.controller;

import lombok.Getter;
import org.example.digitalwallet.dto.DepositRequest;
import org.example.digitalwallet.dto.WalletRequest;
import org.example.digitalwallet.dto.WalletResponse;
import org.example.digitalwallet.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/deposit")
    public ResponseEntity<String> depositToWallet(@RequestBody DepositRequest request) {
        walletService.depositToWallet(request);

        return ResponseEntity.status(HttpStatus.OK).body("Deposit of " + request.getDepositAmount() +  " was successful ");
    }

    @GetMapping("/{id}")
    public ResponseEntity<WalletResponse> getWalletById(@PathVariable Long id) {
        WalletResponse response = walletService.getWalletById(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
