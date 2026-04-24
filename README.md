# Disaster Relief Management System

**Course:** ENSF 380 — Object-Oriented Software Design, University of Calgary, Winter 2026  
**Author:** Eric Hallett

## Overview

The Disaster Relief Management System is a command-line Java application designed to help relief workers manage disaster victims, supplies, medical records, family relationships, inquiries, and cultural requirements at one or more shelter locations. All data is persisted in a PostgreSQL database and every user-driven change is recorded in a plain-text action log.

## Prerequisites

- Java 21
- PostgreSQL (42.7.10 or newer)
- PostgreSQL JDBC driver (included in the project or available from jdbc.postgresql.org)
- JUnit 4 and Hamcrest (for running tests)

## Setup

1. Clone the repository
2. Initialize the database by running the provided SQL file against your PostgreSQL instance:
```bash
   psql -U oop -d ensf380project -f project.sql
```
3. Copy the credentials template and configure it for your environment:
```bash
   cp src/main/resources/db.template src/main/resources/db.properties
```
   Edit `db.properties` if your PostgreSQL instance uses a non-standard host or port. The default values match the course-standard configuration and will work without modification for most setups.
4. Ensure `available_requirements.ser` is present in `src/main/resources/` before launching. See Configuration for details.

## Compiling and Running

From the project root:

```bash
javac -cp  -d bin src/main/java/edu/ucalgary/oop/*.java
java -cp bin: edu.ucalgary.oop.Main
```

Replace `<jdbc-jar>` with the path to the PostgreSQL JDBC driver jar on your system.

---

## Configuration

### 2.1 Database Credentials

Connection details are read from `db.properties` located in `src/main/resources/`. A template is provided at `src/main/resources/db.properties.template` — copy it and rename it to `db.properties` before running. The file is excluded from version control to follow standard credential management practice.

If the file is absent, the application falls back to the default values defined in the template, so no edits are necessary for a standard setup.

### 2.2 Cultural Options File

The application reads available cultural and religious accommodation options from a serialized file at startup:
src/main/resources/available_requirements.ser

This file must be present. If it cannot be found or read, the program will display an error message and exit cleanly. The file must be named `available_requirements.ser` (case-sensitive) before running.

---

## Navigating the Interface

The interface is menu driven. At every prompt, numbered options are displayed; enter the number corresponding to your choice. The program will inform you of any invalid input and prompt you to try again — it will not exit unexpectedly. To return to a previous menu, select the Back or Cancel option where available. To exit the application, select Exit from the main menu.

---

## Features

### Disaster Victims

Select **Manage Victims** from the main menu to add, view, or modify victim records. When adding a new victim, you will be prompted to supply a first name and entry date. You may then optionally provide:

- Last name and comments
- Either a date of birth OR an approximate age — not both. Once a date of birth has been recorded it cannot be replaced with an approximate age, but an approximate age can later be replaced with an exact date of birth.
- Gender (validated against census categories; age must be set first). A custom gender may be entered by selecting "Please specify" at the gender prompt.
- Current shelter location (selected from a numbered list)

### Archive and Delete Victims (Soft Delete / Hard Delete)

Two distinct removal modes are supported. Both require a confirmation step before the action is carried out.

- **Archive (soft delete)** — the victim's record is retained in the database but is hidden throughout the application. Archived victims do not appear in victim lists, skill searches, supply allocation screens, or inquiry results. This is reversible via the Restore option in the archived victims list.
- **Delete (hard delete)** — the victim and all associated data (medical records, allocated supplies, inquiries, family relationships, skills, and cultural requirements) are permanently removed from the program and the database. This action cannot be undone.

### Medical Records

Select **Manage Medical Records** from a victim's detail screen. Each record requires a treatment location (selected from a list), a description of the treatment, and the date treatment was administered. Future dates are not accepted.

### Family Relationships

Select **Manage Relationships** from a victim's detail screen. Choose two victims and provide a relationship type (e.g. spouse, sibling, parent). Existing relationships can be removed from the same screen.

### Supplies

Select **Manage Supplies** from the main menu to add supplies to a location or allocate them to a victim. When adding a new supply you will be asked whether the item is perishable. Perishable supplies require an expiry date.

- Expired supplies are excluded from the allocation list and cannot be assigned to a victim.
- When you access the supply allocation screen, the program will display a one-time warning listing all expired items currently in inventory.

### Inquiries

Select **Log Inquiry** from the main menu. You will be prompted to identify the inquirer (select an existing one or enter a new name), the victim being sought, the date of the inquiry, and any details provided. Inquiries involving archived victims are hidden from the inquiry list.

### Cultural and Religious Requirements

Requirements can be set when modifying a victim's record. Available categories and options are loaded from `available_requirements.ser` at startup. A victim may have at most one option per category. Selecting a category that already has a value will update it rather than adding a duplicate.

### Skills and Volunteer Registry

Skills can be added or removed from a victim's record via the **Manage Skills** option on the victim's detail screen. Three skill categories are supported:

- **Medical** — requires a certification type (first-aid, counseling, nursing, or doctor) and a certification expiry date.
- **Language** — requires a language name and at least one capability (read/write and/or speak/listen).
- **Trade** — type must be one of carpentry, plumbing, or electricity.

All skills also require a proficiency level: beginner, intermediate, or advanced. A victim cannot register the same skill type more than once. Skills belonging to archived victims are excluded from search results.

To search for victims by skill category, select **Search Skills by Category** from the main menu and choose medical, language, or trade.

### Locations

Select **Manage Locations** from the main menu to add a new shelter or update an existing location's name or address.

---

## Log Files

| File | Contents |
|------|----------|
| `data/action_log.txt` | Every user-driven add, update, archive, or delete. Each entry includes the action type, timestamp, and a description. |
| `data/error_log.txt` | Unrecoverable application errors (e.g. failed database connection at startup). Errors never appear in `action_log.txt`. |

The `data/` directory is created automatically if it does not exist. Both files are appended to on each run, so they accumulate a full history across sessions.

---

## Known Limitations and Notes

- The program must be able to reach the PostgreSQL instance at startup. If the connection fails, an error is written to `data/error_log.txt` and the program exits cleanly.
- `available_requirements.ser` must be present in `src/main/resources/` before launching. The program will not start without it.
- The `data/` directory must be writable from the working directory. If log files cannot be written, a warning is printed to standard output but the program continues.
- Hard deletion is permanent and cannot be undone. Use Archive instead if there is any chance the record will be needed again; a Restore feature is also implemented.
