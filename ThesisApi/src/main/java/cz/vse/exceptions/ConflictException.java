package cz.vse.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by Martin Kravec on 13. 1. 2015.
 */

@ResponseStatus(value = HttpStatus.CONFLICT)  // 409
public class ConflictException extends RuntimeException {

    public ConflictException() {
    }

    public ConflictException(String message) {
        super(message);
    }
}
