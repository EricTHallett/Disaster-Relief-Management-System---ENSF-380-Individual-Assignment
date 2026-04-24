package edu.ucalgary.oop;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Central controller for the disaster relief management application. Mediates
 * all interactions between the user interface and the underlying data layer,
 * enforcing business rules, persisting changes to the database via a
 * {@link DatabaseQueryInterface}, and recording every user-driven action via
 * {@link ActionLogger}.
 *
 * <p>At construction time the controller loads all entities from the database
 * and deserializes the cultural options file. It exposes query methods that
 * return defensive copies of internal lists, and mutating methods that keep
 * in-memory state and the database synchronized.</p>
 *
 * @author Eric Hallett (30117108)
 * @version 1.0
 * @since 2026-03-23
 */
public class DisasterReliefController {

    private final DatabaseQueryInterface db;
    private final ActionLogger logger;

    private List<Location> locations;
    private List<DisasterVictim> victims;
    private List<Inquirer> inquirers;
    private List<ReliefService> inquiries;
    private List<Supply> supplies;
    private CulturalOptions culturalOptions;

    /**
     * Constructs a {@code DisasterReliefController} using the given database
     * query implementation. Immediately loads all data from the database and
     * the cultural options file.
     *
     * @param db the {@link DatabaseQueryInterface} used for all database
     *           operations
     */
    public DisasterReliefController(DatabaseQueryInterface db) {
        this.db = db;
        this.logger = ActionLogger.getInstance();
        loadAll();
        loadCulturalOptions();
    }

    /**
     * Loads all entities from the database into memory, including locations,
     * victims, inquirers, inquiries, supplies, medical records, family
     * relations, cultural requirements, and skills.
     */
    public void loadAll() {
        locations  = db.loadLocations();
        victims    = db.loadDisasterVictims(locations);
        inquirers  = db.loadInquirers();
        inquiries  = db.loadInquiries(victims, inquirers, locations);
        supplies   = db.loadSupplies(locations, victims);
        db.loadMedicalRecords(victims, locations);
        db.loadFamilyRelations(victims);
        db.loadCulturalRequirements(victims);
        db.loadSkills(victims);
    }

    /**
     * Deserializes the {@code available_requirements.ser} file from the
     * {@code src/main/resources/} directory and stores the result as the
     * active {@link CulturalOptions}. If the file cannot be found or read,
     * an error is logged and a {@link RuntimeException} is thrown so that
     * the caller can exit the application cleanly.
     *
     * @throws RuntimeException if the cultural options file cannot be found
     *                          or deserialized
     */
    public void loadCulturalOptions() {
        Path serPath = Paths.get(
            "src", "main", "resources",
            "available_requirements.ser");
        try (var fis = Files.newInputStream(serPath);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            culturalOptions = (CulturalOptions) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.logError("Failed to load cultural options", e);
            throw new RuntimeException(
                "Cannot read cultural options file: " +
                "available_requirements.ser. " +
                "The application cannot start without it.", e);
        }
    }

    /**
     * Returns a copy of all known locations.
     *
     * @return a new list of {@link Location} objects
     */
    public List<Location> getLocations() {
        return new ArrayList<>(locations);
    }

    /**
     * Returns a copy of all active (non-soft-deleted) victims, sorted by
     * database ID.
     *
     * @return a new list of active {@link DisasterVictim} objects
     */
    public List<DisasterVictim> getActiveVictims() {
        List<DisasterVictim> active = new ArrayList<>();
        for (DisasterVictim v : victims) {
            if (!v.isSoftDeleted()) {
                active.add(v);
            }
        }
        active.sort(Comparator.comparingInt(DisasterVictim::getDbId));
        return active;
    }

    /**
     * Returns a copy of all victims including soft-deleted ones.
     *
     * @return a new list of all {@link DisasterVictim} objects
     */
    public List<DisasterVictim> getAllVictims() {
        return new ArrayList<>(victims);
    }

    /**
     * Returns a copy of all soft-deleted (archived) victims.
     *
     * @return a new list of archived {@link DisasterVictim} objects
     */
    public List<DisasterVictim> getSoftDeletedVictims() {
        List<DisasterVictim> result = new ArrayList<>();
        for (DisasterVictim v : victims) {
            if (v.isSoftDeleted()) {
                result.add(v);
            }
        }
        return result;
    }

    /**
     * Returns a copy of all known inquirers.
     *
     * @return a new list of {@link Inquirer} objects
     */
    public List<Inquirer> getInquirers() {
        return new ArrayList<>(inquirers);
    }

