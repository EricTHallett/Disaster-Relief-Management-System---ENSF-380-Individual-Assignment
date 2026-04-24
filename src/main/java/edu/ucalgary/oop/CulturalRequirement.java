package edu.ucalgary.oop;

/**
 * Represents a single cultural or religious requirement assigned to a
 * {@link DisasterVictim}.
 * <p>
 * Each requirement belongs to a category (e.g., {@code "dietary restrictions"})
 * and has one selected option within that category (e.g., {@code "halal"}).
 * A victim may hold at most one requirement per category.
 * </p>
 *
 * @author Eric Hallett (30117108)
 * @version 1.0
 * @since 2026-03-24
 */
public class CulturalRequirement {

    /** Database primary key; 0 until persisted. */
    private int dbId;

    /** The requirement category (e.g., {@code "dietary restrictions"}). */
    private String category;

    /** The selected option within the category (e.g., {@code "halal"}). */
    private String option;

    /**
     * Constructs a {@code CulturalRequirement} with the given category and
     * option, trimming surrounding whitespace from both values.
     *
     * @param category the requirement category; must not be
     *                 {@code null} or blank
     * @param option   the selected option; must not be {@code null} or blank
     * @throws IllegalArgumentException if {@code category} or {@code option}
     *                                  is {@code null} or blank
     */
    public CulturalRequirement(String category, String option) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException(
                "Category cannot be null or blank");
        }
        if (option == null || option.isBlank()) {
            throw new IllegalArgumentException(
                "Option cannot be null or blank");
        }
        this.category = category.trim();
        this.option = option.trim();
    }

    /**
     * Returns the database ID of this requirement.
     *
     * @return the database primary key, or {@code 0} if not yet persisted
     */
    public int getDbId() { return dbId; }

    /**
     * Sets the database ID of this requirement.
     *
     * @param dbId the database primary key assigned after persistence
     */
    public void setDbId(int dbId) { this.dbId = dbId; }

    /**
     * Returns the requirement category.
     *
     * @return the category string (e.g., {@code "dietary restrictions"})
     */
    public String getCategory() { return category; }

    /**
     * Sets the requirement category, trimming surrounding whitespace.
     *
     * @param category the new category; must not be {@code null} or blank
     * @throws IllegalArgumentException if {@code category} is {@code null}
     *                                  or blank
     */
    public void setCategory(String category) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException(
                "Category cannot be null or blank");
        }
        this.category = category.trim();
    }

    /**
     * Returns the selected option for this requirement.
     *
     * @return the option string (e.g., {@code "halal"})
     */
    public String getOption() { return option; }

    /**
     * Sets the selected option for this requirement, trimming whitespace.
     *
     * @param option the new option; must not be {@code null} or blank
     * @throws IllegalArgumentException if {@code option} is {@code null}
     *                                  or blank
     */
    public void setOption(String option) {
        if (option == null || option.isBlank()) {
            throw new IllegalArgumentException(
                "Option cannot be null or blank");
        }
        this.option = option.trim();
    }

    /**
     * Returns a human-readable representation in the form
     * {@code "category: option"}.
     *
     * @return a formatted string describing this requirement
     */
    @Override
    public String toString() {
        return category + ": " + option;
    }
}