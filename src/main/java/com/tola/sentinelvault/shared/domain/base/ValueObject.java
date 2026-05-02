package com.tola.sentinelvault.shared.domain.base;

/**
 * Marker interface for all Value Objects in the domain.
 *
 * Value Objects are:
 *  - Immutable
 *  - Compared by value (not identity)
 *  - Side-effect free
 *
 * Implementations must override equals() and hashCode()
 * based purely on their fields.
 */
public interface ValueObject {
}
