package edu.ucalgary.oop;

import java.time.LocalDate;

/**
 * Represents an inquiry logged by an {@link Inquirer} seeking information
 * about a missing {@link DisasterVictim}.
 * <p>
 * Each inquiry records who made the inquiry, the person being sought, the
 * date the inquiry was made, and any information provided by the inquirer.
 * Future inquiry dates are not permitted. Inquiries are persisted in the
 * {@code Inquiry} table; only fields present in that table are stored.
 * </p>
 *
 * @author Eric Hallett (30117108)
 * @version 1.0
 * @since 2026-03-25
 */
public class ReliefService {

    /** Database primary key; 0 until persisted. */
    private int dbId;

    /** The person who made this inquiry. */
    private Inquirer inquirer;

    /** The disaster victim being sought; may be {@code null}. */
    private DisasterVictim missingPerson;

    /** The date on which this inquiry was made. */
    private LocalDate dateOfInquiry;

    /** Information provided by the inquirer. */
    private String infoProvided;

    /**
     * Constructs a {@code ReliefService} inquiry record.
     *
     * @param inquirer      the person making the inquiry; may be {@code null}
     * @param missingPerson the victim being sought; may be {@code null}
     * @param dateOfInquiry the date the inquiry was made; must not be
     *                      {@code null} or in the future
     * @param infoProvided  information supplied by the inquirer
     * @throws IllegalArgumentException if {@code dateOfInquiry} is
     *                                  {@code null} or after today
     */
    public ReliefService(Inquirer inquirer,
                         DisasterVictim missingPerson,
                         LocalDate dateOfInquiry,
                         String infoProvided) {
        this.inquirer = inquirer;
        this.missingPerson = missingPerson;
        setDateOfInquiry(dateOfInquiry);
        this.infoProvided = infoProvided;
    }

    /**
     * Returns the database ID of this inquiry.
     *
     * @return the database primary key, or {@code 0} if not yet persisted
     */
    public int getDbId() {
        return dbId;
    }

    /**
     * Sets the database ID of this inquiry.
     *
     * @param dbId the database primary key assigned after persistence
     */
    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    /**
     * Returns the inquirer who made this inquiry.
     *
     * @return the {@link Inquirer}, or {@code null} if not set
     */
    public Inquirer getInquirer() {
        return inquirer;
    }

    /**
     * Sets the inquirer who made this inquiry.
     *
     * @param inquirer the {@link Inquirer} who made the inquiry;
     *                 may be {@code null}
     */
    public void setInquirer(Inquirer inquirer) {
        this.inquirer = inquirer;
    }

    /**
     * Returns the disaster victim being sought.
     *
     * @return the missing {@link DisasterVictim}, or {@code null} if unknown
     */
    public DisasterVictim getMissingPerson() {
        return missingPerson;
    }

    /**
     * Sets the disaster victim being sought.
     *
     * @param missingPerson the missing {@link DisasterVictim};
     *                      may be {@code null}
     */
    public void setMissingPerson(DisasterVictim missingPerson) {
        this.missingPerson = missingPerson;
    }

    /**
     * Returns the date on which this inquiry was made.
     *
     * @return the inquiry date as a {@link LocalDate}
     */
    public LocalDate getDateOfInquiry() {
        return dateOfInquiry;
    }

    /**
     * Sets the date on which this inquiry was made.
     *
     * @param dateOfInquiry the inquiry date; must not be {@code null}
     *                      or in the future
     * @throws IllegalArgumentException if {@code dateOfInquiry} is
     *                                  {@code null} or after today
     */
    public void setDateOfInquiry(LocalDate dateOfInquiry) {
        if (dateOfInquiry == null) {
            throw new IllegalArgumentException(
                "Date of inquiry cannot be null");
        }
        if (dateOfInquiry.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                "Date of inquiry cannot be in the future");
        }
        this.dateOfInquiry = dateOfInquiry;
    }

    /**
     * Returns the information provided by the inquirer.
     *
     * @return the info string
     */
    public String getInfoProvided() {
        return infoProvided;
    }

    /**
     * Sets the information provided by the inquirer.
     *
     * @param infoProvided the inquiry details text; may be {@code null}
     */
    public void setInfoProvided(String infoProvided) {
        this.infoProvided = infoProvided;
    }

    /**
     * Returns a formatted summary of this inquiry suitable for display
     * in the user interface.
     *
     * @return a string containing the inquirer name, missing person name,
     *         inquiry date, and info provided
     */
    public String getLogDetails() {
        String inqFirst = (inquirer == null)
            ? "null" : inquirer.getFirstName();
        String victimName = (missingPerson == null)
            ? "null" : missingPerson.getFirstName();
        String dateStr = dateOfInquiry.toString();
        String infoStr = (infoProvided == null) ? "null" : infoProvided;
        return "Inquirer: " + inqFirst
            + ", Missing Person: " + victimName
            + ", Date of Inquiry: " + dateStr
            + ", Info Provided: " + infoStr;
    }
}