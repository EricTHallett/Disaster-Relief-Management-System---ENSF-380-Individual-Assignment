package edu.ucalgary.oop;

import org.junit.*;
import static org.junit.Assert.*;
import java.time.LocalDate;

/**
 * Unit tests for {@link ReliefService}.
 */
public class ReliefServiceTest {

    private Inquirer inquirer;
    private DisasterVictim missingPerson;
    private LocalDate validDate;

    @Before
    public void setUp() {
        inquirer = new Inquirer("John", "Doe", "555-0101", null);
        missingPerson =
            new DisasterVictim("Jane", LocalDate.of(2025, 1, 1));
        validDate = LocalDate.of(2025, 1, 15);
    }

    @Test
    public void testConstructorSetsInquirer() {
        ReliefService rs =
            new ReliefService(inquirer, missingPerson,
                validDate, "Seeking family");
        assertEquals("Inquirer should be set",
            inquirer, rs.getInquirer());
    }

    @Test
    public void testConstructorSetsMissingPerson() {
        ReliefService rs =
            new ReliefService(inquirer, missingPerson,
                validDate, "Seeking family");
        assertEquals("Missing person should be set",
            missingPerson, rs.getMissingPerson());
    }

    @Test
    public void testConstructorSetsDateOfInquiry() {
        ReliefService rs =
            new ReliefService(inquirer, missingPerson,
                validDate, "Seeking family");
        assertEquals("Date of inquiry should be set",
            validDate, rs.getDateOfInquiry());
    }

    @Test
    public void testConstructorSetsInfoProvided() {
        ReliefService rs =
            new ReliefService(inquirer, missingPerson,
                validDate, "Seeking family");
        assertEquals("Info provided should be set",
            "Seeking family", rs.getInfoProvided());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullDateThrows() {
        new ReliefService(inquirer, missingPerson, null, "details");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorFutureDateThrows() {
        new ReliefService(inquirer, missingPerson,
            LocalDate.now().plusDays(1), "details");
    }

    @Test
    public void testSetDateOfInquiryTodaySucceeds() {
        ReliefService rs =
            new ReliefService(inquirer, missingPerson,
                validDate, "details");
        LocalDate today = LocalDate.now();
        rs.setDateOfInquiry(today);
        assertEquals("Today should be a valid inquiry date",
            today, rs.getDateOfInquiry());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDateOfInquiryFutureThrows() {
        ReliefService rs =
            new ReliefService(inquirer, missingPerson,
                validDate, "details");
        rs.setDateOfInquiry(LocalDate.now().plusDays(1));
    }

    @Test
    public void testNullMissingPersonPermitted() {
        ReliefService rs =
            new ReliefService(inquirer, null, validDate, "details");
        assertNull("Null missing person should be accepted",
            rs.getMissingPerson());
    }

    @Test
    public void testGetLogDetailsContainsInquirerName() {
        ReliefService rs =
            new ReliefService(inquirer, missingPerson,
                validDate, "info");
        assertTrue("Log details should contain inquirer first name",
            rs.getLogDetails().contains("John"));
    }

    @Test
    public void testGetLogDetailsContainsMissingPersonName() {
        ReliefService rs =
            new ReliefService(inquirer, missingPerson,
                validDate, "info");
        assertTrue("Log details should contain missing person name",
            rs.getLogDetails().contains("Jane"));
    }
}