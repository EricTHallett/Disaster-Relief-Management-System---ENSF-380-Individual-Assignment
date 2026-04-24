package edu.ucalgary.oop;

/**
 * Represents a trade skill held by a {@link DisasterVictim}.
 * <p>
 * Trade skills have a fixed set of valid types: {@code "carpentry"},
 * {@code "plumbing"}, and {@code "electricity"}. The type is validated and
 * normalized to lowercase during construction.
 * </p>
 *
 * @author Eric Hallett (30117108)
 * @version 1.0
 * @since 2026-03-28
 */
public class TradeSkill extends Skill {

    /** The accepted trade skill type values. */
    public static final String[] VALID_TRADE_TYPES = {
        "carpentry", "plumbing", "electricity"
    };

    /**
     * Constructs a {@code TradeSkill} with the given trade type and
     * proficiency level.
     *
     * @param tradeType        the type of trade skill; must be one of
     *                         {@code "carpentry"}, {@code "plumbing"}, or
     *                         {@code "electricity"}
     * @param proficiencyLevel the proficiency level; must be one of
     *                         {@code "beginner"}, {@code "intermediate"},
     *                         or {@code "advanced"}
     * @throws IllegalArgumentException if {@code tradeType} is invalid or
     *                                  {@code null}
     */
    public TradeSkill(String tradeType, String proficiencyLevel) {
        super(validateAndReturn(tradeType), "trade", proficiencyLevel);
    }

    /**
     * Validates the given trade type and returns it normalized to lowercase.
     *
     * @param tradeType the trade type string to validate
     * @return the normalized lowercase trade type string
     * @throws IllegalArgumentException if {@code tradeType} is {@code null}
     *                                  or not one of the accepted values
     */
    private static String validateAndReturn(String tradeType) {
        if (tradeType == null) {
            throw new IllegalArgumentException("Trade type cannot be null");
        }
        String normalized = tradeType.trim().toLowerCase();
        for (String valid : VALID_TRADE_TYPES) {
            if (valid.equals(normalized)) {
                return normalized;
            }
        }
        throw new IllegalArgumentException("Invalid trade type: " + tradeType
            + ". Must be one of: carpentry, plumbing, electricity");
    }

    /**
     * Validates that the given trade type is one of the accepted values.
     * This is a convenience wrapper around the private validation method.
     *
     * @param tradeType the trade type to validate
     * @throws IllegalArgumentException if {@code tradeType} is invalid
     */
    public static void validateTradeType(String tradeType) {
        validateAndReturn(tradeType);
    }

    /**
     * Returns a human-readable description including the trade type and
     * proficiency level.
     *
     * @return a formatted string in the form
     *         {@code "trade / tradeType (level)"}
     */
    @Override
    public String toString() {
        return "trade / " + getSkillName() + " (" + getProficiencyLevel() + ")";
    }
}