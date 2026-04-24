package edu.ucalgary.oop;

import java.util.List;

/**
 * Defines all database operations required by the disaster relief application.
 * Implementations include {@link DatabaseQuery} (live PostgreSQL) and
 * {@link MockDatabaseQuery} (in-memory stub for testing).
 *
 * <p>Separating the interface from the implementation allows business-logic
 * classes to depend on this abstraction rather than on a concrete database
 * class, supporting dependency injection and isolated unit testing.</p>
 *
 * @author Eric Hallett (30117108)
 * @version 1.0
 * @since 2026-03-26
 */
public interface DatabaseQueryInterface {

    /**
     * Loads all locations from the database.
     *
     * @return a list of {@link Location} objects
     */
    List<Location> loadLocations();

    /**
     * Loads all disaster victims from the database and associates each with
     * their location.
     *
     * @param locations the list of already-loaded locations
     *                  used for association
     * @return a list of {@link DisasterVictim} objects
     */
    List<DisasterVictim> loadDisasterVictims(List<Location> locations);

    /**
     * Loads all inquirers from the database.
     *
     * @return a list of {@link Inquirer} objects
     */
    List<Inquirer> loadInquirers();

    /**
     * Loads all inquiries from the database and links them to their associated
     * victims, inquirers, and locations.
     *
     * @param victims   the list of already-loaded disaster victims
     * @param inquirers the list of already-loaded inquirers
     * @param locations the list of already-loaded locations
     * @return a list of {@link ReliefService} inquiry objects
     */
    List<ReliefService> loadInquiries(List<DisasterVictim> victims,
                                      List<Inquirer> inquirers,
                                      List<Location> locations);

    /**
     * Loads all supplies from the database and associates each with its
     * location or allocated victim.
     *
     * @param locations the list of already-loaded locations
     * @param victims   the list of already-loaded disaster victims
     * @return a list of {@link Supply} objects
     */
    List<Supply> loadSupplies(List<Location> locations,
                              List<DisasterVictim> victims);

    /**
     * Loads all medical records from the database and attaches each to the
     * appropriate disaster victim.
     *
     * @param victims   the list of already-loaded disaster victims
     * @param locations the list of already-loaded locations
     * @return a list of {@link MedicalRecord} objects
     */
    List<MedicalRecord> loadMedicalRecords(List<DisasterVictim> victims,
                                           List<Location> locations);

    /**
     * Loads all family relationships from the database and attaches each to
     * the appropriate disaster victims.
     *
     * @param victims the list of already-loaded disaster victims
     * @return a list of {@link FamilyRelation} objects
     */
    List<FamilyRelation> loadFamilyRelations(List<DisasterVictim> victims);

    /**
     * Loads all cultural requirements from the database and attaches each to
     * the appropriate disaster victim.
     *
     * @param victims the list of already-loaded disaster victims
     * @return a list of {@link CulturalRequirement} objects
     */
    List<CulturalRequirement> loadCulturalRequirements(
            List<DisasterVictim> victims);

    /**
     * Loads all skills from the database and attaches each to the appropriate
     * disaster victim.
     *
     * @param victims the list of already-loaded disaster victims
     * @return a list of {@link Skill} objects
     */
    List<Skill> loadSkills(List<DisasterVictim> victims);

    /**
     * Inserts a new disaster victim into the database.
     *
     * @param victim the {@link DisasterVictim} to persist
     * @return the database-assigned ID for the new victim
     */
    int insertDisasterVictim(DisasterVictim victim);

    /**
     * Updates an existing disaster victim's record in the database.
     *
     * @param victim the {@link DisasterVictim} with updated field values
     */
    void updateDisasterVictim(DisasterVictim victim);

    /**
     * Sets or clears the soft-delete flag for a disaster victim in the
     * database.
     *
     * @param victimDbId  the database ID of the victim
     * @param softDeleted {@code true} to archive the victim, {@code false} to
     *                    restore them
     */
    void setSoftDeleted(int victimDbId, boolean softDeleted);

    /**
     * Permanently removes a disaster victim and all their associated records
     * (medical records, supplies, inquiries, skills, relationships) from the
     * database.
     *
     * @param victimDbId the database ID of the victim to delete
     */
    void hardDeleteDisasterVictim(int victimDbId);

