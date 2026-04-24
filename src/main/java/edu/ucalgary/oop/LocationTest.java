package edu.ucalgary.oop;

import org.junit.*;
import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Unit tests for {@link Location}.
 */
public class LocationTest {

    private Location location;
    private DisasterVictim activeVictim;
    private DisasterVictim softDeletedVictim;

    @Before
    public void setUp() {
        location = new Location("Shelter A", "100 Main St");

        activeVictim =
            new DisasterVictim("Alice", LocalDate.of(2025, 1, 1));
        activeVictim.setDbId(1);

        softDeletedVictim =
            new DisasterVictim("Deleted", LocalDate.of(2025, 1, 2));
        softDeletedVictim.setDbId(2);
        softDeletedVictim.setSoftDeleted(true);
    }

    @Test
    public void testConstructorSetsName() {
        assertEquals("Name should be set by constructor",
            "Shelter A", location.getName());
    }

    @Test
    public void testConstructorSetsAddress() {
        assertEquals("Address should be set by constructor",
            "100 Main St", location.getAddress());
    }

    @Test
    public void testGetOccupantsExcludesSoftDeleted() {
        location.addOccupant(activeVictim);
        location.addOccupant(softDeletedVictim);

        List<DisasterVictim> visible = location.getOccupants();
        assertEquals("Only active victims should be visible", 1,
            visible.size());
        assertTrue("Active victim should be present",
            visible.contains(activeVictim));
    }

    @Test
    public void testGetAllOccupantsIncludesSoftDeleted() {
        location.addOccupant(activeVictim);
        location.addOccupant(softDeletedVictim);

        List<DisasterVictim> all = location.getAllOccupants();
        assertEquals("All occupants including soft-deleted should appear",
            2, all.size());
    }

    @Test
    public void testAddOccupantNullIsIgnored() {
        location.addOccupant(null);
        assertTrue("Null occupant should not be added",
            location.getAllOccupants().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveAbsentOccupantThrows() {
        location.removeOccupant(activeVictim);
    }

    @Test
    public void testRemoveOccupantSucceeds() {
        location.addOccupant(activeVictim);
        location.removeOccupant(activeVictim);
        assertTrue("Occupant list should be empty after removal",
            location.getAllOccupants().isEmpty());
    }

    @Test
    public void testAddSupply() {
        Supply supply = new Supply("blanket", 1);
        location.addSupply(supply);
        assertEquals("Location should hold one supply",
            1, location.getSupplies().size());
    }

    @Test
    public void testAddSupplyNullIsIgnored() {
        location.addSupply(null);
        assertTrue("Null supply should not be added",
            location.getSupplies().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveAbsentSupplyThrows() {
        location.removeSupply(new Supply("blanket", 1));
    }

    @Test
    public void testRemoveSupplySucceeds() {
        Supply supply = new Supply("blanket", 1);
        location.addSupply(supply);
        location.removeSupply(supply);
        assertTrue("Supply list should be empty after removal",
            location.getSupplies().isEmpty());
    }

    @Test
    public void testGetSuppliesReturnsCopy() {
        Supply supply = new Supply("blanket", 1);
        location.addSupply(supply);
        List<Supply> copy = location.getSupplies();
        copy.clear();
        assertEquals("Modifying returned list should not affect location",
            1, location.getSupplies().size());
    }

    @Test
    public void testToStringContainsName() {
        assertTrue("toString should contain location name",
            location.toString().contains("Shelter A"));
    }
}