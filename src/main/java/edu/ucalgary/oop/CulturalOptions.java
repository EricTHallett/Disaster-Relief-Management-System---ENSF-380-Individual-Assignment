package edu.ucalgary.oop;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

/**
 * Serializable container for the cultural and religious accommodation options
 * available at a relief site.
 * <p>
 * An instance of this class is deserialized at startup from
 * {@code src/main/resources/available_requirements.ser}. Each key in the
 * internal map represents a requirement category (e.g.,
 * {@code "dietary restrictions"}) and the associated value is the set of
 * valid options for that category (e.g., {@code "halal"}, {@code "kosher"}).
 * </p>
 *
 * @author Eric Hallett (30117108)
 * @version 1.0
 * @since 2026-03-24
 */
public class CulturalOptions implements Serializable {

    /** Serial version UID required for safe deserialization. */
    static final long serialVersionUID = 1L;

    /** Maps requirement categories to their sets of valid options. */
    private final HashMap<String, Set<String>> accommodations;

    /**
     * Constructs a {@code CulturalOptions} instance with the given
     * accommodation map.
     *
     * @param accommodations a map from category names to sets of valid options;
     *                       must not be {@code null}
     */
    public CulturalOptions(HashMap<String, Set<String>> accommodations) {
        this.accommodations = accommodations;
    }

    /**
     * Returns the full accommodations map.
     *
     * @return a {@code HashMap} mapping category names to sets of valid options
     */
    public HashMap<String, Set<String>> getAccommodations() {
        return accommodations;
    }

    /**
     * Returns the set of valid options for the specified category.
     *
     * @param category the requirement category to look up
     * @return the {@code Set} of valid option strings for that category,
     *         or {@code null} if the category does not exist
     */
    public Set<String> getOptionsForCategory(String category) {
        return accommodations.get(category);
    }

    /**
     * Returns the set of all available requirement category names.
     *
     * @return a {@code Set} of category name strings
     */
    public Set<String> getCategories() {
        return accommodations.keySet();
    }

    /**
     * Returns whether the given option is valid for the given category.
     *
     * @param category the requirement category to check
     * @param option   the option value to validate
     * @return {@code true} if the category exists and contains the option;
     *         {@code false} otherwise
     */
    public boolean isValidOption(String category, String option) {
        Set<String> options = accommodations.get(category);
        return options != null && options.contains(option);
    }
}