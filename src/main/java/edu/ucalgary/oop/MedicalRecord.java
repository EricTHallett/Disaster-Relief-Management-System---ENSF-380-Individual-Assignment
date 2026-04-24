package edu.ucalgary.oop;

import java.time.LocalDate;

/**
 * Represents a medical treatment record for a {@link DisasterVictim}.
 * <p>
 * Each record captures where treatment occurred, a description of the
 * treatment, and the date it was administered. Future treatment dates are
 * not permitted, as records must reflect events that have already occurred.
 * </p>
 *
 * @author Eric Hallett (30117108)
 * @version 1.0
 * @since 2026-03-25
 */
public class MedicalRecord {

    /** Database primary key; 0 until persisted. */
    private int dbId;

    /** The location where treatment was administered. */
    private Location location;

    /** A description of the treatment provided. */
    private String treatmentDetails;

    /** The date on which treatment was administered. */
    private LocalDate dateOfTreatment;

    /**
     * Constructs a {@code MedicalRecord} with the given location, details,
     * and treatment date.
     *
     * @param location         the location where treatment occurred;
     *                         must not be {@code null}
     * @param treatmentDetails a description of the treatment provided
     * @param dateOfTreatment  the date treatment was administered;
     *                         must not be {@code null} or in the future
     * @throws IllegalArgumentException if {@code location} or
     *                                  {@code dateOfTreatment} is {@code null},
     *                                  or if {@code dateOfTreatment} is after
     *                                  today
     */
    public MedicalRecord(Location location,
                         String treatmentDetails,
                         LocalDate dateOfTreatment) {
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }
        if (dateOfTreatment == null) {
            throw new IllegalArgumentException(
                "Date of treatment cannot be null");
        }
        if (dateOfTreatment.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                "Date of treatment cannot be in the future");
        }
        this.location = location;
        this.treatmentDetails = treatmentDetails;
        this.dateOfTreatment = dateOfTreatment;
    }

    /**
     * Returns the database ID of this record.
     *
     * @return the database primary key, or {@code 0} if not yet persisted
     */
    public int getDbId() {
        return dbId;
    }

    /**
     * Sets the database ID of this record.
     *
     * @param dbId the database primary key assigned after persistence
     */
    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    /**
     * Returns the location where treatment was administered.
     *
     * @return the treatment {@link Location}
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Sets the location where treatment was administered.
     *
     * @param location the new treatment location; must not be {@code null}
     * @throws IllegalArgumentException if {@code location} is {@code null}
     */
    public void setLocation(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }
        this.location = location;
    }

    /**
     * Returns the description of the treatment provided.
     *
     * @return the treatment details string
     */
    public String getTreatmentDetails() {
        return treatmentDetails;
    }

    /**
     * Sets the description of the treatment provided.
     *
     * @param treatmentDetails the new treatment description
     */
    public void setTreatmentDetails(String treatmentDetails) {
        this.treatmentDetails = treatmentDetails;
    }

    /**
     * Returns the date on which treatment was administered.
     *
     * @return the treatment date as a {@link LocalDate}
     */
    public LocalDate getDateOfTreatment() {
        return dateOfTreatment;
    }

    /**
     * Sets the date on which treatment was administered.
     *
     * @param dateOfTreatment the new treatment date; must not be {@code null}
     *                        or in the future
     * @throws IllegalArgumentException if {@code dateOfTreatment} is
     *                                  {@code null} or after today
     */
    public void setDateOfTreatment(LocalDate dateOfTreatment) {
        if (dateOfTreatment == null) {
            throw new IllegalArgumentException(
                "Date of treatment cannot be null");
        }
        if (dateOfTreatment.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                "Date of treatment cannot be in the future");
        }
        this.dateOfTreatment = dateOfTreatment;
    }
}