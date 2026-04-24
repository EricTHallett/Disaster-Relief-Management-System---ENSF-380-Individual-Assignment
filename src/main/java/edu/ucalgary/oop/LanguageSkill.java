package edu.ucalgary.oop;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a language skill held by a {@link DisasterVictim}.
 * <p>
 * A language skill tracks the language name (e.g., {@code "French"}) and
 * the capabilities the person has in that language. Valid capabilities are
 * {@code "read/write"} and {@code "speak/listen"}; at least one must be
 * provided. A victim may have both capabilities for the same language.
 * </p>
 *
 * @author Eric Hallett (30117108)
 * @version 1.0
 * @since 2026-03-28
 */
public class LanguageSkill extends Skill {

    /** The accepted language capability values. */
    public static final String[] VALID_CAPABILITIES = {
        "read/write", "speak/listen"
    };

    /** The set of capabilities this person has in the language. */
    private Set<String> capabilities;

    /**
     * Constructs a {@code LanguageSkill} for the given language with the
     * specified proficiency level and capabilities.
     *
     * @param language         the language name (e.g., {@code "French"});
     *                         must not be {@code null} or blank
     * @param proficiencyLevel the proficiency level; must be one of
     *                         {@code "beginner"}, {@code "intermediate"},
     *                         or {@code "advanced"}
     * @param capabilities     a non-empty set of capabilities; each element
     *                         must be {@code "read/write"} or
     *                         {@code "speak/listen"}
     * @throws IllegalArgumentException if {@code capabilities} is {@code null},
     *                                  empty, or contains an invalid value
     */
    public LanguageSkill(String language, String proficiencyLevel,
                         Set<String> capabilities) {
        super(language, "language", proficiencyLevel);
        if (capabilities == null || capabilities.isEmpty()) {
            throw new IllegalArgumentException(
                "Language capabilities cannot be null or empty");
        }
        for (String cap : capabilities) {
            validateCapability(cap);
        }
        this.capabilities = new HashSet<>(capabilities);
    }

    /**
     * Validates that the given capability string is one of the accepted values.
     *
     * @param capability the capability to validate
     * @throws IllegalArgumentException if {@code capability} is {@code null}
     *                                  or not one of the accepted values
     */
    public static void validateCapability(String capability) {
        if (capability == null) {
            throw new IllegalArgumentException("Capability cannot be null");
        }
        String normalized = capability.trim().toLowerCase();
        for (String valid : VALID_CAPABILITIES) {
            if (valid.equals(normalized)) {
                return;
            }
        }
        throw new IllegalArgumentException("Invalid capability: " + capability
            + ". Must be one of: read/write, speak/listen");
    }

    /**
     * Returns a copy of the capabilities set for this language skill.
     *
     * @return a {@code Set} containing {@code "read/write"} and/or
     *         {@code "speak/listen"}
     */
    public Set<String> getCapabilities() {
        return new HashSet<>(capabilities);
    }

    /**
     * Adds a capability to this language skill.
     *
     * @param capability the capability to add; must be {@code "read/write"}
     *                   or {@code "speak/listen"}
     * @throws IllegalArgumentException if the capability is not valid
     */
    public void addCapability(String capability) {
        validateCapability(capability);
        capabilities.add(capability.trim().toLowerCase());
    }

    /**
     * Removes a capability from this language skill.
     *
     * @param capability the capability to remove
     * @throws IllegalArgumentException if the capability is not currently
     *                                  in the set
     */
    public void removeCapability(String capability) {
        if (!capabilities.remove(capability)) {
            throw new IllegalArgumentException(
                "Capability not found: " + capability);
        }
    }

    /**
     * Returns the capabilities as a comma-separated string for database
     * storage.
     *
     * @return a comma-separated string of capability values
     */
    public String getCapabilitiesAsString() {
        return String.join(", ", capabilities);
    }

    /**
     * Returns a human-readable description including the language name,
     * capabilities, and proficiency level.
     *
     * @return a formatted string describing this language skill
     */
    @Override
    public String toString() {
        return "language / " + getSkillName()
            + " [" + getCapabilitiesAsString() + "] ("
            + getProficiencyLevel() + ")";
    }
}