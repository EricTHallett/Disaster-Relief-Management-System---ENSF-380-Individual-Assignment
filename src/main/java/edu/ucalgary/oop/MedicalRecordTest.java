package edu.ucalgary.oop;

import org.junit.*;
import static org.junit.Assert.*;
import java.time.LocalDate;

/**
 * Unit tests for {@link MedicalRecord}.
 */
public class MedicalRecordTest {

    private Location location;
    private LocalDate validDate;

    @Before
    public void setUp() {
        location = new Location("Clinic", "1 Health St");
        validDate = LocalDate.of(2025, 1, 10);
    }

    @Test
    public void testConstructorSetsLocation() {
        MedicalRecord rec =
            new MedicalRecord(location, "Treated cut", validDate);
        assertEquals("Location should be set by constructor",
            location, rec.getLocation());
    }

    @Test
    public void testConstructorSetsTreatmentDetails() {
        MedicalRecord rec =
            new MedicalRecord(location, "Treated cut", validDate);
        assertEquals("Treatment details should be set by constructor",
            "Treated cut", rec.getTreatmentDetails());
    }

    @Test
    public void testConstructorSetsDateOfTreatment() {
        MedicalRecord rec =
            new MedicalRecord(location, "Treated cut", validDate);
        assertEquals("Date of treatment should be set by constructor",
            validDate, rec.getDateOfTreatment());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullLocationThrows() {
        new MedicalRecord(null, "Treated cut", validDate);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullDateThrows() {
        new MedicalRecord(location, "Treated cut", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorFutureDateThrows() {
        new MedicalRecord(location, "Future treatment",
            LocalDate.now().plusDays(1));
    }

    @Test
    public void testSetDateOfTreatmentTodaySucceeds() {
        MedicalRecord rec =
            new MedicalRecord(location, "details", validDate);
        LocalDate today = LocalDate.now();
        rec.setDateOfTreatment(today);
        assertEquals("Setting date to today should succeed",
            today, rec.getDateOfTreatment());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDateOfTreatmentFutureThrows() {
        MedicalRecord rec =
            new MedicalRecord(location, "details", validDate);
        rec.setDateOfTreatment(LocalDate.now().plusDays(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetLocationNullThrows() {
        MedicalRecord rec =
            new MedicalRecord(location, "details", validDate);
        rec.setLocation(null);
    }

    @Test
    public void testSetTreatmentDetailsUpdates() {
        MedicalRecord rec =
            new MedicalRecord(location, "original", validDate);
        rec.setTreatmentDetails("updated");
        assertEquals("updated", rec.getTreatmentDetails());
    }
}