package edu.ucalgary.oop;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link LanguageSkill}.
 */
public class LanguageSkillTest {

    private Set<String> bothCapabilities;
    private Set<String> readWriteOnly;

    @Before
    public void setUp() {
        bothCapabilities = new HashSet<>();
        bothCapabilities.add("read/write");
        bothCapabilities.add("speak/listen");

        readWriteOnly = new HashSet<>();
        readWriteOnly.add("read/write");
    }

    @Test
    public void testConstructorSetsSkillName() {
        LanguageSkill skill =
            new LanguageSkill("French", "intermediate", readWriteOnly);
        assertEquals("French", skill.getSkillName());
    }

    @Test
    public void testConstructorSetsCategoryToLanguage() {
        LanguageSkill skill =
            new LanguageSkill("French", "beginner", readWriteOnly);
        assertEquals("language", skill.getCategory());
    }

    @Test
    public void testConstructorSetsCapabilities() {
        LanguageSkill skill =
            new LanguageSkill("French", "beginner", readWriteOnly);
        assertTrue("read/write should be present",
            skill.getCapabilities().contains("read/write"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullCapabilitiesThrows() {
        new LanguageSkill("French", "beginner", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyCapabilitiesThrows() {
        new LanguageSkill("French", "beginner", new HashSet<>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidCapabilityThrows() {
        Set<String> bad = new HashSet<>();
        bad.add("write only");
        new LanguageSkill("French", "beginner", bad);
    }

    @Test
    public void testValidateCapabilityReadWriteSucceeds() {
        LanguageSkill.validateCapability("read/write");
    }

    @Test
    public void testValidateCapabilitySpeakListenSucceeds() {
        LanguageSkill.validateCapability("speak/listen");
    }

    @Test
    public void testValidateCapabilityCaseInsensitive() {
        LanguageSkill.validateCapability("READ/WRITE");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateCapabilityNullThrows() {
        LanguageSkill.validateCapability(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateCapabilityInvalidThrows() {
        LanguageSkill.validateCapability("sign language");
    }

    @Test
    public void testAddCapabilityIncreasesSet() {
        LanguageSkill skill =
            new LanguageSkill("French", "beginner", readWriteOnly);
        skill.addCapability("speak/listen");
        assertEquals("Both capabilities should be present",
            2, skill.getCapabilities().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddCapabilityInvalidThrows() {
        LanguageSkill skill =
            new LanguageSkill("French", "beginner", readWriteOnly);
        skill.addCapability("draw/paint");
    }

    @Test
    public void testRemoveCapabilityDecreasesSet() {
        LanguageSkill skill =
            new LanguageSkill("French", "intermediate", bothCapabilities);
        skill.removeCapability("speak/listen");
        assertFalse("speak/listen should be removed",
            skill.getCapabilities().contains("speak/listen"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveAbsentCapabilityThrows() {
        LanguageSkill skill =
            new LanguageSkill("French", "beginner", readWriteOnly);
        skill.removeCapability("speak/listen");
    }

    @Test
    public void testGetCapabilitiesAsStringNotEmpty() {
        LanguageSkill skill =
            new LanguageSkill("French", "beginner", readWriteOnly);
        assertFalse("Capabilities string should not be empty",
            skill.getCapabilitiesAsString().isEmpty());
    }

    @Test
    public void testGetCapabilitiesReturnsCopy() {
        LanguageSkill skill =
            new LanguageSkill("French", "beginner", readWriteOnly);
        Set<String> copy = skill.getCapabilities();
        copy.clear();
        assertEquals("Modifying returned set should not affect skill",
            1, skill.getCapabilities().size());
    }

    @Test
    public void testToStringContainsLanguageName() {
        LanguageSkill skill =
            new LanguageSkill("Spanish", "advanced", bothCapabilities);
        assertTrue("toString should contain language name",
            skill.toString().contains("Spanish"));
    }
}