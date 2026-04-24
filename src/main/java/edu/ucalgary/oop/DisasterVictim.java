package edu.ucalgary.oop;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a person who has been registered as a disaster victim in the
 * relief management system.
 * <p>
 * A victim is identified by a first name and an entry date. Age may be
 * recorded as either an exact date of birth or an approximate age — never
 * both simultaneously. Gender is validated against census categories and
 * requires an age to be set first. Victims may be soft-deleted (archived)
 * to hide them from the user interface while retaining their database record,
 * or hard-deleted to remove all of their data permanently.
 * </p>
 *
 * @author Eric Hallett (30117108)
 * @version 1.0
 * @since 2026-03-24
 */
public class DisasterVictim {

    /** Database primary key; 0 until persisted. */
    private int dbId;

    /** The victim's first name. */
    private String firstName;

    /** The victim's last name; may be {@code null}. */
    private String lastName;

    /** Free-text comments about this victim; may be {@code null}. */
    private String comments;

    /** The date this victim was entered into the system. */
    private final LocalDate entryDate;

    /** The victim's exact date of birth; mutually exclusive
     *  with approximateAge. */
    private LocalDate dateOfBirth;

    /** The victim's approximate age; mutually exclusive with dateOfBirth. */
    private Integer approximateAge;

    /** The victim's gender. */
    private String gender;

    /**
     * Internal flag that allows a two-step custom gender entry where the
     * first call to {@link #setGender(String)} passes {@code "Please specify"}
     * and the second call provides the actual value.
     */
    private boolean allowCustomGender;

    /** Whether this victim has been soft-deleted (archived). */
    private boolean isSoftDeleted;

    /** Supplies allocated to this victim. */
    private List<Supply> personalBelongings;

    /** Family relationships this victim is involved in. */
    private List<FamilyRelation> familyConnections;

    /** Medical treatment records for this victim. */
    private List<MedicalRecord> medicalRecords;

    /** Cultural and religious requirements for this victim. */
    private List<CulturalRequirement> culturalRequirements;

    /** Skills this victim has registered. */
    private List<Skill> skills;

    /** The location where this victim is currently staying. */
    private Location location;

    /**
     * Constructs a new {@code DisasterVictim} with the given first name and
     * entry date. All list fields are initialized as empty.
     *
     * @param firstName the victim's first name; must not be {@code null}
     * @param entryDate the date the victim was entered into the system;
     *                  must not be {@code null}
     * @throws IllegalArgumentException if {@code firstName} or
     *                                  {@code entryDate} is {@code null}
     */
    public DisasterVictim(String firstName, LocalDate entryDate) {
        if (firstName == null || entryDate == null) {
            throw new IllegalArgumentException(
                "firstName and entryDate cannot be null");
        }
        this.firstName = firstName;
        this.entryDate = entryDate;
        this.isSoftDeleted = false;

        this.personalBelongings = new ArrayList<>();
        this.familyConnections = new ArrayList<>();
        this.medicalRecords = new ArrayList<>();
        this.culturalRequirements = new ArrayList<>();
        this.skills = new ArrayList<>();
    }

    /**
     * Returns the database ID of this victim.
     *
     * @return the database primary key, or {@code 0} if not yet persisted
     */
    public int getDbId() {
        return dbId;
    }

    /**
     * Sets the database ID of this victim.
     *
     * @param dbId the database primary key assigned after persistence
     */
    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    /**
     * Returns whether this victim has been soft-deleted (archived).
     *
     * @return {@code true} if the victim is archived; {@code false} otherwise
     */
    public boolean isSoftDeleted() {
        return isSoftDeleted;
    }

    /**
     * Sets the soft-delete (archive) flag for this victim.
     *
     * @param softDeleted {@code true} to archive the victim;
     *                    {@code false} to restore them
     */
    public void setSoftDeleted(boolean softDeleted) {
        this.isSoftDeleted = softDeleted;
    }

    /**
     * Returns the date this victim was entered into the system.
     *
     * @return the entry date as a {@link LocalDate}
     */
    public LocalDate getEntryDate() {
        return entryDate;
    }

