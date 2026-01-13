package org.example.digitalwallet.controller;

import org.example.digitalwallet.dto.TransferRequest;
import org.example.digitalwallet.dto.TransferResponse;
import org.example.digitalwallet.service.TransferService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transfer")
public class TransferController {


    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> saveTransfer(@RequestBody TransferRequest request) {
        TransferResponse response = transferService.saveTransfer(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
