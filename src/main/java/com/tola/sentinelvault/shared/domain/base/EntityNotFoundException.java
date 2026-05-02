package com.tola.sentinelvault.shared.domain.base;

import com.tola.sentinelvault.shared.domain.exception.DomainException;

public class EntityNotFoundException extends DomainException {

    public EntityNotFoundException(String entityName, Object id) {
        super(entityName + "not found with id: " + id);
    }
}
