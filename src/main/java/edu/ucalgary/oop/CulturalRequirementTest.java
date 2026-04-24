package edu.ucalgary.oop;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link CulturalRequirement}.
 */
public class CulturalRequirementTest {

    @Test
    public void testConstructorSetsCategory() {
        CulturalRequirement req =
            new CulturalRequirement("dietary restrictions", "halal");
        assertEquals("Category should be set by constructor",
            "dietary restrictions", req.getCategory());
    }

    @Test
    public void testConstructorSetsOption() {
        CulturalRequirement req =
            new CulturalRequirement("dietary restrictions", "halal");
        assertEquals("Option should be set by constructor",
            "halal", req.getOption());
    }

    @Test
    public void testConstructorTrimsCategoryWhitespace() {
        CulturalRequirement req =
            new CulturalRequirement("  dietary restrictions  ", "halal");
        assertEquals("Category should be trimmed",
            "dietary restrictions", req.getCategory());
    }

    @Test
    public void testConstructorTrimsOptionWhitespace() {
        CulturalRequirement req =
            new CulturalRequirement("dietary restrictions", "  halal  ");
        assertEquals("Option should be trimmed", "halal", req.getOption());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullCategoryThrows() {
        new CulturalRequirement(null, "halal");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorBlankCategoryThrows() {
        new CulturalRequirement("   ", "halal");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullOptionThrows() {
        new CulturalRequirement("dietary restrictions", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorBlankOptionThrows() {
        new CulturalRequirement("dietary restrictions", "");
    }

    @Test
    public void testSetOptionUpdates() {
        CulturalRequirement req =
            new CulturalRequirement("dietary restrictions", "halal");
        req.setOption("kosher");
        assertEquals("Option should be updated", "kosher", req.getOption());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetOptionNullThrows() {
        CulturalRequirement req =
            new CulturalRequirement("dietary restrictions", "halal");
        req.setOption(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetCategoryNullThrows() {
        CulturalRequirement req =
            new CulturalRequirement("dietary restrictions", "halal");
        req.setCategory(null);
    }

    @Test
    public void testToStringFormat() {
        CulturalRequirement req =
            new CulturalRequirement("dietary restrictions", "halal");
        assertEquals("dietary restrictions: halal", req.toString());
    }

    @Test
    public void testDbIdDefaultIsZero() {
        CulturalRequirement req =
            new CulturalRequirement("dietary restrictions", "halal");
        assertEquals(0, req.getDbId());
    }
}