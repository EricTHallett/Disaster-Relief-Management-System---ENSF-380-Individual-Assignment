package edu.ucalgary.oop;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link CulturalOptions}.
 */
public class CulturalOptionsTest {

    private CulturalOptions options;

    @Before
    public void setUp() {
        HashMap<String, Set<String>> map = new HashMap<>();

        Set<String> dietary = new HashSet<>();
        dietary.add("halal");
        dietary.add("kosher");
        dietary.add("vegetarian");
        map.put("dietary restrictions", dietary);

        Set<String> safeSpace = new HashSet<>();
        safeSpace.add("LGBTQIA+ affirming");
        map.put("safe-space requirements", safeSpace);

        options = new CulturalOptions(map);
    }

    @Test
    public void testGetCategoriesContainsDietaryRestrictions() {
        assertTrue("Categories should include dietary restrictions",
            options.getCategories()
                .contains("dietary restrictions"));
    }

    @Test
    public void testGetCategoriesContainsSafeSpace() {
        assertTrue("Categories should include safe-space requirements",
            options.getCategories()
                .contains("safe-space requirements"));
    }

    @Test
    public void testGetOptionsForCategoryReturnsCorrectOptions() {
        Set<String> dietaryOptions =
            options.getOptionsForCategory("dietary restrictions");
        assertTrue("halal should be a valid dietary option",
            dietaryOptions.contains("halal"));
        assertTrue("kosher should be a valid dietary option",
            dietaryOptions.contains("kosher"));
    }

    @Test
    public void testGetOptionsForCategoryUnknownReturnsNull() {
        assertNull("Unknown category should return null",
            options.getOptionsForCategory("unknown category"));
    }

    @Test
    public void testIsValidOptionTrueForKnownOption() {
        assertTrue("halal should be valid for dietary restrictions",
            options.isValidOption("dietary restrictions", "halal"));
    }

    @Test
    public void testIsValidOptionFalseForUnknownOption() {
        assertFalse("vegan should not be valid in this options set",
            options.isValidOption("dietary restrictions", "vegan"));
    }

    @Test
    public void testIsValidOptionFalseForUnknownCategory() {
        assertFalse("Unknown category should return false",
            options.isValidOption("prayer needs", "daily prayer"));
    }

    @Test
    public void testGetAccommodationsReturnsMap() {
        assertNotNull("Accommodations map should not be null",
            options.getAccommodations());
    }

    @Test
    public void testGetAccommodationsSizeMatchesInput() {
        assertEquals("Map should contain two categories",
            2, options.getAccommodations().size());
    }
}