package org.example.digitalwallet.controller;

import jakarta.validation.Valid;
import org.example.digitalwallet.dto.PagedResponse;
import org.example.digitalwallet.dto.TransferRequest;
import org.example.digitalwallet.dto.TransferResponse;
import org.example.digitalwallet.service.TransferService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transfer")
public class TransferController {


    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> saveTransfer(@Valid @RequestBody TransferRequest request) {
        TransferResponse response = transferService.saveTransfer(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<TransferResponse>> getTransfers(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int limit) {

        List<TransferResponse> transfers = transferService.getTransferHistory(cursor, limit);

        Long nextCursor = transfers.isEmpty() ? null :
                transfers.getLast().id();

        return ResponseEntity.ok(new PagedResponse<>(transfers, nextCursor));
    }

}
