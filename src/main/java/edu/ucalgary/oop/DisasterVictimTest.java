package edu.ucalgary.oop;

import org.junit.*;
import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Unit tests for {@link DisasterVictim}.
 */
public class DisasterVictimTest {

    private DisasterVictim victim;

    @Before
    public void setUp() {
        victim = new DisasterVictim("Alice", LocalDate.of(2025, 1, 1));
    }

    @Test
    public void testConstructorSetsFirstName() {
        assertEquals("First name should be set by constructor",
            "Alice", victim.getFirstName());
    }

    @Test
    public void testConstructorSetsEntryDate() {
        assertEquals("Entry date should be set by constructor",
            LocalDate.of(2025, 1, 1), victim.getEntryDate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullFirstNameThrows() {
        new DisasterVictim(null, LocalDate.of(2025, 1, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullEntryDateThrows() {
        new DisasterVictim("Bob", null);
    }

    @Test
    public void testSetDateOfBirthSucceeds() {
        victim.setDateOfBirth(LocalDate.of(1990, 6, 15));
        assertEquals(LocalDate.of(1990, 6, 15), victim.getDateOfBirth());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDateOfBirthFutureThrows() {
        victim.setDateOfBirth(LocalDate.now().plusDays(1));
    }

    @Test(expected = IllegalStateException.class)
    public void testSetDateOfBirthWhenApproxAgeSetThrows() {
        victim.setApproximateAge(25);
        victim.setDateOfBirth(LocalDate.of(2000, 1, 1));
    }

    @Test
    public void testSetApproximateAgeSucceeds() {
        victim.setApproximateAge(30);
        assertEquals(Integer.valueOf(30), victim.getApproximateAge());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetApproximateAgeNegativeThrows() {
        victim.setApproximateAge(-1);
    }

    @Test(expected = IllegalStateException.class)
    public void testSetApproximateAgeWhenDobSetThrows() {
        victim.setDateOfBirth(LocalDate.of(1990, 1, 1));
        victim.setApproximateAge(35);
    }

    @Test
    public void testClearApproximateAgeAllowsDobSet() {
        victim.setApproximateAge(20);
        victim.clearApproximateAge();
        victim.setDateOfBirth(LocalDate.of(2000, 1, 1));
        assertNotNull("Date of birth should be set", victim.getDateOfBirth());
    }

    @Test
    public void testSetDateOfBirthNullClearsField() {
        victim.setDateOfBirth(LocalDate.of(1990, 1, 1));
        victim.setDateOfBirth(null);
        assertNull("Date of birth should be null after clearing",
            victim.getDateOfBirth());
    }

    @Test
    public void testGetAgeFromDateOfBirth() {
        victim.setDateOfBirth(LocalDate.now().minusYears(20));
        assertEquals("Age derived from DOB should be 20",
            Integer.valueOf(20), victim.getAge());
    }

    @Test
    public void testGetAgeFromApproximateAge() {
        victim.setApproximateAge(15);
        assertEquals("Age should match approximate age",
            Integer.valueOf(15), victim.getAge());
    }

    @Test
    public void testGetAgeNullWhenNeitherSet() {
        assertNull("Age should be null when neither DOB nor approx age set",
            victim.getAge());
    }

    @Test(expected = IllegalStateException.class)
    public void testSetGenderWithoutAgeThrows() {
        victim.setGender("Man");
    }

    @Test
    public void testSetGenderManWithAdultAge() {
        victim.setDateOfBirth(LocalDate.now().minusYears(30));
        victim.setGender("man");
        assertEquals("Gender should be normalized to 'Man'",
            "Man", victim.getGender());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetGenderManWithMinorAgeThrows() {
        victim.setApproximateAge(10);
        victim.setGender("Man");
    }

    @Test
    public void testSetGenderBoyWithMinorAge() {
        victim.setApproximateAge(5);
        victim.setGender("Boy");
        assertEquals("boy", "Boy", victim.getGender());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetGenderBoyWithAdultAgeThrows() {
        victim.setDateOfBirth(LocalDate.now().minusYears(25));
        victim.setGender("Boy");
    }

    @Test
    public void testSetGenderNonBinary() {
        victim.setApproximateAge(25);
        victim.setGender("non-binary person");
        assertEquals("non-binary person", victim.getGender());
    }

    @Test
    public void testSetGenderCustomViaPleasSpecify() {
        victim.setApproximateAge(25);
        victim.setGender("Please specify");
        victim.setGender("Two-Spirit");
        assertEquals("Custom gender should be stored",
            "Two-Spirit", victim.getGender());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetGenderInvalidValueThrows() {
        victim.setApproximateAge(25);
        victim.setGender("Unknown");
    }

    @Test
    public void testDefaultNotSoftDeleted() {
        assertFalse("New victim should not be soft-deleted",
            victim.isSoftDeleted());
    }

    @Test
    public void testSetSoftDeleted() {
        victim.setSoftDeleted(true);
        assertTrue("Victim should be soft-deleted",
            victim.isSoftDeleted());
    }

    @Test
    public void testAddSkill() {
        Skill skill = new TradeSkill("carpentry", "beginner");
        victim.addSkill(skill);
        assertEquals("Victim should have one skill", 1,
            victim.getSkills().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDuplicateSkillThrows() {
        victim.addSkill(new TradeSkill("carpentry", "beginner"));
        victim.addSkill(new TradeSkill("carpentry", "advanced"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullSkillThrows() {
        victim.addSkill(null);
    }

    @Test
    public void testRemoveSkill() {
        Skill skill = new TradeSkill("plumbing", "intermediate");
        victim.addSkill(skill);
        victim.removeSkill(skill);
        assertTrue("Skills list should be empty after removal",
            victim.getSkills().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveAbsentSkillThrows() {
        victim.removeSkill(new TradeSkill("electricity", "advanced"));
    }

    @Test
    public void testGetSkillsByCategory() {
        victim.addSkill(new TradeSkill("carpentry", "beginner"));
        victim.addSkill(new TradeSkill("plumbing", "advanced"));
        List<Skill> tradeSkills = victim.getSkillsByCategory("trade");
        assertEquals("Should return two trade skills", 2, tradeSkills.size());
    }

    @Test
    public void testGetSkillsByCategoryEmpty() {
        List<Skill> result = victim.getSkillsByCategory("medical");
        assertTrue("No medical skills should return empty list",
            result.isEmpty());
    }

    @Test
    public void testAddCulturalRequirement() {
        CulturalRequirement req =
            new CulturalRequirement("dietary restrictions", "halal");
        victim.addCulturalRequirement(req);
        assertEquals("Should have one cultural requirement",
            1, victim.getCulturalRequirements().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDuplicateCategoryRequirementThrows() {
        victim.addCulturalRequirement(
            new CulturalRequirement("dietary restrictions", "halal"));
        victim.addCulturalRequirement(
            new CulturalRequirement("dietary restrictions", "kosher"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullCulturalRequirementThrows() {
        victim.addCulturalRequirement(null);
    }

    @Test
    public void testSetCulturalRequirementUpdatesExisting() {
        victim.addCulturalRequirement(
            new CulturalRequirement("dietary restrictions", "halal"));
        victim.setCulturalRequirement("dietary restrictions", "kosher");
        assertEquals("Option should be updated to kosher",
            "kosher",
            victim.getCulturalRequirements().get(0).getOption());
    }

    @Test
    public void testAddPersonalBelonging() {
        Supply s = new Supply("blanket", 1);
        victim.addPersonalBelonging(s);
        assertEquals(1, victim.getPersonalBelongings().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveAbsentBelongingThrows() {
        victim.removePersonalBelonging(new Supply("blanket", 1));
    }

    @Test
    public void testAddMedicalRecord() {
        Location loc = new Location("Clinic", "1 Health St");
        MedicalRecord rec = new MedicalRecord(
            loc, "Bandaged wound", LocalDate.of(2025, 1, 5));
        victim.addMedicalRecord(rec);
        assertEquals(1, victim.getMedicalRecords().size());
    }

    @Test
    public void testAddFamilyConnection() {
        DisasterVictim other =
            new DisasterVictim("Bob", LocalDate.of(2025, 1, 2));
        FamilyRelation rel =
            new FamilyRelation(victim, "sibling", other);
        victim.addFamilyConnection(rel);
        assertEquals(1, victim.getFamilyConnections().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveAbsentFamilyConnectionThrows() {
        DisasterVictim other =
            new DisasterVictim("Bob", LocalDate.of(2025, 1, 2));
        FamilyRelation rel = new FamilyRelation(victim, "sibling", other);
        victim.removeFamilyConnection(rel);
    }

    @Test
    public void testToStringIncludesFirstName() {
        assertTrue("toString should contain first name",
            victim.toString().contains("Alice"));
    }
}