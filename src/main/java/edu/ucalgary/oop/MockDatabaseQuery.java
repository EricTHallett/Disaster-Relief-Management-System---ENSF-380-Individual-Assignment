package edu.ucalgary.oop;

import java.time.LocalDate;
import java.util.*;

/**
 * In-memory stub implementation of {@link DatabaseQueryInterface} for use in
 * unit tests. All load methods return hard-coded objects and all mutating
 * methods are no-ops. No database connection is required.
 *
 * <p>The stub provides a small, deterministic data set that exercises the main
 * code paths: two locations, two active victims (one with a date of birth, one
 * with an approximate age), one soft-deleted victim, one inquirer, one inquiry,
 * three supplies (non-perishable, fresh perishable, and expired perishable),
 * one medical record, and one cultural requirement.</p>
 *
 * @author Eric Hallett (30117108)
 * @version 1.0
 * @since 2026-03-24
 */
public class MockDatabaseQuery implements DatabaseQueryInterface {

    private int nextId = 100;

    /**
     * Constructs an empty mock database query backend.
     */
    public MockDatabaseQuery() {
    }
    
    /**
     * Returns the next auto-incremented mock ID.
     *
     * @return an integer ID that increments with each call
     */
    private int nextId() {
        return nextId++;
    }

    /**
     * Returns two hard-coded test locations.
     *
     * @return a list containing "Test Shelter A" and "Test Shelter B"
     */
    @Override
    public List<Location> loadLocations() {
        List<Location> locs = new ArrayList<>();
        Location l1 = new Location("Test Shelter A", "100 Test Ave");
        l1.setDbId(1);
        Location l2 = new Location("Test Shelter B", "200 Test Blvd");
        l2.setDbId(2);
        locs.add(l1);
        locs.add(l2);
        return locs;
    }

    /**
     * Returns three hard-coded test victims: one with a date of birth, one with
     * an approximate age, and one soft-deleted victim.
     *
     * @param locations the list of locations used to assign victim locations
     * @return a list of three {@link DisasterVictim} objects
     */
    @Override
    public List<DisasterVictim> loadDisasterVictims(List<Location> locations) {
        List<DisasterVictim> victims = new ArrayList<>();

        DisasterVictim v1 = new DisasterVictim(
            "Jane", LocalDate.of(2025, 1, 10));
        v1.setDbId(10);
        v1.setLastName("Doe");
        v1.setDateOfBirth(LocalDate.of(1990, 5, 15));
        if (!locations.isEmpty()) {
            v1.setLocation(locations.get(0));
            locations.get(0).addOccupant(v1);
        }
        victims.add(v1);

        DisasterVictim v2 = new DisasterVictim(
            "John", LocalDate.of(2025, 1, 11));
        v2.setDbId(11);
        v2.setApproximateAge(7);
        if (!locations.isEmpty()) {
            v2.setLocation(locations.get(0));
            locations.get(0).addOccupant(v2);
        }
        victims.add(v2);

        DisasterVictim v3 = new DisasterVictim(
            "Deleted", LocalDate.of(2025, 1, 12));
        v3.setDbId(12);
        v3.setSoftDeleted(true);
        victims.add(v3);

        return victims;
    }

    /**
     * Returns one hard-coded test inquirer.
     *
     * @return a list containing a single {@link Inquirer}
     */
    @Override
    public List<Inquirer> loadInquirers() {
        List<Inquirer> list = new ArrayList<>();
        Inquirer i1 = new Inquirer(
            "Bob", "Smith", "555-1234", "Seeking family");
        i1.setDbId(50);
        list.add(i1);
        return list;
    }

    /**
     * Returns one hard-coded test inquiry linking the first inquirer to the
     * first victim.
     *
     * @param victims   the list of loaded victims used for association
     * @param inquirers the list of loaded inquirers used for association
     * @param locations unused in this stub
     * @return a list containing a single {@link ReliefService} inquiry
     */
    @Override
    public List<ReliefService> loadInquiries(List<DisasterVictim> victims,
                                              List<Inquirer> inquirers,
                                              List<Location> locations) {
        List<ReliefService> list = new ArrayList<>();
        if (!inquirers.isEmpty() && !victims.isEmpty()) {
            ReliefService rs = new ReliefService(
                inquirers.get(0), victims.get(0),
                LocalDate.of(2025, 1, 15), "Looking for family");
            rs.setDbId(200);
            list.add(rs);
        }
        return list;
    }

