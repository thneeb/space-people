package de.neebs.spacepeoples.control;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class ShipPartMissingException extends RuntimeException {
    public ShipPartMissingException(String message) {
        super(message);
    }
}
