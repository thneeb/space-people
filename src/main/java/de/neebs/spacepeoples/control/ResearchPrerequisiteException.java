package de.neebs.spacepeoples.control;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
public class ResearchPrerequisiteException extends RuntimeException {
    public ResearchPrerequisiteException(String message) {
        super(message);
    }
}