    /**
     * Sets the victim's exact date of birth. Mutually exclusive with
     * approximate age — if an approximate age is already set, this method
     * will throw.
     *
     * @param dob the date of birth; passing {@code null} clears the field
     * @throws IllegalStateException    if an approximate age is already set
     * @throws IllegalArgumentException if {@code dob} is in the future
     */
    public void setDateOfBirth(LocalDate dob) {
        if (dob == null) {
            this.dateOfBirth = null;
            return;
        }
        if (approximateAge != null) {
            throw new IllegalStateException(
                "Cannot set date of birth when approximate age is already set");
        }
        if (dob.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                "Date of birth cannot be in the future");
        }
        this.dateOfBirth = dob;
    }

    /**
     * Returns the victim's exact date of birth.
     *
     * @return the date of birth, or {@code null} if not set
     */
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Sets the victim's approximate age. Mutually exclusive with date of
     * birth — if a date of birth is already set, this method will throw.
     *
     * @param age the approximate age in years; must not be negative
     * @throws IllegalStateException    if a date of birth is already set
     * @throws IllegalArgumentException if {@code age} is negative
     */
    public void setApproximateAge(int age) {
        if (dateOfBirth != null) {
            throw new IllegalStateException(
                "Cannot set approximate age when date of birth is already set");
        }
        if (age < 0) {
            throw new IllegalArgumentException(
                "Approximate age cannot be negative");
        }
        this.approximateAge = age;
    }

    /**
     * Returns the victim's approximate age.
     *
     * @return the approximate age as an {@code Integer}, or {@code null} if
     *         not set
     */
    public Integer getApproximateAge() {
        return approximateAge;
    }

    /**
     * Clears the approximate age field, allowing a date of birth to be set
     * afterwards.
     */
    public void clearApproximateAge() {
        this.approximateAge = null;
    }

    /**
     * Returns the victim's current age in years. If a date of birth is set,
     * the age is calculated precisely; otherwise the stored approximate age
     * is returned.
     *
     * @return the age in years, or {@code null} if neither field is set
     */
    public Integer getAge() {
        if (dateOfBirth != null) {
            return Period.between(dateOfBirth, LocalDate.now()).getYears();
        }
        return approximateAge;
    }

    /**
     * Sets the victim's first name.
     *
     * @param firstName the new first name; must not be {@code null} or blank
     * @throws IllegalArgumentException if {@code firstName} is {@code null}
     *                                  or blank
     */
    public void setFirstName(String firstName) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException(
                "First name cannot be null or blank");
        }
        this.firstName = firstName;
    }

    /**
     * Returns the victim's first name.
     *
     * @return the first name string
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the victim's last name. {@code null} is accepted to represent an
     * unknown last name.
     *
     * @param lastName the last name, or {@code null} if unknown
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the victim's last name.
     *
     * @return the last name, or {@code null} if not set
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets free-text comments about this victim.
     *
     * @param comments the comments string, or {@code null} to clear
     */
    public void setComments(String comments) {
        this.comments = comments;
    }

    /**
     * Returns the free-text comments about this victim.
     *
     * @return the comments string, or {@code null} if not set
     */
    public String getComments() {
        return comments;
    }

    /**
     * Sets the victim's gender, validating against census categories.
     * <p>
     * Standard values are {@code "Man"}, {@code "Woman"}, {@code "Boy"},
     * {@code "Girl"}, and {@code "non-binary person"}. Adult categories
     * (Man/Woman) require age 18+; minor categories (Boy/Girl) require age
     * under 18. To enter a custom gender, call this method first with
     * {@code "Please specify"}, then call it again with the custom value.
     * </p>
     *
     * @param input the gender string to set
     * @throws IllegalArgumentException if the gender is invalid for the
     *                                  victim's age
     * @throws IllegalStateException    if neither age nor date of birth is
     *                                  set when required
     */
    public void setGender(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Gender cannot be null");
        }
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Gender cannot be empty");
        }
        if (trimmed.equalsIgnoreCase("Please specify")) {
            allowCustomGender = true;
            return;
        }
        if (allowCustomGender) {
            this.gender = trimmed;
            allowCustomGender = false;
            return;
        }

        String normalized = normalizeCensusGender(trimmed);

        Integer age = getAge();
        if (age == null) {
            throw new IllegalStateException(
                "Age (date of birth or approximate age) "
                + "must be set before setting gender");
        }
        boolean adult = age >= 18;

        if ((normalized.equals("Man") || normalized.equals("Woman"))
                && !adult) {
            throw new IllegalArgumentException("Man/Woman requires age 18+");
        }
        if ((normalized.equals("Boy") || normalized.equals("Girl")) && adult) {
            throw new IllegalArgumentException(
                "Boy/Girl requires age under 18");
        }

        this.gender = normalized;
    }

    /**
     * Returns the victim's gender.
     *
     * @return the gender string, or {@code null} if not set
     */
    public String getGender() {
        return gender;
    }

    /**
     * Sets the gender field directly, bypassing age-based validation.
     * This method is intended for database loading only, where the gender
     * value was already validated when it was first stored.
     *
     * @param gender the gender value to store
     */
    void setGenderDirect(String gender) {
        this.gender = gender;
    }

    /**
     * Normalizes a gender input string to the canonical census form.
     *
     * @param s the input gender string
     * @return the normalized gender string
     * @throws IllegalArgumentException if the string does not match any
     *                                  recognized census option
     */
    private String normalizeCensusGender(String s) {
        if (s.equalsIgnoreCase("Man")) {
            return "Man";
        }
        if (s.equalsIgnoreCase("Woman")) {
            return "Woman";
        }
        if (s.equalsIgnoreCase("Boy")) {
            return "Boy";
        }
        if (s.equalsIgnoreCase("Girl")) {
            return "Girl";
        }
        if (s.equalsIgnoreCase("non-binary person")) {
            return "non-binary person";
        }
        throw new IllegalArgumentException("Invalid gender option: " + s);
    }

    /**
     * Sets the location where this victim is currently staying.
     *
     * @param location the new location, or {@code null} to unassign
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Returns the location where this victim is currently staying.
     *
     * @return the current {@link Location}, or {@code null} if unassigned
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Adds a supply to this victim's personal belongings. Silently ignores
     * {@code null} values.
     *
     * @param supply the {@link Supply} to add
     */
    public void addPersonalBelonging(Supply supply) {
        if (supply == null) {
            return;
        }
        personalBelongings.add(supply);
    }

    /**
     * Removes a supply from this victim's personal belongings.
     *
     * @param supply the {@link Supply} to remove
     * @throws IllegalArgumentException if the supply is not in the list
     */
    public void removePersonalBelonging(Supply supply) {
        if (!personalBelongings.remove(supply)) {
            throw new IllegalArgumentException(
                "Supply not found in personal belongings");
        }
    }

    /**
     * Returns a copy of this victim's personal belongings list.
     *
     * @return a new {@code List} of {@link Supply} objects
     */
    public List<Supply> getPersonalBelongings() {
        return new ArrayList<>(personalBelongings);
    }

    /**
     * Replaces the personal belongings list. If {@code null} is passed, the
     * list is cleared.
     *
     * @param supplies the new list of supplies, or {@code null} to clear
     */
    public void setPersonalBelongings(List<Supply> supplies) {
        this.personalBelongings = (supplies == null)
            ? new ArrayList<>() : new ArrayList<>(supplies);
    }

    /**
     * Adds a family connection to this victim. Silently ignores {@code null}.
     *
     * @param relation the {@link FamilyRelation} to add
     */
    public void addFamilyConnection(FamilyRelation relation) {
        if (relation == null) {
            return;
        }
        familyConnections.add(relation);
    }

    /**
     * Removes a family connection from this victim.
     *
     * @param relation the {@link FamilyRelation} to remove
     * @throws IllegalArgumentException if the relation is not in the list
     */
    public void removeFamilyConnection(FamilyRelation relation) {
        if (!familyConnections.remove(relation)) {
            throw new IllegalArgumentException("Family relation not found");
        }
    }

    /**
     * Returns a copy of this victim's family connections list.
     *
     * @return a new {@code List} of {@link FamilyRelation} objects
     */
    public List<FamilyRelation> getFamilyConnections() {
        return new ArrayList<>(familyConnections);
    }

    /**
     * Replaces the family connections list. If {@code null} is passed, the
     * list is cleared.
     *
     * @param relations the new list of relations, or {@code null} to clear
     */
    public void setFamilyConnections(List<FamilyRelation> relations) {
        this.familyConnections = (relations == null)
            ? new ArrayList<>() : new ArrayList<>(relations);
    }

    /**
     * Adds a medical record to this victim. Silently ignores {@code null}.
     *
     * @param record the {@link MedicalRecord} to add
     */
    public void addMedicalRecord(MedicalRecord record) {
        if (record == null) {
            return;
        }
        medicalRecords.add(record);
    }

    /**
     * Returns a copy of this victim's medical records list.
     *
     * @return a new {@code List} of {@link MedicalRecord} objects
     */
    public List<MedicalRecord> getMedicalRecords() {
        return new ArrayList<>(medicalRecords);
    }

    /**
     * Replaces the medical records list. If {@code null} is passed, the list
     * is cleared.
     *
     * @param records the new list of records, or {@code null} to clear
     */
    public void setMedicalRecords(List<MedicalRecord> records) {
        this.medicalRecords = (records == null)
            ? new ArrayList<>() : new ArrayList<>(records);
    }

    /**
     * Adds a cultural requirement to this victim. A victim may only hold one
     * requirement per category.
     *
     * @param req the {@link CulturalRequirement} to add; must not be
     *            {@code null}
     * @throws IllegalArgumentException if {@code req} is {@code null} or a
     *                                  requirement for the same category is
     *                                  already set
     */
    public void addCulturalRequirement(CulturalRequirement req) {
        if (req == null) {
            throw new IllegalArgumentException(
                "Cultural requirement cannot be null");
        }
        for (CulturalRequirement existing : culturalRequirements) {
            if (existing.getCategory().equalsIgnoreCase(req.getCategory())) {
                throw new IllegalArgumentException(
                    "Victim already has a requirement for category: "
                    + req.getCategory());
            }
        }
        culturalRequirements.add(req);
    }

    /**
     * Removes a cultural requirement from this victim.
     *
     * @param req the {@link CulturalRequirement} to remove
     * @throws IllegalArgumentException if the requirement is not in the list
     */
    public void removeCulturalRequirement(CulturalRequirement req) {
        if (!culturalRequirements.remove(req)) {
            throw new IllegalArgumentException(
                "Cultural requirement not found");
        }
    }

    /**
     * Sets or updates a cultural requirement for the given category. If a
     * requirement for the category already exists, its option is updated;
     * otherwise a new requirement is added.
     *
     * @param category the requirement category
     * @param option   the selected option for the category
     */
    public void setCulturalRequirement(String category, String option) {
        for (CulturalRequirement req : culturalRequirements) {
            if (req.getCategory().equalsIgnoreCase(category)) {
                req.setOption(option);
                return;
            }
        }
        culturalRequirements.add(new CulturalRequirement(category, option));
    }

    /**
     * Returns a copy of this victim's cultural requirements list.
     *
     * @return a new {@code List} of {@link CulturalRequirement} objects
     */
    public List<CulturalRequirement> getCulturalRequirements() {
        return new ArrayList<>(culturalRequirements);
    }

    /**
     * Replaces the cultural requirements list. If {@code null} is passed, the
     * list is cleared.
     *
     * @param reqs the new list of requirements, or {@code null} to clear
     */
    public void setCulturalRequirements(List<CulturalRequirement> reqs) {
        this.culturalRequirements = (reqs == null)
            ? new ArrayList<>() : new ArrayList<>(reqs);
    }

    /**
     * Adds a skill to this victim. A victim may not register the same skill
     * type more than once within the same category.
     *
     * @param skill the {@link Skill} to add; must not be {@code null}
     * @throws IllegalArgumentException if {@code skill} is {@code null} or a
     *                                  skill with the same name and category
     *                                  already exists
     */
    public void addSkill(Skill skill) {
        if (skill == null) {
            throw new IllegalArgumentException("Skill cannot be null");
        }
        for (Skill existing : skills) {
            if (existing.getCategory().equalsIgnoreCase(skill.getCategory())
                    && existing.getSkillName()
                        .equalsIgnoreCase(skill.getSkillName())) {
                throw new IllegalArgumentException(
                    "Victim already has skill: " + skill.getSkillName()
                    + " in category " + skill.getCategory());
            }
        }
        skills.add(skill);
    }

    /**
     * Removes a skill from this victim.
     *
     * @param skill the {@link Skill} to remove
     * @throws IllegalArgumentException if the skill is not in the list
     */
    public void removeSkill(Skill skill) {
        if (!skills.remove(skill)) {
            throw new IllegalArgumentException("Skill not found");
        }
    }

    /**
     * Returns a copy of all skills registered by this victim.
     *
     * @return a new {@code List} of {@link Skill} objects
     */
    public List<Skill> getSkills() {
        return new ArrayList<>(skills);
    }

    /**
     * Returns all skills for this victim that belong to the specified category.
     *
     * @param category the skill category to filter by (case-insensitive)
     * @return a new {@code List} of matching {@link Skill} objects;
     *         empty if none found
     */
    public List<Skill> getSkillsByCategory(String category) {
        List<Skill> result = new ArrayList<>();
        for (Skill s : skills) {
            if (s.getCategory().equalsIgnoreCase(category)) {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Replaces the skills list. If {@code null} is passed, the list is cleared.
     *
     * @param skills the new list of skills, or {@code null} to clear
     */
    public void setSkills(List<Skill> skills) {
        this.skills = (skills == null)
            ? new ArrayList<>() : new ArrayList<>(skills);
    }

    /**
     * Returns a human-readable representation including the database ID and
     * full name.
     *
     * @return a string in the form {@code "[id] firstName lastName"}
     */
    @Override
    public String toString() {
        String name = firstName + (lastName != null ? " " + lastName : "");
        return "[" + dbId + "] " + name;
    }
}