    /**
     * Returns three hard-coded test supplies: one non-perishable (blanket), one
     * fresh perishable (water), and one expired perishable (food ration).
     *
     * @param locations the list of locations; supplies are added to the first
     *                  location
     * @param victims   unused in this stub
     * @return a list of three {@link Supply} objects
     */
    @Override
    public List<Supply> loadSupplies(List<Location> locations,
                                     List<DisasterVictim> victims) {
        List<Supply> list = new ArrayList<>();

        Supply s1 = new Supply(301, "blanket", null, null, "Wool blanket");
        if (!locations.isEmpty()) {
            locations.get(0).addSupply(s1);
        }
        list.add(s1);

        Supply s2 = new Supply(302, "water",
            LocalDate.now().plusDays(30), null, "Case of 24");
        if (!locations.isEmpty()) {
            locations.get(0).addSupply(s2);
        }
        list.add(s2);

        Supply s3 = new Supply(303, "food ration",
            LocalDate.of(2024, 1, 1), null, "Expired item");
        if (!locations.isEmpty()) {
            locations.get(0).addSupply(s3);
        }
        list.add(s3);

        return list;
    }

    /**
     * Returns one hard-coded medical record attached to the first victim.
     *
     * @param victims   the list of victims; the record is attached to the first
     *                  victim
     * @param locations the list of locations; the record's
     *                  treatment location is
     *                  the first location
     * @return a list containing a single {@link MedicalRecord}
     */
    @Override
    public List<MedicalRecord> loadMedicalRecords(List<DisasterVictim> victims,
                                                   List<Location> locations) {
        List<MedicalRecord> list = new ArrayList<>();
        if (!victims.isEmpty() && !locations.isEmpty()) {
            MedicalRecord rec = new MedicalRecord(
                locations.get(0), "Broken arm treated",
                LocalDate.of(2025, 1, 10));
            rec.setDbId(401);
            victims.get(0).addMedicalRecord(rec);
            list.add(rec);
        }
        return list;
    }

    /**
     * Returns an empty list; no family relations are defined in the stub.
     *
     * @param victims unused
     * @return an empty list
     */
    @Override
    public List<FamilyRelation> loadFamilyRelations(
            List<DisasterVictim> victims) {
        return new ArrayList<>();
    }

    /**
     * Returns one hard-coded cultural requirement ("dietary restrictions:
     * vegetarian") attached to the first victim.
     *
     * @param victims the list of victims; the requirement is attached to the
     *                first victim
     * @return a list containing a single {@link CulturalRequirement}
     */
    @Override
    public List<CulturalRequirement> loadCulturalRequirements(
            List<DisasterVictim> victims) {
        List<CulturalRequirement> list = new ArrayList<>();
        if (!victims.isEmpty()) {
            CulturalRequirement req = new CulturalRequirement(
                "dietary restrictions", "vegetarian");
            req.setDbId(501);
            victims.get(0).setCulturalRequirement(
                "dietary restrictions", "vegetarian");
            list.add(req);
        }
        return list;
    }

    /**
     * Returns an empty list; no skills are pre-loaded in the stub.
     *
     * @param victims unused
     * @return an empty list
     */
    @Override
    public List<Skill> loadSkills(List<DisasterVictim> victims) {
        return new ArrayList<>();
    }

    /**
     * Simulates inserting a disaster victim by returning a new mock ID.
     *
     * @param victim unused
     * @return a new mock database ID
     */
    @Override
    public int insertDisasterVictim(DisasterVictim victim) {
        return nextId();
    }

    /**
     * No-op stub for updating a disaster victim.
     *
     * @param victim unused
     */
    @Override
    public void updateDisasterVictim(DisasterVictim victim) {}

    /**
     * No-op stub for setting the soft-delete flag.
     *
     * @param victimDbId  unused
     * @param softDeleted unused
     */
    @Override
    public void setSoftDeleted(int victimDbId, boolean softDeleted) {}

    /**
     * No-op stub for hard-deleting a disaster victim.
     *
     * @param victimDbId unused
     */
    @Override
    public void hardDeleteDisasterVictim(int victimDbId) {}

    /**
     * Simulates inserting a supply by returning a new mock ID.
     *
     * @param supply     unused
     * @param locationId unused
     * @return a new mock database ID
     */
    @Override
    public int insertSupply(Supply supply, int locationId) {
        return nextId();
    }

