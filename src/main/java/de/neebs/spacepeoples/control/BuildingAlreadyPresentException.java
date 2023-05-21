package de.neebs.spacepeoples.control;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class BuildingAlreadyPresentException extends RuntimeException {
}
