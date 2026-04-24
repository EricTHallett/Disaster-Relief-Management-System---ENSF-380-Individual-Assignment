package edu.ucalgary.oop;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link TradeSkill}.
 */
public class TradeSkillTest {

    @Test
    public void testConstructorSetsCategoryToTrade() {
        TradeSkill skill = new TradeSkill("carpentry", "beginner");
        assertEquals("trade", skill.getCategory());
    }

    @Test
    public void testConstructorSetsSkillName() {
        TradeSkill skill = new TradeSkill("carpentry", "beginner");
        assertEquals("carpentry", skill.getSkillName());
    }

    @Test
    public void testConstructorNormalizesNameToLowercase() {
        TradeSkill skill = new TradeSkill("PLUMBING", "advanced");
        assertEquals("plumbing", skill.getSkillName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidTradeTypeThrows() {
        new TradeSkill("welding", "beginner");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullTradeTypeThrows() {
        new TradeSkill(null, "beginner");
    }

    @Test
    public void testValidateCarpentrySucceeds() {
        TradeSkill.validateTradeType("carpentry");
    }

    @Test
    public void testValidatePlumbingSucceeds() {
        TradeSkill.validateTradeType("plumbing");
    }

    @Test
    public void testValidateElectricitySucceeds() {
        TradeSkill.validateTradeType("electricity");
    }

    @Test
    public void testValidateTradeTypeCaseInsensitive() {
        TradeSkill.validateTradeType("ELECTRICITY");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateTradeTypeInvalidThrows() {
        TradeSkill.validateTradeType("masonry");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateTradeTypeNullThrows() {
        TradeSkill.validateTradeType(null);
    }

    @Test
    public void testProficiencyLevelSet() {
        TradeSkill skill = new TradeSkill("electricity", "advanced");
        assertEquals("advanced", skill.getProficiencyLevel());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidProficiencyLevelThrows() {
        new TradeSkill("carpentry", "master");
    }

    @Test
    public void testToStringContainsTradeType() {
        TradeSkill skill = new TradeSkill("plumbing", "intermediate");
        assertTrue("toString should contain trade type",
            skill.toString().contains("plumbing"));
    }

    @Test
    public void testToStringContainsProficiency() {
        TradeSkill skill = new TradeSkill("plumbing", "intermediate");
        assertTrue("toString should contain proficiency level",
            skill.toString().contains("intermediate"));
    }
}