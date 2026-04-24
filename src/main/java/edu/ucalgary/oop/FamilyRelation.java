package edu.ucalgary.oop;

/**
 * Represents a family relationship between two {@link DisasterVictim} objects.
 * <p>
 * A relationship has a type description (e.g., {@code "sibling"},
 * {@code "spouse"}) and references both participating victims. Both victims
 * hold a reference to the same {@code FamilyRelation} instance in their
 * family connections list.
 * </p>
 *
 * @author Eric Hallett (30117108)
 * @version 1.0
 * @since 2026-03-25
 */
public class FamilyRelation {

    /** Database primary key; 0 until persisted. */
    private int dbId;

    /** The first victim in the relationship. */
    private DisasterVictim personOne;

    /** The second victim in the relationship. */
    private DisasterVictim personTwo;

    /** A description of the relationship (e.g., {@code "sibling"}). */
    private String relationshipTo;

    /**
     * Constructs a {@code FamilyRelation} between two victims.
     *
     * @param personOne      the first victim; must not be {@code null}
     * @param relationshipTo a description of the relationship type
     * @param personTwo      the second victim; must not be {@code null}
     * @throws IllegalArgumentException if either victim is {@code null}
     */
    public FamilyRelation(DisasterVictim personOne,
                         String relationshipTo,
                         DisasterVictim personTwo) {
        if (personOne == null || personTwo == null) {
            throw new IllegalArgumentException("Persons cannot be null");
        }
        this.personOne = personOne;
        this.personTwo = personTwo;
        this.relationshipTo = relationshipTo;
    }

    /**
     * Returns the database ID of this relationship.
     *
     * @return the database primary key, or {@code 0} if not yet persisted
     */
    public int getDbId() {
        return dbId;
    }

    /**
     * Sets the database ID of this relationship.
     *
     * @param dbId the database primary key assigned after persistence
     */
    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    /**
     * Returns the first victim in this relationship.
     *
     * @return the first {@link DisasterVictim}
     */
    public DisasterVictim getPersonOne() {
        return personOne;
    }

    /**
     * Sets the first victim in this relationship.
     *
     * @param personOne the new first victim; must not be {@code null}
     * @throws IllegalArgumentException if {@code personOne} is {@code null}
     */
    public void setPersonOne(DisasterVictim personOne) {
        if (personOne == null) throw new IllegalArgumentException(
            "personOne cannot be null");
        this.personOne = personOne;
    }

    /**
     * Returns the second victim in this relationship.
     *
     * @return the second {@link DisasterVictim}
     */
    public DisasterVictim getPersonTwo() {
        return personTwo;
    }

    /**
     * Sets the second victim in this relationship.
     *
     * @param personTwo the new second victim; must not be {@code null}
     * @throws IllegalArgumentException if {@code personTwo} is {@code null}
     */
    public void setPersonTwo(DisasterVictim personTwo) {
        if (personTwo == null) throw new IllegalArgumentException(
            "personTwo cannot be null");
        this.personTwo = personTwo;
    }

    /**
     * Returns the relationship type description.
     *
     * @return a string describing the relationship (e.g., {@code "sibling"})
     */
    public String getRelationshipTo() {
        return relationshipTo;
    }

    /**
     * Sets the relationship type description.
     *
     * @param relationshipTo a string describing the relationship type
     */
    public void setRelationshipTo(String relationshipTo) {
        this.relationshipTo = relationshipTo;
    }
}