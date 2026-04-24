package edu.ucalgary.oop;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link Inquirer}.
 */
public class InquirerTest {

    private Inquirer inquirer;

    @Before
    public void setUp() {
        inquirer =
            new Inquirer("Jane", "Smith", "555-0100", "Seeking family");
    }

    @Test
    public void testConstructorSetsFirstName() {
        assertEquals("First name should be set",
            "Jane", inquirer.getFirstName());
    }

    @Test
    public void testConstructorSetsLastName() {
        assertEquals("Last name should be set",
            "Smith", inquirer.getLastName());
    }

    @Test
    public void testConstructorSetsPhoneNumber() {
        assertEquals("Phone number should be set",
            "555-0100", inquirer.getServicesPhoneNum());
    }

    @Test
    public void testConstructorSetsInfo() {
        assertEquals("Info should be set",
            "Seeking family", inquirer.getInfo());
    }

    @Test
    public void testNullLastNamePermitted() {
        Inquirer i = new Inquirer("Jane", null, null, null);
        assertNull("Null last name should be accepted", i.getLastName());
    }

    @Test
    public void testToStringWithLastName() {
        assertEquals("toString should include full name",
            "Jane Smith", inquirer.toString());
    }

    @Test
    public void testToStringWithoutLastName() {
        Inquirer i = new Inquirer("Jane", null, null, null);
        assertEquals("toString without last name should be first name only",
            "Jane", i.toString());
    }

    @Test
    public void testDbIdDefaultIsZero() {
        assertEquals("Default DB id should be 0", 0, inquirer.getDbId());
    }

    @Test
    public void testSetDbIdUpdates() {
        inquirer.setDbId(99);
        assertEquals(99, inquirer.getDbId());
    }
}