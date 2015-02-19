package cz.vse.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by Martin Kravec on 29. 12. 2014.
 */

@ResponseStatus(value = HttpStatus.NOT_FOUND)  // 404
public class NotFoundException extends RuntimeException {

    public NotFoundException() {
        super("Requested resource does not exist.");
    }

    public NotFoundException(String message) {
        super(message);
    }

}
