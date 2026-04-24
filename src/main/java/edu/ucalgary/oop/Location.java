package edu.ucalgary.oop;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a physical relief location such as a shelter or hospital.
 * <p>
 * A location holds a list of {@link DisasterVictim} occupants and a list of
 * {@link Supply} items. Soft-deleted victims are excluded from
 * {@link #getOccupants()} but are still accessible via
 * {@link #getAllOccupants()}.
 * </p>
 *
 * @author Eric Hallett (30117108)
 * @version 1.0
 * @since 2026-03-26
 */
public class Location {

    /** Database primary key; 0 until persisted. */
    private int dbId;

    /** The name of this location (e.g., {@code "Shelter A"}). */
    private String name;

    /** The street address of this location. */
    private String address;

    /** All occupants at this location, including soft-deleted victims. */
    private List<DisasterVictim> occupants;

    /** Supplies held at this location. */
    private List<Supply> supplies;

    /**
     * Constructs a {@code Location} with the given name and address.
     * Both occupant and supply lists are initialized as empty.
     *
     * @param name    the name of the location; must not be {@code null}
     * @param address the street address of the location
     */
    public Location(String name, String address) {
        this.name = name;
        this.address = address;
        this.occupants = new ArrayList<>();
        this.supplies = new ArrayList<>();
    }

    /**
     * Returns the database ID of this location.
     *
     * @return the database primary key, or {@code 0} if not yet persisted
     */
    public int getDbId() {
        return dbId;
    }

    /**
     * Sets the database ID of this location.
     *
     * @param dbId the database primary key assigned after persistence
     */
    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    /**
     * Returns the name of this location.
     *
     * @return the location name string
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this location.
     *
     * @param name the new name for this location
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the street address of this location.
     *
     * @return the address string
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the street address of this location.
     *
     * @param address the new address for this location
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Returns a list of active (non-soft-deleted) occupants at this location.
     *
     * @return a new {@code List} containing only victims where
     *         {@link DisasterVictim#isSoftDeleted()} is {@code false}
     */
    public List<DisasterVictim> getOccupants() {
        List<DisasterVictim> visible = new ArrayList<>();
        for (DisasterVictim v : occupants) {
            if (!v.isSoftDeleted()) {
                visible.add(v);
            }
        }
        return visible;
    }

    /**
     * Returns a list of all occupants at this location, including
     * soft-deleted victims.
     *
     * @return a new {@code List} containing all occupants
     */
    public List<DisasterVictim> getAllOccupants() {
        return new ArrayList<>(occupants);
    }

    /**
     * Adds a victim to this location's occupant list. Silently ignores
     * {@code null} values.
     *
     * @param victim the {@link DisasterVictim} to add
     */
    public void addOccupant(DisasterVictim victim) {
        if (victim == null) {
            return;
        }
        occupants.add(victim);
    }

    /**
     * Removes a victim from this location's occupant list.
     *
     * @param victim the {@link DisasterVictim} to remove
     * @throws IllegalArgumentException if the victim is not found in the list
     */
    public void removeOccupant(DisasterVictim victim) {
        if (!occupants.remove(victim)) {
            throw new IllegalArgumentException("Occupant not found");
        }
    }

    /**
     * Replaces the entire occupant list with the provided list. If
     * {@code null} is passed, the list is cleared.
     *
     * @param occupants the new list of occupants, or {@code null} to clear
     */
    public void setOccupants(List<DisasterVictim> occupants) {
        this.occupants = (occupants == null)
            ? new ArrayList<>() : new ArrayList<>(occupants);
    }

    /**
     * Returns all supplies currently held at this location.
     *
     * @return a new {@code List} of {@link Supply} objects
     */
    public List<Supply> getSupplies() {
        return new ArrayList<>(supplies);
    }

    /**
     * Adds a supply to this location. Silently ignores {@code null} values.
     *
     * @param supply the {@link Supply} to add
     */
    public void addSupply(Supply supply) {
        if (supply == null) {
            return;
        }
        supplies.add(supply);
    }

    /**
     * Removes a supply from this location.
     *
     * @param supply the {@link Supply} to remove
     * @throws IllegalArgumentException if the supply is not found in the list
     */
    public void removeSupply(Supply supply) {
        if (!supplies.remove(supply)) {
            throw new IllegalArgumentException("Supply not found");
        }
    }

    /**
     * Replaces the entire supply list with the provided list. If {@code null}
     * is passed, the list is cleared.
     *
     * @param supplies the new list of supplies, or {@code null} to clear
     */
    public void setSupplies(List<Supply> supplies) {
        this.supplies = (supplies == null)
            ? new ArrayList<>() : new ArrayList<>(supplies);
    }

    /**
     * Returns a human-readable representation including the database ID,
     * name, and address.
     *
     * @return a string in the form {@code "[id] name — address"}
     */
    @Override
    public String toString() {
        return "[" + dbId + "] " + name + " \u2014 " + address;
    }
}