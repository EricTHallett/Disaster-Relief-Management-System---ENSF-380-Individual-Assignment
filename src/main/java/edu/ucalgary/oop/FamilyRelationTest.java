package edu.ucalgary.oop;

import org.junit.*;
import static org.junit.Assert.*;
import java.time.LocalDate;

/**
 * Unit tests for {@link FamilyRelation}.
 */
public class FamilyRelationTest {

    private DisasterVictim personOne;
    private DisasterVictim personTwo;

    @Before
    public void setUp() {
        personOne =
            new DisasterVictim("Alice", LocalDate.of(2025, 1, 1));
        personTwo =
            new DisasterVictim("Bob", LocalDate.of(2025, 1, 2));
    }

    @Test
    public void testConstructorSetsPersonOne() {
        FamilyRelation rel =
            new FamilyRelation(personOne, "sibling", personTwo);
        assertEquals("personOne should be set",
            personOne, rel.getPersonOne());
    }

    @Test
    public void testConstructorSetsPersonTwo() {
        FamilyRelation rel =
            new FamilyRelation(personOne, "sibling", personTwo);
        assertEquals("personTwo should be set",
            personTwo, rel.getPersonTwo());
    }

    @Test
    public void testConstructorSetsRelationshipType() {
        FamilyRelation rel =
            new FamilyRelation(personOne, "sibling", personTwo);
        assertEquals("Relationship type should be set",
            "sibling", rel.getRelationshipTo());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullPersonOneThrows() {
        new FamilyRelation(null, "sibling", personTwo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullPersonTwoThrows() {
        new FamilyRelation(personOne, "sibling", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPersonOneNullThrows() {
        FamilyRelation rel =
            new FamilyRelation(personOne, "sibling", personTwo);
        rel.setPersonOne(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPersonTwoNullThrows() {
        FamilyRelation rel =
            new FamilyRelation(personOne, "sibling", personTwo);
        rel.setPersonTwo(null);
    }

    @Test
    public void testSetRelationshipToUpdates() {
        FamilyRelation rel =
            new FamilyRelation(personOne, "sibling", personTwo);
        rel.setRelationshipTo("spouse");
        assertEquals("Relationship type should be updated",
            "spouse", rel.getRelationshipTo());
    }

    @Test
    public void testDbIdDefaultIsZero() {
        FamilyRelation rel =
            new FamilyRelation(personOne, "sibling", personTwo);
        assertEquals("Default DB id should be 0", 0, rel.getDbId());
    }

    @Test
    public void testSetDbIdUpdates() {
        FamilyRelation rel =
            new FamilyRelation(personOne, "sibling", personTwo);
        rel.setDbId(42);
        assertEquals("DB id should be updated", 42, rel.getDbId());
    }
}