package edu.ucalgary.oop;

import java.time.LocalDate;

/**
 * Represents a supply item held at a {@link Location} or allocated to a
 * {@link DisasterVictim}.
 * <p>
 * A supply is considered perishable if and only if it carries an expiry
 * date. This determination is made at construction time for database-loaded
 * supplies (where the expiry date is read directly from the record) and
 * at the point the user explicitly marks a new supply as perishable via
 * {@link #setPerishable(boolean)}. Expired supplies must not be allocated
 * to victims.
 * </p>
 *
 * @author Eric Hallett (30117108)
 * @version 1.0
 * @since 2026-03-26
 */
public class Supply {

    /** Database primary key; 0 until persisted. */
    private int dbId;

    /** The type of this supply (e.g., {@code "blanket"}, {@code "water"}). */
    private String type;

    /** The quantity of this supply item. */
    private int quantity;

    /** Whether this supply can expire. */
    private boolean perishable;

    /**
     * The expiry date for perishable supplies;
     * {@code null} for non-perishable.
     */
    private LocalDate expiryDate;

    /**
     * The date this supply was allocated to a victim;
     * {@code null} if unallocated.
     */
    private LocalDate allocationDate;

    /** An optional description or note about this supply item. */
    private String description;

    /**
     * Constructs a new {@code Supply} for user-driven creation.
     * The supply is non-perishable by default; call
     * {@link #setPerishable(boolean)} to mark it as perishable before
     * setting an expiry date.
     *
     * @param type     the supply type string; must not be {@code null}
     * @param quantity the initial quantity; must not be negative
     * @throws IllegalArgumentException if {@code quantity} is negative
     */
    public Supply(String type, int quantity) {
        this.type = type;
        setQuantity(quantity);
        this.perishable = false;
    }

    /**
     * Constructs a {@code Supply} from an existing database record.
     * Perishability is determined by whether {@code expiryDate} is
     * non-{@code null}: a supply that has an expiry date in the database
     * is perishable; one without is not.
     *
     * @param dbId           the database primary key
     * @param type           the supply type string
     * @param expiryDate     the expiry date, or {@code null} if non-perishable
     * @param allocationDate the allocation date, or {@code null} if unallocated
     * @param description    an optional description of this supply
     */
    public Supply(int dbId, String type, LocalDate expiryDate,
                  LocalDate allocationDate, String description) {
        this.dbId = dbId;
        this.type = type;
        this.quantity = 1;
        this.perishable = (expiryDate != null);
        this.expiryDate = expiryDate;
        this.allocationDate = allocationDate;
        this.description = description;
    }

    /**
     * Returns whether this supply has passed its expiry date.
     * Non-perishable supplies are never considered expired.
     *
     * @return {@code true} if this supply is perishable, has an expiry date,
     *         and that date is strictly before today; {@code false} otherwise
     */
    public boolean isExpired() {
        if (!perishable || expiryDate == null) {
            return false;
        }
        return expiryDate.isBefore(LocalDate.now());
    }

    /**
     * Returns the database ID of this supply.
     *
     * @return the database primary key, or {@code 0} if not yet persisted
     */
    public int getDbId() {
        return dbId;
    }

    /**
     * Sets the database ID of this supply.
     *
     * @param dbId the database primary key assigned after persistence
     */
    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    /**
     * Returns the type of this supply.
     *
     * @return the supply type string
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of this supply.
     *
     * @param type the new supply type string
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the quantity of this supply.
     *
     * @return the quantity as a non-negative integer
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity of this supply.
     *
     * @param quantity the new quantity; must not be negative
     * @throws IllegalArgumentException if {@code quantity} is negative
     */
    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException(
                "Quantity cannot be negative");
        }
        this.quantity = quantity;
    }

    /**
     * Returns whether this supply is perishable.
     *
     * @return {@code true} if this supply can expire; {@code false} otherwise
     */
    public boolean isPerishable() {
        return perishable;
    }

    /**
     * Sets whether this supply is perishable. When changed from perishable
     * to non-perishable, the expiry date is cleared automatically.
     *
     * @param perishable {@code true} to mark this supply as perishable;
     *                   {@code false} otherwise
     */
    public void setPerishable(boolean perishable) {
        this.perishable = perishable;
        if (!perishable) {
            this.expiryDate = null;
        }
    }

    /**
     * Returns the expiry date of this supply.
     *
     * @return the expiry date, or {@code null} if not set or non-perishable
     */
    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    /**
     * Sets the expiry date for this supply. Only perishable supplies may
     * carry an expiry date.
     *
     * @param expiryDate the expiry date to set; must not be {@code null}
     * @throws IllegalStateException    if this supply is not perishable
     * @throws IllegalArgumentException if {@code expiryDate} is {@code null}
     */
    public void setExpiryDate(LocalDate expiryDate) {
        if (!perishable) {
            throw new IllegalStateException(
                "Cannot set expiry date on a non-perishable supply");
        }
        if (expiryDate == null) {
            throw new IllegalArgumentException(
                "Expiry date cannot be null for a perishable supply");
        }
        this.expiryDate = expiryDate;
    }

    /**
     * Returns the date this supply was allocated to a victim.
     *
     * @return the allocation date, or {@code null} if not yet allocated
     */
    public LocalDate getAllocationDate() {
        return allocationDate;
    }

    /**
     * Sets the date this supply was allocated to a victim.
     *
     * @param allocationDate the allocation date
     */
    public void setAllocationDate(LocalDate allocationDate) {
        this.allocationDate = allocationDate;
    }

    /**
     * Returns the description of this supply.
     *
     * @return the description string, or {@code null} if not set
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this supply.
     *
     * @param description the new description string
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns a human-readable representation of this supply, including
     * the database ID, type, and expiry information when applicable.
     *
     * @return a formatted string describing this supply
     */
    @Override
    public String toString() {
        String base = "[" + dbId + "] " + type;
        if (perishable && expiryDate != null) {
            base += " (exp: " + expiryDate
                + (isExpired() ? " EXPIRED" : "") + ")";
        }
        return base;
    }
}