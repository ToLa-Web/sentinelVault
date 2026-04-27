package com.tola.sentinelvault.identity.domain.model;

import com.tola.sentinelvault.shared.domain.exception.DomainException;

public class InvalidEmailException extends DomainException {

    public InvalidEmailException(String message) {
        super(message);
    }
}