    /**
     * Returns a copy of all logged inquiries.
     *
     * @return a new list of {@link ReliefService} inquiry objects
     */
    public List<ReliefService> getInquiries() {
        return new ArrayList<>(inquiries);
    }

    /**
     * Returns a copy of all supplies across all locations.
     *
     * @return a new list of {@link Supply} objects
     */
    public List<Supply> getSupplies() {
        return new ArrayList<>(supplies);
    }

    /**
     * Returns the currently loaded cultural accommodation options.
     *
     * @return the {@link CulturalOptions} loaded from the resources file
     */
    public CulturalOptions getCulturalOptions() {
        return culturalOptions;
    }

    /**
     * Replaces the cultural options with a test-provided instance. Intended
     * for use in unit tests only.
     *
     * @param options the {@link CulturalOptions} to use during testing
     */
    protected void setCulturalOptionsForTest(CulturalOptions options) {
        this.culturalOptions = options;
    }

    /**
     * Returns all active victims who have at least one skill in the given
     * category.
     *
     * @param category the skill category to search (case-insensitive)
     * @return a list of active {@link DisasterVictim} objects with matching
     *         skills
     */
    public List<DisasterVictim> searchBySkillCategory(String category) {
        List<DisasterVictim> result = new ArrayList<>();
        for (DisasterVictim v : victims) {
            if (v.isSoftDeleted()) {
                continue;
            }
            if (!v.getSkillsByCategory(category).isEmpty()) {
                result.add(v);
            }
        }
        return result;
    }

    /**
     * Returns all supplies that have passed their expiry date.
     *
     * @return a list of expired {@link Supply} objects
     */
    public List<Supply> getExpiredSupplies() {
        List<Supply> expired = new ArrayList<>();
        for (Supply s : supplies) {
            if (s.isExpired()) {
                expired.add(s);
            }
        }
        return expired;
    }

