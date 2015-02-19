package cz.vse.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by Martin Kravec on 29. 12. 2014.
 */

@ResponseStatus(value = HttpStatus.BAD_REQUEST)  // 400
public class BadRequestException extends RuntimeException {

    public BadRequestException() {
        super("Invalid User Input");
    }

    public BadRequestException(String message) {
        super(message);
    }

}
