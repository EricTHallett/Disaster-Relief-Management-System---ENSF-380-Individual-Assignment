package edu.ucalgary.oop;

/**
 * Abstract base class representing a skill that a {@link DisasterVictim} can
 * contribute during their stay in the relief system.
 * <p>
 * Each skill belongs to one of three categories ({@code "medical"},
 * {@code "language"}, or {@code "trade"}) and has a proficiency level of
 * {@code "beginner"}, {@code "intermediate"}, or {@code "advanced"}.
 * Concrete subclasses ({@link MedicalSkill}, {@link LanguageSkill},
 * {@link TradeSkill}) add category-specific validation and fields.
 * </p>
 *
 * @author Eric Hallett (30117108)
 * @version 1.0
 * @since 2026-03-27
 */
public abstract class Skill {

    /** Valid proficiency level values accepted by this class. */
    public static final String[] VALID_PROFICIENCY_LEVELS = {
        "beginner", "intermediate", "advanced"
    };

    /** Database primary key for the {@code Skill} master record. */
    private int dbId;

    /** Database primary key for the {@code VictimSkill} join record. */
    private int victimSkillDbId;

    /** The name of the skill (e.g., {@code "carpentry"}, {@code "French"}). */
    private String skillName;

    /**
     * The category this skill belongs to ({@code "medical"},
     * {@code "language"}, or {@code "trade"}).
     */
    private String category;

    /** The proficiency level ({@code "beginner"}, {@code "intermediate"},
     *  or {@code "advanced"}). */
    private String proficiencyLevel;

    /**
     * Constructs a {@code Skill} with the given name, category, and
     * proficiency level. Subclasses must call this constructor.
     *
     * @param skillName        the name of the skill; must not be {@code null}
     *                         or blank
     * @param category         the skill category; must not be {@code null}
     *                         or blank
     * @param proficiencyLevel the proficiency level; must be one of
     *                         {@code "beginner"}, {@code "intermediate"},
     *                         or {@code "advanced"}
     * @throws IllegalArgumentException if any argument is invalid
     */
    public Skill(String skillName, String category, String proficiencyLevel) {
        if (skillName == null || skillName.isBlank()) {
            throw new IllegalArgumentException(
                "Skill name cannot be null or blank");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException(
                "Category cannot be null or blank");
        }
        validateProficiencyLevel(proficiencyLevel);

        this.skillName = skillName.trim();
        this.category = category.trim().toLowerCase();
        this.proficiencyLevel = proficiencyLevel.trim().toLowerCase();
    }

    /**
     * Validates that the given proficiency level is one of the accepted values.
     *
     * @param level the proficiency level to validate
     * @throws IllegalArgumentException if {@code level} is {@code null} or
     *                                  not one of the accepted values
     */
    public static void validateProficiencyLevel(String level) {
        if (level == null) {
            throw new IllegalArgumentException(
                "Proficiency level cannot be null");
        }
        String normalized = level.trim().toLowerCase();
        for (String valid : VALID_PROFICIENCY_LEVELS) {
            if (valid.equals(normalized)) {
                return;
            }
        }
        throw new IllegalArgumentException("Invalid proficiency level: " + level
            + ". Must be one of: beginner, intermediate, advanced");
    }

    /**
     * Returns the database ID of the {@code Skill} master record.
     *
     * @return the skill table primary key, or {@code 0} if not yet persisted
     */
    public int getDbId() {
        return dbId;
    }

    /**
     * Sets the database ID of the {@code Skill} master record.
     *
     * @param dbId the primary key assigned by the database
     */
    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    /**
     * Returns the database ID of the {@code VictimSkill} join record.
     *
     * @return the VictimSkill table primary key, or {@code 0} if not yet
     *         persisted
     */
    public int getVictimSkillDbId() {
        return victimSkillDbId;
    }

    /**
     * Sets the database ID of the {@code VictimSkill} join record.
     *
     * @param victimSkillDbId the primary key assigned by the database
     */
    public void setVictimSkillDbId(int victimSkillDbId) {
        this.victimSkillDbId = victimSkillDbId;
    }

    /**
     * Returns the name of this skill.
     *
     * @return the skill name string
     */
    public String getSkillName() {
        return skillName;
    }

    /**
     * Sets the name of this skill.
     *
     * @param skillName the new skill name; must not be {@code null} or blank
     * @throws IllegalArgumentException if {@code skillName} is {@code null}
     *                                  or blank
     */
    public void setSkillName(String skillName) {
        if (skillName == null || skillName.isBlank()) {
            throw new IllegalArgumentException(
                "Skill name cannot be null or blank");
        }
        this.skillName = skillName.trim();
    }

    /**
     * Returns the category of this skill.
     *
     * @return one of {@code "medical"}, {@code "language"}, or {@code "trade"}
     */
    public String getCategory() {
        return category;
    }

    /**
     * Returns the proficiency level of this skill.
     *
     * @return one of {@code "beginner"}, {@code "intermediate"}, or
     *         {@code "advanced"}
     */
    public String getProficiencyLevel() {
        return proficiencyLevel;
    }

    /**
     * Sets the proficiency level of this skill.
     *
     * @param proficiencyLevel the new proficiency level; must be one of
     *                         {@code "beginner"}, {@code "intermediate"},
     *                         or {@code "advanced"}
     * @throws IllegalArgumentException if the value is not valid
     */
    public void setProficiencyLevel(String proficiencyLevel) {
        validateProficiencyLevel(proficiencyLevel);
        this.proficiencyLevel = proficiencyLevel.trim().toLowerCase();
    }

    /**
     * Returns a human-readable description of this skill.
     *
     * @return a string in the form {@code "category / skillName (level)"}
     */
    @Override
    public String toString() {
        return category + " / " + skillName + " (" + proficiencyLevel + ")";
    }
}