    /**
     * Returns all supplies that are neither expired nor already allocated to a
     * victim.
     *
     * @return a list of allocatable {@link Supply} objects
     */
    public List<Supply> getAllocatableSupplies() {
        List<Supply> result = new ArrayList<>();
        for (Supply s : supplies) {
            if (!s.isExpired() && s.getAllocationDate() == null) {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Adds a new disaster victim to the system, persists it to the database,
     * and logs the action.
     *
     * @param victim the {@link DisasterVictim} to add; must not be null
     * @throws IllegalArgumentException if victim is null
     */
    public void addVictim(DisasterVictim victim) {
        if (victim == null) {
            throw new IllegalArgumentException("Victim cannot be null");
        }
        int id = db.insertDisasterVictim(victim);
        victim.setDbId(id);
        victims.add(victim);
        if (victim.getLocation() != null) {
            victim.getLocation().addOccupant(victim);
        }
        logger.logAdded("disaster victim", id,
            "Name: " + victim.getFirstName()
            + (victim.getLastName() != null ? " " + victim.getLastName() : ""));
    }

    /**
     * Persists changes to an existing disaster victim and logs the update.
     *
     * @param victim      the {@link DisasterVictim} with updated field values
     * @param description a human-readable description of what changed
     */
    public void updateVictim(DisasterVictim victim, String description) {
        db.updateDisasterVictim(victim);
        logger.logUpdated("disaster victim", victim.getDbId(), description);
    }

    /**
     * Soft-deletes a victim by setting their archived flag, persisting the
     * change, and logging the action.
     *
     * @param victim the {@link DisasterVictim} to archive
     */
    public void softDeleteVictim(DisasterVictim victim) {
        if (victim == null) {
            throw new IllegalArgumentException("Victim cannot be null");
        }

        int id = victim.getDbId();
        String name = victim.getFirstName()
            + (victim.getLastName() != null ? " " + victim.getLastName() : "");

        db.setSoftDeleted(id, true);
        loadAll();
        logger.logSoftDeleted("disaster victim", id, "Name: " + name);
    }

    /**
     * Hard-deletes a victim by removing them and all associated data from both
     * the in-memory state and the database, then logs the action.
     *
     * @param victim the {@link DisasterVictim} to permanently delete
     */
    public void hardDeleteVictim(DisasterVictim victim) {
        if (victim == null) {
            throw new IllegalArgumentException("Victim cannot be null");
        }

        String name = victim.getFirstName()
            + (victim.getLastName() != null ? " " + victim.getLastName() : "");
        int id = victim.getDbId();

        db.hardDeleteDisasterVictim(id);
        loadAll();

        logger.logDeleted("disaster victim", id, "Name: " + name);
    }

    /**
     * Restores a soft-deleted victim by clearing their archived
     * flag, persisting
     * the change, and logging the action.
     *
     * @param victim the {@link DisasterVictim} to restore
     */
    public void restoreVictim(DisasterVictim victim) {
        victim.setSoftDeleted(false);
        db.setSoftDeleted(victim.getDbId(), false);
        logger.logUpdated("disaster victim", victim.getDbId(),
            "Restored from soft delete: " + victim.getFirstName()
            + victim.getLastName());
    }

    /**
     * Updates the approximate age of a victim who does not have a date of
     * birth, persists the change, and logs the action.
     *
     * @param victim the {@link DisasterVictim} to update
     * @param newAge the new approximate age; must be non-negative
     * @throws IllegalStateException if the victim already has a date of birth
     */
    public void updateApproximateAge(DisasterVictim victim, int newAge) {
        if (victim.getDateOfBirth() != null) {
            throw new IllegalStateException(
                "Cannot update approximate age: victim has a date of birth");
        }
        int oldAge = victim.getApproximateAge() != null
            ? victim.getApproximateAge() : -1;
        victim.clearApproximateAge();
        victim.setApproximateAge(newAge);
        db.updateDisasterVictim(victim);
        logger.logUpdated("disaster victim", victim.getDbId(),
            "Approximate age: " + oldAge + " -> " + newAge);
    }

    /**
     * Replaces a victim's approximate age with an exact date of birth,
     * persists the change, and logs the action.
     *
     * @param victim the {@link DisasterVictim} to update
     * @param dob    the exact date of birth to set
     * @throws IllegalStateException if the victim already has a date of birth
     */
    public void setDateOfBirthFromAge(DisasterVictim victim, LocalDate dob) {
        if (victim.getDateOfBirth() != null) {
            throw new IllegalStateException(
                "Victim already has a date of birth; "
                + "cannot replace with approximate age");
        }
        victim.clearApproximateAge();
        victim.setDateOfBirth(dob);
        db.updateDisasterVictim(victim);
        logger.logUpdated("disaster victim", victim.getDbId(),
            "Date of birth set: " + dob + " (replaced approximate age)");
    }

    /**
     * Adds a supply to a location, persists it to the database, and logs the
     * action.
     *
     * @param supply   the {@link Supply} to add
     * @param location the {@link Location} to add the supply to
     */
    public void addSupply(Supply supply, Location location) {
        int id = db.insertSupply(supply, location.getDbId());
        supply.setDbId(id);
        location.addSupply(supply);
        supplies.add(supply);
        logger.logAdded("supply", id,
            "Type: " + supply.getType() + " at " + location.getName());
    }

    /**
     * Allocates a supply to a disaster victim, persists the change, and logs
     * the action. Expired supplies cannot be allocated.
     *
     * @param supply the {@link Supply} to allocate
     * @param victim the {@link DisasterVictim} receiving the supply
     * @throws IllegalStateException if the supply is expired
     */
    public void allocateSupply(Supply supply, DisasterVictim victim) {
        if (supply.isExpired()) {
            throw new IllegalStateException(
                "Cannot allocate expired supply: " + supply.getType());
        }
        supply.setAllocationDate(LocalDate.now());
        db.allocateSupplyToVictim(supply.getDbId(), victim.getDbId());
        victim.addPersonalBelonging(supply);
        logger.logUpdated("supply", supply.getDbId(),
            "Type: " + supply.getType()
            + " -> allocated to disaster victim " + victim.getDbId());
    }

    /**
     * Adds a medical record to a victim, persists it to the database, and logs
     * the action.
     *
     * @param record the {@link MedicalRecord} to add
     * @param victim the {@link DisasterVictim} this record belongs to
     */
    public void addMedicalRecord(MedicalRecord record, DisasterVictim victim) {
        int id = db.insertMedicalRecord(record, victim.getDbId());
        record.setDbId(id);
        victim.addMedicalRecord(record);
        logger.logAdded("medical record", id,
            "Victim: " + victim.getDbId()
            + " | " + record.getTreatmentDetails());
    }

    /**
     * Adds a family relationship between two victims, persists it to the
     * database, and logs the action.
     *
     * @param relation the {@link FamilyRelation} to add
     */
    public void addFamilyRelation(FamilyRelation relation) {
        int id = db.insertFamilyRelation(relation);
        relation.setDbId(id);
        relation.getPersonOne().addFamilyConnection(relation);
        relation.getPersonTwo().addFamilyConnection(relation);
        logger.logAdded("family relationship", id,
            relation.getPersonOne().getDbId()
            + " <-> " + relation.getPersonTwo().getDbId()
            + " (" + relation.getRelationshipTo() + ")");
    }

    /**
     * Removes a family relationship, deletes it from the database, and logs the
     * action.
     *
     * @param relation the {@link FamilyRelation} to remove
     */
    public void removeFamilyRelation(FamilyRelation relation) {
        try { relation.getPersonOne().removeFamilyConnection(relation);
        } catch (Exception ignored) {}
        try { relation.getPersonTwo().removeFamilyConnection(relation);
        } catch (Exception ignored) {}
        db.deleteFamilyRelation(relation.getDbId());
        logger.logDeleted("family relationship", relation.getDbId(),
            relation.getPersonOne().getDbId()
            + " <-> " + relation.getPersonTwo().getDbId());
    }

    /**
     * Logs a new inquiry, persisting the inquirer to the database first if
     * they are new (db ID of 0), then persists the inquiry itself and logs
     * the action.
     *
     * @param inquiry   the {@link ReliefService} inquiry to add
     * @param inquirer  the {@link Inquirer} making the inquiry
     */
    public void addInquiry(ReliefService inquiry, Inquirer inquirer) {
        if (inquirer.getDbId() == 0) {
            int inquirerId = db.insertInquirer(inquirer);
            inquirer.setDbId(inquirerId);
            inquirers.add(inquirer);
        }
        int id = db.insertInquiry(inquiry, inquirer.getDbId());
        inquiry.setDbId(id);
        inquiries.add(inquiry);
        logger.logAdded("inquiry", id,
            "By: " + inquirer.getFirstName()
            + " | " + inquiry.getInfoProvided());
    }

    /**
     * Sets or updates a cultural requirement for a victim. If the category
     * already has a requirement, it is updated; otherwise a new one is added.
     * Validates the option against the loaded {@link CulturalOptions}, persists
     * the change, and logs the action.
     *
     * @param victim   the {@link DisasterVictim} to update
     * @param category the requirement category
     * @param option   the selected option
     * @throws IllegalArgumentException if the option is not valid for the
     *                                  category
     */
    public void setCulturalRequirement(DisasterVictim victim,
                                       String category, String option) {
        if (!culturalOptions.isValidOption(category, option)) {
            throw new IllegalArgumentException(
                "Invalid option '" + option
                + "' for category '" + category + "'");
        }
        CulturalRequirement existing = null;
        for (CulturalRequirement req : victim.getCulturalRequirements()) {
            if (req.getCategory().equalsIgnoreCase(category)) {
                existing = req; break;
            }
        }
        if (existing == null) {
            CulturalRequirement newReq =
                new CulturalRequirement(category, option);
            int id = db.insertCulturalRequirement(newReq, victim.getDbId());
            newReq.setDbId(id);
            victim.addCulturalRequirement(newReq);
            logger.logAdded("cultural requirement", id,
                "Victim: " + victim.getDbId()
                + " | " + category + ": " + option);
        } else {
            String old = existing.getOption();
            existing.setOption(option);
            db.updateCulturalRequirement(existing);
            logger.logUpdated("cultural requirement", existing.getDbId(),
                "Victim: " + victim.getDbId()
                + " | " + category + ": " + old + " -> " + option);
        }
    }

    /**
     * Removes a cultural requirement from a victim, deletes it from the
     * database, and logs the action.
     *
     * @param victim the {@link DisasterVictim} to update
     * @param req    the {@link CulturalRequirement} to remove
     */
    public void removeCulturalRequirement(DisasterVictim victim,
                                          CulturalRequirement req) {
        victim.removeCulturalRequirement(req);
        db.deleteCulturalRequirement(req.getDbId());
        logger.logDeleted("cultural requirement", req.getDbId(),
            "Victim: " + victim.getDbId()
            + " | " + req.getCategory() + ": " + req.getOption());
    }

    /**
     * Adds a skill to a victim, persists the association to the database, and
     * logs the action.
     *
     * @param victim the {@link DisasterVictim} to update
     * @param skill  the {@link Skill} to add
     */
    public void addSkill(DisasterVictim victim, Skill skill) {
        victim.addSkill(skill);
        int vsId = db.insertVictimSkill(skill, victim.getDbId());
        skill.setVictimSkillDbId(vsId);
        logger.logAdded("skill", vsId,
            "Victim: " + victim.getDbId()
            + " | " + skill.getCategory() + "/" + skill.getSkillName());
    }

    /**
     * Removes a skill from a victim, deletes the association from the database,
     * and logs the action.
     *
     * @param victim the {@link DisasterVictim} to update
     * @param skill  the {@link Skill} to remove
     */
    public void removeSkill(DisasterVictim victim, Skill skill) {
        victim.removeSkill(skill);
        db.deleteVictimSkill(skill.getVictimSkillDbId());
        logger.logDeleted("skill", skill.getVictimSkillDbId(),
            "Victim: " + victim.getDbId()
            + " | " + skill.getCategory() + "/" + skill.getSkillName());
    }

    /**
     * Adds a new location to the system.
     * Persists the location, assigns its database ID, updates
     * in-memory storage, and logs the action.
     *
     * @param location the location to add
     */
    public void addLocation(Location location) {
        int id = db.insertLocation(location);
        location.setDbId(id);
        locations.add(location);
        logger.logAdded("location", id,
            "Name: " + location.getName()
            + " | Address: " + location.getAddress());
    }

    /**
     * Updates an existing location in the system, persists the change,
     * and records the update in the action log.
     *
     * @param location the location to update
     * @param description a human-readable description of the change
     */
    public void updateLocation(Location location, String description) {
        db.updateLocation(location);
        logger.logUpdated("location", location.getDbId(), description);
    }

    /**
     * Updates an existing supply in the system.
     * Persists the changes and logs the update.
     *
     * @param supply the supply to update
     * @param locationId the location ID, or {@code null}
     * @param victimId the victim ID, or {@code null}
     * @param description a description of the update for logging
     */
    public void updateSupply(Supply supply,
            Integer locationId, Integer victimId,
            String description) {
        db.updateSupply(supply, locationId, victimId);
        logger.logUpdated("supply", supply.getDbId(), description);
    }

    /**
     * Updates an existing inquiry and persists the changes.
     *
     * <p>The provided {@link ReliefService} object must already exist in the
     * system. This method updates the database record, keeps the in-memory
     * inquiry list synchronized, and records the modification in the action
     * log.</p>
     *
     * @param inquiry the inquiry object containing the updated values
     * @throws IllegalArgumentException if {@code inquiry} is {@code null}
     */
    public void updateInquiry(ReliefService inquiry) {
        if (inquiry == null) {
            throw new IllegalArgumentException("Inquiry cannot be null");
        }

        db.updateInquiry(inquiry);

        for (int i = 0; i < inquiries.size(); i++) {
            if (inquiries.get(i).getDbId() == inquiry.getDbId()) {
                inquiries.set(i, inquiry);
                break;
            }
        }

        logger.logUpdated(
            "inquiry",
            inquiry.getDbId(),
            "Updated inquiry for "
                + (inquiry.getMissingPerson() != null
                    ? inquiry.getMissingPerson().toString()
                    : "unknown victim")
        );
    }

    /**
     * Updates an existing medical record, persists the changes, and logs the
     * update.
     *
     * @param record the {@link MedicalRecord} with updated field values
     * @param description a human-readable description of what changed
     * @throws IllegalArgumentException if {@code record} is {@code null}
     */
    public void updateMedicalRecord(MedicalRecord record, String description) {
        if (record == null) {
            throw new IllegalArgumentException("Medical record cannot be null");
        }

        db.updateMedicalRecord(record);
        logger.logUpdated("medical record", record.getDbId(), description);
    }

    /**
     * Updates an existing family relationship, persists the changes, and logs
     * the update.
     *
     * @param relation the {@link FamilyRelation} with updated field values
     * @param description a human-readable description of what changed
     * @throws IllegalArgumentException if {@code relation} is {@code null}
     */
    public void updateFamilyRelation(FamilyRelation relation,
                                    String description) {
        if (relation == null) {
            throw new IllegalArgumentException(
                "Family relationship cannot be null");
        }

        db.updateFamilyRelation(relation);

        logger.logUpdated("family relationship",
            relation.getDbId(), description);
    }

    /**
     * Returns the inquiries that should remain visible in the application.
     * Inquiries involving soft-deleted disaster victims are excluded.
     *
     * @return a list of inquiries visible to the user
     */
    public List<ReliefService> getVisibleInquiries() {
        List<ReliefService> visible = new ArrayList<>();
        for (ReliefService inquiry : inquiries) {
            DisasterVictim missing = inquiry.getMissingPerson();
            if (missing == null || !missing.isSoftDeleted()) {
                visible.add(inquiry);
            }
        }
        return visible;
    }
}