    /**
     * Inserts a new supply record into the database at the specified location.
     *
     * @param supply     the {@link Supply} to persist
     * @param locationId the database ID of the location holding the supply
     * @return the database-assigned ID for the new supply
     */
    int insertSupply(Supply supply, int locationId);

    /**
     * Allocates a supply to a disaster victim in the database, setting the
     * allocation date to the current date.
     *
     * @param supplyDbId the database ID of the supply
     * @param victimDbId the database ID of the victim receiving the supply
     */
    void allocateSupplyToVictim(int supplyDbId, int victimDbId);

    /**
     * Inserts a new medical record into the database for the specified victim.
     *
     * @param record     the {@link MedicalRecord} to persist
     * @param victimDbId the database ID of the victim this record belongs to
     * @return the database-assigned ID for the new medical record
     */
    int insertMedicalRecord(MedicalRecord record, int victimDbId);

    /**
     * Inserts a new family relationship into the database.
     *
     * @param relation the {@link FamilyRelation} to persist
     * @return the database-assigned ID for the new relationship
     */
    int insertFamilyRelation(FamilyRelation relation);

    /**
     * Permanently removes a family relationship from the database.
     *
     * @param relationDbId the database ID of the relationship to delete
     */
    void deleteFamilyRelation(int relationDbId);

    /**
     * Inserts a new inquirer as a person record into the database.
     *
     * @param inquirer the {@link Inquirer} to persist
     * @return the database-assigned ID for the new person record
     */
    int insertInquirer(Inquirer inquirer);

    /**
     * Inserts a new inquiry into the database.
     *
     * @param inquiry    the {@link ReliefService} inquiry to persist
     * @param inquirerId the database ID of the inquirer making the inquiry
     * @return the database-assigned ID for the new inquiry
     */
    int insertInquiry(ReliefService inquiry, int inquirerId);

    /**
     * Inserts a new cultural requirement for a victim into the database.
     *
     * @param req        the {@link CulturalRequirement} to persist
     * @param victimDbId the database ID of the victim this
     *                   requirement belongs to
     * @return the database-assigned ID for the new requirement
     */
    int insertCulturalRequirement(CulturalRequirement req, int victimDbId);

    /**
     * Updates an existing cultural requirement's selected option in the
     * database.
     *
     * @param req the {@link CulturalRequirement} with the updated option value
     */
    void updateCulturalRequirement(CulturalRequirement req);

    /**
     * Permanently removes a cultural requirement from the database.
     *
     * @param reqDbId the database ID of the requirement to delete
     */
    void deleteCulturalRequirement(int reqDbId);

    /**
     * Inserts a skill assignment for a victim into the database, creating the
     * skill definition row if it does not already exist.
     *
     * @param skill      the {@link Skill} to persist
     * @param victimDbId the database ID of the victim this skill belongs to
     * @return the database-assigned ID for the new victim-skill association row
     */
    int insertVictimSkill(Skill skill, int victimDbId);

    /**
     * Permanently removes a victim-skill association from the database.
     *
     * @param victimSkillDbId the database ID of the victim-skill association to
     *                        delete
     */
    void deleteVictimSkill(int victimSkillDbId);

    /**
     * Inserts a new location into the database.
     *
     * @param location the location to insert
     * @return the generated database ID for the location
     */
    int insertLocation(Location location);

    /**
     * Updates an existing location in the database.
     *
     * @param location the location with updated values
     */
    void updateLocation(Location location);

    /**
     * Updates an existing supply in the database.
     *
     * @param supply the supply with updated values
     * @param locationId the location ID, or null if not stored at a location
     * @param victimId the victim ID, or null if not allocated
     */
    void updateSupply(Supply supply, Integer locationId, Integer victimId);

    /**
     * Updates an existing inquiry record in the database.
     *
     * <p>The inquiry must already exist in the database (i.e., have a valid
     * database ID). Implementations must persist any changes made to the
     * inquiry's fields, such as the inquirer, missing person, date, or
     * details.</p>
     *
     * @param inquiry the {@link ReliefService} inquiry object with updated data
     */
    void updateInquiry(ReliefService inquiry);

    /**
     * Updates an existing medical record in the database.
     *
     * @param record the {@link MedicalRecord} with updated field values
     */
    void updateMedicalRecord(MedicalRecord record);

    /**
     * Updates an existing family relationship in the database.
     *
     * @param relation the {@link FamilyRelation} with updated field values
     */
    void updateFamilyRelation(FamilyRelation relation);
}