    /**
     * No-op stub for allocating a supply to a victim.
     *
     * @param supplyDbId unused
     * @param victimDbId unused
     */
    @Override
    public void allocateSupplyToVictim(int supplyDbId, int victimDbId) {}

    /**
     * Simulates inserting a medical record by returning a new mock ID.
     *
     * @param record     unused
     * @param victimDbId unused
     * @return a new mock database ID
     */
    @Override
    public int insertMedicalRecord(MedicalRecord record, int victimDbId) {
        return nextId();
    }

    /**
     * Simulates inserting a family relation by returning a new mock ID.
     *
     * @param relation unused
     * @return a new mock database ID
     */
    @Override
    public int insertFamilyRelation(FamilyRelation relation) {
        return nextId();
    }

    /**
     * No-op stub for deleting a family relation.
     *
     * @param relationDbId unused
     */
    @Override
    public void deleteFamilyRelation(int relationDbId) {}

    /**
     * Simulates inserting an inquirer by returning a new mock ID.
     *
     * @param inquirer unused
     * @return a new mock database ID
     */
    @Override
    public int insertInquirer(Inquirer inquirer) {
        return nextId();
    }

    /**
     * Simulates inserting an inquiry by returning a new mock ID.
     *
     * @param inquiry    unused
     * @param inquirerId unused
     * @return a new mock database ID
     */
    @Override
    public int insertInquiry(ReliefService inquiry, int inquirerId) {
        return nextId();
    }

    /**
     * Simulates inserting a cultural requirement by returning a new mock ID.
     *
     * @param req        unused
     * @param victimDbId unused
     * @return a new mock database ID
     */
    @Override
    public int insertCulturalRequirement(
            CulturalRequirement req, int victimDbId) {
        return nextId();
    }

    /**
     * No-op stub for updating a cultural requirement.
     *
     * @param req unused
     */
    @Override
    public void updateCulturalRequirement(CulturalRequirement req) {}

    /**
     * No-op stub for deleting a cultural requirement.
     *
     * @param reqDbId unused
     */
    @Override
    public void deleteCulturalRequirement(int reqDbId) {}

    /**
     * Simulates inserting a victim skill by returning a new mock ID.
     *
     * @param skill      unused
     * @param victimDbId unused
     * @return a new mock database ID
     */
    @Override
    public int insertVictimSkill(Skill skill, int victimDbId) {
        return nextId();
    }

    /**
     * No-op stub for deleting a victim skill.
     *
     * @param victimSkillDbId unused
     */
    @Override
    public void deleteVictimSkill(int victimSkillDbId) {}

    /**
     * Simulates inserting a new location into the database.
     *
     * @param location the location to insert
     * @return a generated mock ID
     */
    @Override
    public int insertLocation(Location location) {
        return nextId();
    }

    /**
     * Simulates inserting a new location into the database.
     *
     * @param location the location to insert
     */
    @Override
    public void updateLocation(Location location) {
    }

    /**
     * Simulates updating a supply in the database.
     *
     * @param supply the supply with updated values
     * @param locationId the location ID, or {@code null}
     * @param victimId the victim ID, or {@code null}
     */
    @Override
    public void updateSupply(Supply supply,
            Integer locationId, Integer victimId) {
    }

    /**
     * Updates an existing inquiry in the mock data source.
     *
     * <p>This stub implementation performs no persistence and exists only to
     * satisfy the {@link DatabaseQueryInterface} contract for tests which do
     * not require inquiry update behavior.</p>
     *
     * @param inquiry the inquiry object containing updated data
     */
    @Override
    public void updateInquiry(ReliefService inquiry) {
    }

    /**
     * Updates an existing medical record in the mock data source.
     *
     * <p>This stub implementation performs no persistence and exists only to
     * satisfy the {@link DatabaseQueryInterface} contract for tests which do
     * not require medical record update behavior.</p>
     *
     * @param record the {@link MedicalRecord} containing updated data
     */
    @Override
    public void updateMedicalRecord(MedicalRecord record) {
    }

    /**
     * Updates an existing family relationship in the mock data source.
     *
     * <p>This stub implementation performs no persistence and exists only to
     * satisfy the {@link DatabaseQueryInterface} contract for tests which do
     * not require family relationship update behavior.</p>
     *
     * @param relation the {@link FamilyRelation} containing updated data
     */
    @Override
    public void updateFamilyRelation(FamilyRelation relation) {
    }
}