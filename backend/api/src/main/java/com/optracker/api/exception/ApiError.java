package com.optracker.api.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * Representa a estrutura padrão de erro da API OP Tracker.
 */
public record ApiError(

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime timestamp,

        int status,

        String message,

        String path
) {
    // Os records em Java já geram construtores, getters, equals e hashCode automaticamente.
}

