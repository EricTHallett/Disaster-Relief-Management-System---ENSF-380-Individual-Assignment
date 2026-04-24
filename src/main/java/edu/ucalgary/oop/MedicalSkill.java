package edu.ucalgary.oop;

import java.time.LocalDate;

/**
 * Represents a medical skill held by a {@link DisasterVictim}.
 * <p>
 * Medical skills require a certification type (one of {@code "first-aid"},
 * {@code "counseling"}, {@code "nursing"}, or {@code "doctor"}) and a
 * certification expiry date. The expiry date may be in the past, indicating
 * an expired certification, but it must not be {@code null}.
 * </p>
 *
 * @author Eric Hallett (30117108)
 * @version 1.0
 * @since 2026-03-28
 */
public class MedicalSkill extends Skill {

    /** The accepted medical certification type values. */
    public static final String[] VALID_CERT_TYPES = {
        "first-aid", "counseling", "nursing", "doctor"
    };

    /** The type of medical certification held. */
    private String certType;

    /** The date on which the certification expires. */
    private LocalDate certificationExpiry;

    /**
     * Constructs a {@code MedicalSkill} with the given name, proficiency
     * level, certification type, and expiry date.
     *
     * @param skillName            the name of the skill (typically the same
     *                             as {@code certType})
     * @param proficiencyLevel     the proficiency level; must be one of
     *                             {@code "beginner"}, {@code "intermediate"},
     *                             or {@code "advanced"}
     * @param certType             the certification type; must be one of the
     *                             values in {@link #VALID_CERT_TYPES}
     * @param certificationExpiry  the certification expiry date;
     *                             must not be {@code null}
     * @throws IllegalArgumentException if {@code certType} is invalid or
     *                                  {@code certificationExpiry} is
     *                                  {@code null}
     */
    public MedicalSkill(String skillName, String proficiencyLevel,
                        String certType, LocalDate certificationExpiry) {
        super(skillName, "medical", proficiencyLevel);
        validateCertType(certType);
        if (certificationExpiry == null) {
            throw new IllegalArgumentException(
                "Certification expiry cannot be null for medical skills");
        }
        this.certType = certType.trim().toLowerCase();
        this.certificationExpiry = certificationExpiry;
    }

    /**
     * Validates that the given certification type is one of
     * the accepted values.
     *
     * @param certType the certification type to validate
     * @throws IllegalArgumentException if {@code certType} is {@code null} or
     *                                  not one of the accepted values
     */
    public static void validateCertType(String certType) {
        if (certType == null) {
            throw new IllegalArgumentException(
                "Certification type cannot be null");
        }
        String normalized = certType.trim().toLowerCase();
        for (String valid : VALID_CERT_TYPES) {
            if (valid.equals(normalized)) {
                return;
            }
        }
        throw new IllegalArgumentException(
            "Invalid certification type: " + certType
            + ". Must be one of: first-aid, counseling, nursing, doctor");
    }

    /**
     * Returns the certification type of this medical skill.
     *
     * @return one of {@code "first-aid"}, {@code "counseling"},
     *         {@code "nursing"}, or {@code "doctor"}
     */
    public String getCertType() {
        return certType;
    }

    /**
     * Sets the certification type of this medical skill.
     *
     * @param certType the new certification type; must be one of the values
     *                 in {@link #VALID_CERT_TYPES}
     * @throws IllegalArgumentException if the value is not valid
     */
    public void setCertType(String certType) {
        validateCertType(certType);
        this.certType = certType.trim().toLowerCase();
    }

    /**
     * Returns the certification expiry date.
     *
     * @return the expiry date as a {@link LocalDate}
     */
    public LocalDate getCertificationExpiry() {
        return certificationExpiry;
    }

    /**
     * Sets the certification expiry date.
     *
     * @param certificationExpiry the new expiry date; must not be {@code null}
     * @throws IllegalArgumentException if {@code certificationExpiry} is
     *                                  {@code null}
     */
    public void setCertificationExpiry(LocalDate certificationExpiry) {
        if (certificationExpiry == null) {
            throw new IllegalArgumentException(
                "Certification expiry cannot be null");
        }
        this.certificationExpiry = certificationExpiry;
    }

    /**
     * Returns whether the certification for this skill has expired.
     *
     * @return {@code true} if the expiry date is before today;
     *         {@code false} otherwise
     */
    public boolean isCertificationExpired() {
        return certificationExpiry.isBefore(LocalDate.now());
    }

    /**
     * Returns a human-readable description including the skill name,
     * certification type, expiry date, and proficiency level.
     *
     * @return a formatted string describing this medical skill
     */
    @Override
    public String toString() {
        return "medical / " + getSkillName()
            + " [" + certType + ", exp: " + certificationExpiry + "] ("
            + getProficiencyLevel() + ")";
    }
}