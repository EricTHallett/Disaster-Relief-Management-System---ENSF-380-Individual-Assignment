package edu.ucalgary.oop;

/**
 * Represents a person who contacts relief services to inquire about a missing
 * individual.
 * <p>
 * An inquirer is stored in the {@code Person} table in the database and is
 * distinguished from {@link DisasterVictim} records by the absence of a
 * corresponding {@code DisasterVictim} row. All fields except
 * {@code firstName} are optional and may be {@code null}.
 * </p>
 *
 * @author Eric Hallett (30117108)
 * @version 1.0
 * @since 2026-03-29
 */
public class Inquirer {

    /** Database primary key; 0 until persisted. */
    private int dbId;

    /** The inquirer's first name. */
    private final String firstName;

    /** The inquirer's last name; may be {@code null}. */
    private final String lastName;

    /** The inquirer's contact phone number; may be {@code null}. */
    private final String servicesPhoneNum;

    /** Additional information provided by or about
     *  the inquirer; may be {@code null}. */
    private final String info;

    /**
     * Constructs an {@code Inquirer} with the given personal details.
     *
     * @param firstName        the inquirer's first name
     * @param lastName         the inquirer's last name; may be {@code null}
     * @param servicesPhoneNum the inquirer's contact phone number;
     *                         may be {@code null}
     * @param info             additional notes about the inquirer;
     *                         may be {@code null}
     */
    public Inquirer(String firstName, String lastName,
                    String servicesPhoneNum, String info) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.servicesPhoneNum = servicesPhoneNum;
        this.info = info;
    }

    /**
     * Returns the database ID of this inquirer.
     *
     * @return the database primary key, or {@code 0} if not yet persisted
     */
    public int getDbId() {
        return dbId;
    }

    /**
     * Sets the database ID of this inquirer.
     *
     * @param dbId the database primary key assigned after persistence
     */
    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    /**
     * Returns the inquirer's first name.
     *
     * @return the first name string
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the inquirer's last name.
     *
     * @return the last name string, or {@code null} if not provided
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Returns the inquirer's contact phone number.
     *
     * @return the phone number string, or {@code null} if not provided
     */
    public String getServicesPhoneNum() {
        return servicesPhoneNum;
    }

    /**
     * Returns additional information about the inquirer.
     *
     * @return the info string, or {@code null} if not provided
     */
    public String getInfo() {
        return info;
    }

    /**
     * Returns the inquirer's full name. If no last name is set, only the
     * first name is returned.
     *
     * @return a string in the form {@code "firstName lastName"} or just
     *         {@code "firstName"}
     */
    @Override
    public String toString() {
        return firstName + (lastName != null ? " " + lastName : "");
    }
}