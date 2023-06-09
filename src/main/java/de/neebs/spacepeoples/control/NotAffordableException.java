package de.neebs.spacepeoples.control;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class NotAffordableException extends RuntimeException {
    public NotAffordableException(String message) {
        super(message);
    }
}
