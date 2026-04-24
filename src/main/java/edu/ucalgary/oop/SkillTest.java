package edu.ucalgary.oop;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link Skill} via the concrete {@link TradeSkill} subclass,
 * which exercises all base-class behaviour without introducing
 * category-specific concerns.
 */
public class SkillTest {

    @Test
    public void testValidProficiencyLevelBeginner() {
        
        Skill.validateProficiencyLevel("beginner");
    }

    @Test
    public void testValidProficiencyLevelIntermediate() {
        Skill.validateProficiencyLevel("intermediate");
    }

    @Test
    public void testValidProficiencyLevelAdvanced() {
        Skill.validateProficiencyLevel("advanced");
    }

    @Test
    public void testValidProficiencyLevelCaseInsensitive() {
        
        Skill.validateProficiencyLevel("BEGINNER");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidProficiencyLevelNullThrows() {
        Skill.validateProficiencyLevel(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidProficiencyLevelInvalidThrows() {
        Skill.validateProficiencyLevel("expert");
    }

    @Test
    public void testGetSkillNameReturnsConstructorValue() {
        Skill skill = new TradeSkill("carpentry", "beginner");
        assertEquals("carpentry", skill.getSkillName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSkillNameNullThrows() {
        Skill skill = new TradeSkill("carpentry", "beginner");
        skill.setSkillName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSkillNameBlankThrows() {
        Skill skill = new TradeSkill("carpentry", "beginner");
        skill.setSkillName("   ");
    }

    @Test
    public void testSetSkillNameUpdates() {
        Skill skill = new TradeSkill("carpentry", "beginner");
        skill.setSkillName("plumbing");
        assertEquals("Skill name should be updated",
            "plumbing", skill.getSkillName());
    }

    @Test
    public void testGetCategoryReturnsTrade() {
        Skill skill = new TradeSkill("carpentry", "beginner");
        assertEquals("trade", skill.getCategory());
    }

    @Test
    public void testGetProficiencyLevelReturnsConstructorValue() {
        Skill skill = new TradeSkill("carpentry", "advanced");
        assertEquals("advanced", skill.getProficiencyLevel());
    }

    @Test
    public void testSetProficiencyLevelUpdates() {
        Skill skill = new TradeSkill("carpentry", "beginner");
        skill.setProficiencyLevel("advanced");
        assertEquals("advanced", skill.getProficiencyLevel());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProficiencyLevelInvalidThrows() {
        Skill skill = new TradeSkill("carpentry", "beginner");
        skill.setProficiencyLevel("master");
    }

    @Test
    public void testDbIdDefaultIsZero() {
        Skill skill = new TradeSkill("carpentry", "beginner");
        assertEquals(0, skill.getDbId());
    }

    @Test
    public void testSetDbIdUpdates() {
        Skill skill = new TradeSkill("carpentry", "beginner");
        skill.setDbId(7);
        assertEquals(7, skill.getDbId());
    }

    @Test
    public void testVictimSkillDbIdDefaultIsZero() {
        Skill skill = new TradeSkill("carpentry", "beginner");
        assertEquals(0, skill.getVictimSkillDbId());
    }

    @Test
    public void testSetVictimSkillDbIdUpdates() {
        Skill skill = new TradeSkill("carpentry", "beginner");
        skill.setVictimSkillDbId(42);
        assertEquals(42, skill.getVictimSkillDbId());
    }

    @Test
    public void testToStringContainsCategory() {
        Skill skill = new TradeSkill("carpentry", "beginner");
        assertTrue("toString should contain category",
            skill.toString().contains("trade"));
    }

    @Test
    public void testToStringContainsSkillName() {
        Skill skill = new TradeSkill("carpentry", "beginner");
        assertTrue("toString should contain skill name",
            skill.toString().contains("carpentry"));
    }
}