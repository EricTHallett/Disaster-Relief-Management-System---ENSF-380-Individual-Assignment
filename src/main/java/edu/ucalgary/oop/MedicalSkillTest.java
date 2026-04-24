package edu.ucalgary.oop;

import org.junit.*;
import static org.junit.Assert.*;
import java.time.LocalDate;

/**
 * Unit tests for {@link MedicalSkill}.
 */
public class MedicalSkillTest {

    private LocalDate futureExpiry;
    private LocalDate pastExpiry;

    @Before
    public void setUp() {
        futureExpiry = LocalDate.now().plusYears(1);
        pastExpiry   = LocalDate.of(2020, 1, 1);
    }

    @Test
    public void testConstructorSetsCertType() {
        MedicalSkill skill =
            new MedicalSkill("nursing", "intermediate",
                "nursing", futureExpiry);
        assertEquals("nursing", skill.getCertType());
    }

    @Test
    public void testConstructorSetsCertificationExpiry() {
        MedicalSkill skill =
            new MedicalSkill("first-aid", "beginner",
                "first-aid", futureExpiry);
        assertEquals(futureExpiry, skill.getCertificationExpiry());
    }

    @Test
    public void testConstructorSetsCategoryToMedical() {
        MedicalSkill skill =
            new MedicalSkill("doctor", "advanced", "doctor", futureExpiry);
        assertEquals("medical", skill.getCategory());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidCertTypeThrows() {
        new MedicalSkill("physio", "beginner", "physio", futureExpiry);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullCertTypeThrows() {
        new MedicalSkill("nursing", "beginner", null, futureExpiry);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullExpiryThrows() {
        new MedicalSkill("nursing", "beginner", "nursing", null);
    }

    @Test
    public void testValidateCertTypeFirstAidSucceeds() {
        MedicalSkill.validateCertType("first-aid");
    }

    @Test
    public void testValidateCertTypeCaseInsensitive() {
        MedicalSkill.validateCertType("NURSING");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateCertTypeInvalidThrows() {
        MedicalSkill.validateCertType("physiotherapy");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateCertTypeNullThrows() {
        MedicalSkill.validateCertType(null);
    }

    @Test
    public void testIsCertificationExpiredFutureDate() {
        MedicalSkill skill =
            new MedicalSkill("nursing", "advanced", "nursing", futureExpiry);
        assertFalse("Future expiry should not be expired",
            skill.isCertificationExpired());
    }

    @Test
    public void testIsCertificationExpiredPastDate() {
        MedicalSkill skill =
            new MedicalSkill("first-aid", "beginner",
                "first-aid", pastExpiry);
        assertTrue("Past expiry date should be expired",
            skill.isCertificationExpired());
    }

    @Test
    public void testSetCertTypeUpdates() {
        MedicalSkill skill =
            new MedicalSkill("nursing", "intermediate",
                "nursing", futureExpiry);
        skill.setCertType("doctor");
        assertEquals("doctor", skill.getCertType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetCertTypeInvalidThrows() {
        MedicalSkill skill =
            new MedicalSkill("nursing", "intermediate",
                "nursing", futureExpiry);
        skill.setCertType("invalid");
    }

    @Test
    public void testSetCertificationExpiryUpdates() {
        MedicalSkill skill =
            new MedicalSkill("counseling", "advanced",
                "counseling", futureExpiry);
        LocalDate newDate = LocalDate.now().plusMonths(6);
        skill.setCertificationExpiry(newDate);
        assertEquals(newDate, skill.getCertificationExpiry());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetCertificationExpiryNullThrows() {
        MedicalSkill skill =
            new MedicalSkill("counseling", "beginner",
                "counseling", futureExpiry);
        skill.setCertificationExpiry(null);
    }

    @Test
    public void testToStringContainsCertType() {
        MedicalSkill skill =
            new MedicalSkill("doctor", "advanced", "doctor", futureExpiry);
        assertTrue("toString should contain cert type",
            skill.toString().contains("doctor"));
    }
}