package com.btxtech.server.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * An entity was asked for by id and there is none.
 * <p>
 * It exists to separate "you asked for something that is not there" from "something here is
 * broken". Until now both said the same thing: {@code getEntity} ended in a bare
 * {@code Optional.orElseThrow()}, that throws {@link java.util.NoSuchElementException}, Spring has
 * no mapping for it, and the answer was a 500 with a full stack trace. Seven days of production
 * logs held thirty-three of these, every identifiable one a planet id the editor asked for and
 * did not have - and each cost the same log volume and the same second of attention as a genuine
 * fault would.
 * <p>
 * Deliberately not a handler for {@link java.util.NoSuchElementException} in
 * {@code GlobalExceptionHandler}, which would have been three lines. There are thirty-four bare
 * {@code orElseThrow()} calls in this server. Some are lookups by id, where 404 is the truthful
 * answer; the rest assert an internal invariant, where 500 is. Mapping the type would have turned
 * every one of the second kind into a tidy 404 and hidden exactly the defects worth finding. The
 * cut belongs at the meaning, not at the exception class.
 * <p>
 * The message names the entity and the id, because a 404 that does not say what was missing sends
 * the reader back to the code to find out.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoSuchEntityException extends RuntimeException {
    public NoSuchEntityException(Class<?> entityClass, Object id) {
        super("No " + entityClass.getSimpleName() + " with id " + id);
    }
}
