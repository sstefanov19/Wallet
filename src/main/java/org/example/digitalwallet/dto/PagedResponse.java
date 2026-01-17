package org.example.digitalwallet.dto;

import java.util.List;

public record PagedResponse<T>(
        List<T> data,
        Long nextCursor
) {
}
