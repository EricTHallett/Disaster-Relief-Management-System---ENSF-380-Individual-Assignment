package edu.ucalgary.oop;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Command-line interface for the Disaster Relief Management System.
 * Handles all user interaction and delegates logic to
 * {@link DisasterReliefController}. Contains no business logic.
 *
 * @author Eric Hallett (30117108)
 * @version 1.0
 * @since 2026-03-23
 */
public class CLI {

    private final DisasterReliefController controller;
    private final Scanner scanner;
    private boolean expiredWarningShown = false;

    private static final String DIV =
        "=====================================================================";
    private static final String SUB =
        "---------------------------------------------------------------------";

    /**
     * Constructs a CLI with the given controller.
     *
     * @param controller the application controller
     */
    public CLI(DisasterReliefController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Starts the main application loop. Unrecoverable runtime errors are
     * caught here, logged to {@code data/error_log.txt}, and reported to the
     * user before the program exits cleanly.
     */
    public void start() {
        try {
            System.out.println("\nENSF 380 IA2 - Developed by: Eric Hallett (30117108)");
            System.out.println("Disaster Relief Management System");
            System.out.println(DIV);
            warnExpiredSupplies();
            boolean running = true;
            while (running) {
                printMainMenu();
                int choice = promptInt("Choice", 0, 6);
                switch (choice) {
                    case 1 -> manageVictims();
                    case 2 -> manageSupplies();
                    case 3 -> manageInquiries();
                    case 4 -> manageLocations();
                    case 5 -> searchSkills();
                    case 6 -> viewLogs();
                    case 0 -> running = false;
                }
            }
            System.out.println("\nExiting Application. Goodbye!\n");
        } catch (Exception e) {
            System.err.println(
                "\nAn unrecoverable error occurred. Exiting application.");
            System.err.println(e.getMessage());
            ActionLogger.getInstance().logError(
                "Unrecoverable runtime error", e);
        }
    }

    private void printMainMenu() {
        System.out.println("\nMain Menu");
        System.out.println(DIV);
        System.out.println("  1. Manage Disaster Victims");
        System.out.println("  2. Manage Supplies");
        System.out.println("  3. Manage Inquiries");
        System.out.println("  4. View Locations");
        System.out.println("  5. Search Volunteers by Skill");
        System.out.println("  6. View Action Log");
        System.out.println("  0. Exit");
    }

    private void manageVictims() {
        boolean back = false;
        while (!back) {
            System.out.println("\nDisaster Victims");
            System.out.println(DIV);
            printVictimList();
            System.out.println("\n" + SUB);
            System.out.println("Select Victim");
            System.out.println("  1. Add Victim");
            System.out.println("  2. View / Edit Victim");
            System.out.println("  3. Archive or Delete Victim");
            System.out.println("  4. Manage Archived Victims");
            System.out.println("  0. Back");
            int c = promptInt("Choice", 0, 4);
            switch (c) {
                case 1 -> addVictim();
                case 2 -> selectAndViewVictim();
                case 3 -> deleteVictim();
                case 4 -> manageSoftDeleted();
                case 0 -> back = true;
            }
        }
    }

    private void printVictimList() {
        List<DisasterVictim> victims = controller.getActiveVictims();
        if (victims.isEmpty()) {
            System.out.println("  No active victims on record.");
            return;
        }
        System.out.printf(
            "  %-4s %-35s %-17s %s%n",
            "ID.", "Name", "Age / DOB", "Location");
        System.out.println("  " + SUB);
        for (int i = 0; i < victims.size(); i++) {
            DisasterVictim v = victims.get(i);
            String age = v.getDateOfBirth() != null
                ? "DOB: " + v.getDateOfBirth()
                : "Age~" + (v.getApproximateAge() != null
                    ? v.getApproximateAge() : "?");
            String loc = v.getLocation() != null
                ? v.getLocation().getName() : "Unassigned";
            System.out.printf(
                "  %-4d %-35s %-17s %s%n",
                i + 1, v, age, loc);
        }
    }

    private void selectAndViewVictim() {
        DisasterVictim v = pickVictim("Select victim");
        if (v == null) {
            return;
        }
        viewProfile(v);
    }

    private void viewProfile(DisasterVictim v) {
        boolean back = false;
        while (!back) {
            printProfile(v);
            System.out.println("\n" + SUB);
            System.out.println("  1. Edit Victim");
            System.out.println("  0. Back");
            int c = promptInt("Choice", 0, 1);
            if (c == 1) {
                editVictim(v);
            } else {
                back = true;
            }
        }
    }

    private void printProfile(DisasterVictim v) {
        System.out.println("\n  Victim Profile - " + v);
        System.out.println(DIV);

        String dob = v.getDateOfBirth() != null
            ? v.getDateOfBirth().toString() : "(not set)";
        String age = v.getApproximateAge() != null
            ? v.getApproximateAge().toString() : "(not set)";
        String gender = v.getGender() != null
            ? v.getGender() : "(not set)";
        String loc = v.getLocation() != null
            ? v.getLocation().getName() : "(unassigned)";
        String comments = v.getComments() != null
            ? v.getComments() : "(none)";

        System.out.println("  ID           : " + v.getDbId());
        System.out.println("  First Name   : " + v.getFirstName());
        System.out.println("  Last Name    : "
            + (v.getLastName() != null ? v.getLastName() : "(none)"));
        System.out.println("  Gender       : " + gender);
        System.out.println("  Date of Birth: " + dob);
        System.out.println("  Approx. Age  : " + age);
        System.out.println("  Entry Date   : " + v.getEntryDate());
        System.out.println("  Location     : " + loc);
        System.out.println("  Comments     : " + comments);

        System.out.println("\n" + SUB);
        System.out.println("  Medical Records:");
        List<MedicalRecord> records = v.getMedicalRecords();
        if (records.isEmpty()) {
            System.out.println("    (none)");
        } else for (MedicalRecord r : records) {
            System.out.println("    - " + r.getDateOfTreatment()
                + " at " + r.getLocation().getName()
                + ": " + r.getTreatmentDetails());
        }

        System.out.println("\n  Family Connections:");
        List<FamilyRelation> relations = v.getFamilyConnections();
        if (relations.isEmpty()) {
            System.out.println("    (none)");
        } else for (FamilyRelation r : relations) {
            DisasterVictim other =
                r.getPersonOne().equals(v)
                    ? r.getPersonTwo() : r.getPersonOne();
            System.out.println("    - " + r.getRelationshipTo()
                + " of " + other);
        }

        System.out.println("\n  Personal Belongings:");
        List<Supply> belongings = v.getPersonalBelongings();
        if (belongings.isEmpty()) {
            System.out.println("    (none)");
        } else for (Supply s : belongings) {
            System.out.println("    - " + s);
        }

        System.out.println("\n  Cultural Requirements:");
        List<CulturalRequirement> reqs = v.getCulturalRequirements();
        if (reqs.isEmpty()) {
            System.out.println("    (none)");
        } else for (CulturalRequirement r : reqs) {
            System.out.println("    - " + r);
        }

        System.out.println("\n  Skills:");
        List<Skill> skills = v.getSkills();
        if (skills.isEmpty()) {
            System.out.println("    (none)");
        } else for (Skill s : skills) {
            System.out.println("    - " + s);
        }
    }

    private void editVictim(DisasterVictim v) {
        boolean back = false;
        while (!back) {
            System.out.println("\n  Editing: " + v);
            System.out.println(DIV);
            String nameVal = v.getFirstName()
                + (v.getLastName() != null ? " " + v.getLastName() : "");
            String genderVal = v.getGender() != null
                ? v.getGender() : "(not set)";
            String ageVal = v.getDateOfBirth() != null
                ? "DOB: " + v.getDateOfBirth()
                : v.getApproximateAge() != null
                    ? "Age~: " + v.getApproximateAge() : "(not set)";
            String locVal = v.getLocation() != null
                ? v.getLocation().getName() : "(unassigned)";
            String commentsVal = v.getComments() != null
                ? v.getComments() : "(none)";
            System.out.println("  1.  Name               [" + nameVal + "]");
            System.out.println("  2.  Gender             [" + genderVal + "]");
            System.out.println("  3.  Age / DOB          [" + ageVal + "]");
            System.out.println(
                "  4.  Comments           [" + commentsVal + "]");
            System.out.println("  5.  Location           [" + locVal + "]");
            System.out.println("  6.  Medical Records");
            System.out.println("  7.  Family Relationships");
            System.out.println("  8.  Allocate Supply");
            System.out.println("  9.  Cultural Requirements");
            System.out.println("  10. Skills");
            System.out.println("  0.  Back");
            int c = promptInt("Choice", 0, 10);
            switch (c) {
                case 1  -> editName(v);
                case 2  -> editGender(v);
                case 3  -> editAge(v);
                case 4  -> editComments(v);
                case 5  -> editLocation(v);
                case 6  -> manageMedicalRecords(v);
                case 7  -> manageFamilyRelations(v);
                case 8  -> allocateSupply(v);
                case 9  -> setCulturalRequirementsInteractive(v);
                case 10 -> manageSkills(v);
                case 0  -> back = true;
            }
        }
    }

    private void addVictim() {
        System.out.println("\nAdd Victim");
        System.out.println(DIV);

        System.out.println("\nBasic Information");
        System.out.println(SUB);
        String first = promptStringNullable(
            "First name (blank if unknown)");
        String last = promptStringNullable(
            "Last name (blank if unknown)");
        LocalDate entry = promptDateDefault(
            "Entry date (YYYY-MM-DD, blank = today)",
            LocalDate.now());

        String firstName = (first == null && last == null)
            ? nextUnknownVictimName()
            : (first == null) ? "(First Name Unknown)" : first;
        String lastName = (first == null && last == null) ? null
            : (last == null) ? "(Last Name Unknown)" : last;

        DisasterVictim v = new DisasterVictim(firstName, entry);
        if (lastName != null) {
            v.setLastName(lastName);
        }

        System.out.println("\nAge Information");
        System.out.println(SUB);
        System.out.println("  1. Date of birth");
        System.out.println("  2. Approximate age");
        System.out.println("  0. Cancel");
        int ageChoice = promptInt("Choice", 0, 2);
        if (ageChoice == 1) {
            LocalDate dob = promptDate("Date of birth (YYYY-MM-DD)");
            v.setDateOfBirth(dob);
        } else if (ageChoice == 2) {
            int age = promptInt("Approximate age", 0, 130);
            v.setApproximateAge(age);
        } else {
            return;
        }

        System.out.println("\nGender Information");
        System.out.println(SUB);
        setGenderInteractive(v);

        System.out.println("\nLocation");
        System.out.println(SUB);
        Location loc = pickLocation("Assign to location");
        if (loc != null) {
            v.setLocation(loc);
        }

        System.out.println("\nAdditional Information");
        System.out.println(SUB);
        String comments = promptStringNullable(
            "Comments (blank to skip)");
        if (comments != null) {
            v.setComments(comments);
        }

        try {
            controller.addVictim(v);
            System.out.println("\nVictim added: " + v);
        } catch (Exception e) {
            System.out.println("Error adding victim: " + e.getMessage());
            return;
        }

        System.out.print("\nAdd cultural requirements now? (y/n): ");
        if (yesNo()) {
            try {
                setCulturalRequirementsInteractive(v);
            } catch (Exception e) {
                System.out.println(
                    "Victim was added, but cultural requirements could not be set: "
                    + e.getMessage());
            }
        }
    }

    private String nextUnknownVictimName() {
        int max = 0;
        for (DisasterVictim v : controller.getAllVictims()) {
            String name = v.getFirstName();
            if (name != null && name.startsWith("Unknown Victim - ")) {
                try {
                    int n = Integer.parseInt(
                        name.substring("Unknown Victim - ".length()));
                    if (n > max) {
                        max = n;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("Unknown Victim - %03d", max + 1);
    }

    private void editName(DisasterVictim v) {
        String oldFirst = v.getFirstName();
        String oldLast  = v.getLastName();
        System.out.println("\n" + SUB);
        String first = promptString(
            "First name [" + oldFirst + "]");
        String last = promptStringNullable(
            "Last name ["
            + (oldLast != null ? oldLast : "")
            + "] (blank to clear)");
        v.setFirstName(first.isBlank() ? oldFirst : first);
        v.setLastName(last);
        controller.updateVictim(v,
            "Name: " + oldFirst + " " + oldLast
            + " -> " + v.getFirstName() + " " + v.getLastName());
        System.out.println("Updated.");
    }

    private void editGender(DisasterVictim v) {
        System.out.println("\n" + SUB);
        System.out.println("Current gender: " + v.getGender());
        setGenderInteractive(v);
        controller.updateVictim(v,
            "Gender updated to: " + v.getGender());
    }

    private void editAge(DisasterVictim v) {
        System.out.println("\n" + SUB);
        if (v.getDateOfBirth() != null) {
            System.out.println(
                "Victim has a date of birth ("
                + v.getDateOfBirth()
                + "). Cannot replace with approximate age.");
        } else {
            System.out.println(
                "  Current approximate age: " + v.getApproximateAge());
            System.out.println("  1. Update approximate age");
            System.out.println("  2. Set exact date of birth");
            int c = promptInt("Choice", 1, 2);
            if (c == 1) {
                int newAge = promptInt("New approximate age", 0, 150);
                try {
                    controller.updateApproximateAge(v, newAge);
                    System.out.println("Updated.");
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            } else {
                LocalDate dob = promptDate(
                    "Date of birth (YYYY-MM-DD)");
                try {
                    controller.setDateOfBirthFromAge(v, dob);
                    System.out.println("Updated.");
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
    }

    private void editComments(DisasterVictim v) {
        System.out.println("\n" + SUB);
        String c = promptStringNullable(
            "Comments ["
            + (v.getComments() != null ? v.getComments() : "")
            + "] (blank to clear)");
        v.setComments(c);
        controller.updateVictim(v, "Comments updated");
        System.out.println("Updated.");
    }

    private void editLocation(DisasterVictim v) {
        System.out.println("\n" + SUB);
        System.out.println(
            "Current location: "
            + (v.getLocation() != null
                ? v.getLocation().getName() : "(unassigned)"));
        Location loc = pickLocation("New location");
        if (loc == null) {
            return;
        }
        if (v.getLocation() != null) {
            try {
                v.getLocation().removeOccupant(v);
            } catch (Exception ignored) {}
        }
        v.setLocation(loc);
        loc.addOccupant(v);
        controller.updateVictim(v, "Location -> " + loc.getName());
        System.out.println("Updated.");
    }

    private void deleteVictim() {
        System.out.println("\n  Archive or Delete Victim");
        System.out.println(DIV);
        DisasterVictim v = pickVictim("Select victim");
        if (v == null) {
            return;
        }

        System.out.println("\n" + SUB);
        System.out.println("  1. Archive  (hide, keep in database)");
        System.out.println("  2. Delete   (permanently remove all data)");
        System.out.println("  0. Cancel");
        int c = promptInt("Choice", 0, 2);

        if (c == 1) {
            System.out.print("Confirm archive of " + v + "? (y/n): ");
            if (yesNo()) {
                controller.softDeleteVictim(v);
                System.out.println("Victim archived.");
            }
        } else if (c == 2) {
            System.out.println(
                "\nWARNING: This will permanently delete " + v
                + " and ALL associated records.");
            System.out.print("Type 'DELETE' to confirm: ");
            String confirm = scanner.nextLine().trim();
            if ("DELETE".equals(confirm)) {
                controller.hardDeleteVictim(v);
                System.out.println("Permanently deleted.");
            } else {
                System.out.println("Deletion cancelled.");
            }
        }
    }

    private void addMedicalRecord(DisasterVictim v) {
        System.out.println("\n  Add Medical Record - " + v);
        System.out.println(DIV);
        Location loc = pickLocation("Treatment location");
        if (loc == null) {
            return;
        }
        String details = promptString("Treatment details");
        LocalDate date = promptDateDefault(
            "Treatment date (YYYY-MM-DD, blank = today)",
            LocalDate.now());
        try {
            MedicalRecord rec = new MedicalRecord(loc, details, date);
            controller.addMedicalRecord(rec, v);
            System.out.println("Medical record added.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void addFamilyRelation(DisasterVictim v) {
        System.out.println("\n  Add Family Relationship - " + v);
        System.out.println(DIV);
        DisasterVictim other = pickVictim("Select other person");
        if (other == null || other == v) {
            System.out.println("Invalid selection.");
            return;
        }
        String rel = promptString(
            "Relationship type (e.g., parent, sibling, spouse)");
        try {
            FamilyRelation relation = new FamilyRelation(v, rel, other);
            controller.addFamilyRelation(relation);
            System.out.println("Relationship added.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void setCulturalRequirementsInteractive(DisasterVictim v) {
        CulturalOptions options = controller.getCulturalOptions();
        List<String> categories =
            new ArrayList<>(options.getCategories());
        if (categories.isEmpty()) {
            System.out.println("No cultural options configured.");
            return;
        }

        boolean back = false;
        while (!back) {
            System.out.println("\n  Cultural Requirements - " + v);
            System.out.println(DIV);
            List<CulturalRequirement> reqs =
                v.getCulturalRequirements();
            if (reqs.isEmpty()) {
                System.out.println("  (none)");
            } else reqs.forEach(r -> System.out.println("  - " + r));
            System.out.println("\n" + SUB);
            System.out.println("  1. Set / update a requirement");
            System.out.println("  2. Remove a requirement");
            System.out.println("  0. Back");
            int c = promptInt("Choice", 0, 2);

            if (c == 1) {
                String cat = pickFromList(
                    "Select category", categories);
                if (cat == null) {
                    continue;
                }
                List<String> opts = new ArrayList<>(
                    options.getOptionsForCategory(cat));
                String opt = pickFromList(
                    "Select option for '" + cat + "'", opts);
                if (opt == null) {
                    continue;
                }
                try {
                    controller.setCulturalRequirement(v, cat, opt);
                    System.out.println("Updated.");
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            } else if (c == 2) {
                if (reqs.isEmpty()) {
                    System.out.println("No requirements to remove.");
                    continue;
                }
                List<String> reqStrings = new ArrayList<>();
                for (CulturalRequirement r : reqs) {
                    reqStrings.add(r.toString());
                }
                int idx = pickIndex(
                    "Select requirement to remove", reqStrings);
                if (idx >= 0) {
                    try {
                        controller.removeCulturalRequirement(
                            v, reqs.get(idx));
                        System.out.println("Removed.");
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
            } else {
                back = true;
            }
        }
    }

    private void manageSkills(DisasterVictim v) {
        boolean back = false;
        while (!back) {
            System.out.println("\n  Skills - " + v);
            System.out.println(DIV);
            List<Skill> skills = v.getSkills();
            if (skills.isEmpty()) {
                System.out.println("  (none)");
            } else for (int i = 0; i < skills.size(); i++) {
                System.out.printf("  %d. %s%n", i + 1, skills.get(i));
            }
            System.out.println("\n" + SUB);
            System.out.println("  1. Add skill");
            System.out.println("  2. Remove skill");
            System.out.println("  0. Back");
            int c = promptInt("Choice", 0, 2);

            if (c == 1) {
                addSkillInteractive(v);
            } else if (c == 2) {
                if (skills.isEmpty()) {
                    System.out.println("No skills to remove.");
                    continue;
                }
                List<String> names = new ArrayList<>();
                for (Skill s : skills) {
                    names.add(s.toString());
                }
                int idx = pickIndex("Select skill to remove", names);
                if (idx >= 0) {
                    try {
                        controller.removeSkill(v, skills.get(idx));
                        System.out.println("Removed.");
                    } catch (Exception e) {
                        System.out.println(
                            "Error: " + e.getMessage());
                    }
                }
            } else {
                back = true;
            }
        }
    }

    private void addSkillInteractive(DisasterVictim v) {
        System.out.println("\n" + SUB);
        System.out.println("  Skill category:");
        System.out.println("  1. Medical");
        System.out.println("  2. Language");
        System.out.println("  3. Trade");
        int cat = promptInt("Choice", 1, 3);
        String[] levels = {"beginner", "intermediate", "advanced"};
        String level = pickFromList(
            "Proficiency level", Arrays.asList(levels));
        if (level == null) {
            return;
        }

        try {
            Skill skill;
            if (cat == 1) {
                List<String> certTypes =
                    Arrays.asList(MedicalSkill.VALID_CERT_TYPES);
                String certType = pickFromList(
                    "Certification type", certTypes);
                if (certType == null) {
                    return;
                }
                LocalDate expiry = null;
                while (expiry == null) {
                    expiry = promptDate(
                        "Certification expiry (YYYY-MM-DD)");
                    if (expiry == null) {
                        System.out.println(
                            "A certification expiry date "
                            + "is required.");
                    }
                }
                skill = new MedicalSkill(
                    certType, level, certType, expiry);
            } else if (cat == 2) {
                String lang = promptString(
                    "Language name (e.g., French)");
                System.out.println("\n" + SUB);
                System.out.println("  Capabilities:");
                System.out.println("  1. read/write");
                System.out.println("  2. speak/listen");
                System.out.println("  3. Both");
                int capChoice = promptInt("Choice", 1, 3);
                Set<String> caps = new HashSet<>();
                if (capChoice == 1) {
                    caps.add("read/write");
                } else if (capChoice == 2) {
                    caps.add("speak/listen");
                } else {
                    caps.add("read/write");
                    caps.add("speak/listen");
                }
                skill = new LanguageSkill(lang, level, caps);
            } else {
                List<String> tradeTypes =
                    Arrays.asList(TradeSkill.VALID_TRADE_TYPES);
                String tradeType = pickFromList(
                    "Trade type", tradeTypes);
                if (tradeType == null) {
                    return;
                }
                skill = new TradeSkill(tradeType, level);
            }
            controller.addSkill(v, skill);
            System.out.println("Skill added: " + skill);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void manageSupplies() {
        boolean back = false;
        while (!back) {
            System.out.println("\nSupplies");
            System.out.println(DIV);
            System.out.println("  1. View supplies by location");
            System.out.println("  2. Add supply to location");
            System.out.println("  3. Edit supply at location");
            System.out.println("  0. Back");
            int c = promptInt("Choice", 0, 3);
            switch (c) {
                case 1 -> viewSuppliesByLocation();
                case 2 -> addSupplyToLocation();
                case 3 -> editSupply();
                case 0 -> back = true;
            }
        }
    }

    private void viewSuppliesByLocation() {
        Location loc = pickLocation("Select location");
        if (loc == null) {
            return;
        }
        List<Supply> supplies = loc.getSupplies();
        System.out.println("\n  Supplies at " + loc.getName());
        System.out.println(DIV);
        if (supplies.isEmpty()) {
            System.out.println("  No supplies at this location.");
            return;
        }
        boolean hasExpired = false;
        for (Supply s : supplies) {
            if (s.isExpired()) {
                hasExpired = true;
                break;
            }
        }
        if (hasExpired) {
            System.out.println(
                "  WARNING: Some supplies here are EXPIRED.");
        }
        for (Supply s : supplies) {
            String allocated = s.getAllocationDate() != null
                ? " [allocated]" : "";
            System.out.println("  - " + s + allocated);
        }
    }

    private void addSupplyToLocation() {
        Location loc = pickLocation("Select location");
        if (loc == null) {
            return;
        }
        String type = promptString(
            "Supply type (e.g., blanket, water, food ration)");
        String desc = promptStringNullable(
            "Description (blank to skip)");
        Supply s = new Supply(type, 1);
        if (desc != null) {
            s.setDescription(desc);
        }
        System.out.print("Is this supply perishable? (y/n): ");
        boolean perishable = yesNo();
        s.setPerishable(perishable);
        if (perishable) {
            LocalDate expiry = null;
            while (expiry == null) {
                expiry = promptDate("Expiry date (YYYY-MM-DD)");
                if (expiry == null) {
                    System.out.println(
                        "An expiry date is required "
                        + "for perishable supplies.");
                }
            }
            s.setExpiryDate(expiry);
        }
        controller.addSupply(s, loc);
        System.out.println("Supply added: " + s);
    }

    private void allocateSupply(DisasterVictim v) {
        warnExpiredSupplies();
        List<Supply> available = controller.getAllocatableSupplies();
        if (available.isEmpty()) {
            System.out.println(
                "No supplies available for allocation.");
            return;
        }
        List<String> supplyNames = new ArrayList<>();
        for (Supply s : available) {
            supplyNames.add(s.toString());
        }
        int idx = pickIndex(
            "Select supply to allocate to " + v, supplyNames);
        if (idx < 0) {
            return;
        }
        try {
            controller.allocateSupply(available.get(idx), v);
            System.out.println("Supply allocated.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void warnExpiredSupplies() {
        if (expiredWarningShown) {
            return;
        }
        expiredWarningShown = true;
        List<Supply> expired = controller.getExpiredSupplies();
        if (!expired.isEmpty()) {
            System.out.println(
                "\nWARNING: The following supplies are EXPIRED:");
            for (Supply s : expired) {
                System.out.println("  - " + s);
            }
        }
    }

    private void manageInquiries() {
        boolean back = false;
        while (!back) {
            System.out.println("\nInquiries");
            System.out.println(DIV);
            System.out.println("  1. View all inquiries");
            System.out.println("  2. Add inquiry");
            System.out.println("  3. View / Edit inquiry");
            System.out.println("  0. Back");
            int c = promptInt("Choice", 0, 3);
            switch (c) {
                case 1 -> listInquiries();
                case 2 -> addInquiry();
                case 3 -> selectAndViewInquiry();
                case 0 -> back = true;
            }
        }
    }

    private void listInquiries() {
        List<ReliefService> inquiries = controller.getVisibleInquiries();
        System.out.println("\n  All Inquiries");
        System.out.println(DIV);
        if (inquiries.isEmpty()) {
            System.out.println("  No inquiries on record.");
            return;
        }
        for (int i = 0; i < inquiries.size(); i++) {
            System.out.println(
                "  " + (i + 1) + ". "
                + inquiries.get(i).getLogDetails());
        }
    }

    private void addInquiry() {
        System.out.println("\n  Add Inquiry");
        System.out.println(DIV);
        List<Inquirer> inquirers = controller.getInquirers();
        Inquirer inquirer;
        if (inquirers.isEmpty()) {
            System.out.println(
                "  No inquirers found. Creating new inquirer.");
            inquirer = createInquirer();
        } else {
            System.out.println("\n" + SUB);
            System.out.println("  1. Select existing inquirer");
            System.out.println("  2. Create new inquirer");
            int c = promptInt("Choice", 1, 2);
            inquirer = (c == 1) ? pickInquirer() : createInquirer();
        }
        if (inquirer == null) {
            return;
        }

        DisasterVictim subject = pickVictim(
            "subject (person being sought)");
        String info = promptString("Details / information provided");
        LocalDate date = promptDateDefault(
            "Date of inquiry (blank = today)", LocalDate.now());

        try {
            ReliefService inquiry = new ReliefService(
                inquirer, subject, date, info);
            controller.addInquiry(inquiry, inquirer);
            System.out.println("Inquiry logged.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private Inquirer createInquirer() {
        String first = promptString("Inquirer first name");
        String last = promptStringNullable(
            "Inquirer last name (blank to skip)");
        String phone = promptStringNullable(
            "Phone number (blank to skip)");
        String info = promptStringNullable(
            "Additional info (blank to skip)");
        return new Inquirer(first, last, phone, info);
    }

    private void manageLocations() {
        boolean back = false;
        while (!back) {
            System.out.println("\nLocations");
            System.out.println(DIV);

            List<Location> locations = controller.getLocations();
            if (locations.isEmpty()) {
                System.out.println("  No locations on record.");
            } else {
                System.out.printf(
                    "  %-4s %-20s %-25s %s%n",
                    "No.", "Name", "Address", "Occupants");
                System.out.println("  " + SUB);
                for (int i = 0; i < locations.size(); i++) {
                    Location loc = locations.get(i);
                    System.out.printf(
                        "  %-4d %-20s %-25s %d%n",
                        i + 1, loc.getName(), loc.getAddress(),
                        loc.getOccupants().size());
                }
            }

            System.out.println("\n" + SUB);
            System.out.println("  1. Add location");
            System.out.println("  2. Edit location");
            System.out.println("  0. Back");
            int c = promptInt("Choice", 0, 2);

            switch (c) {
                case 1 -> addLocation();
                case 2 -> editLocation();
                case 0 -> back = true;
            }
        }
    }

    private void searchSkills() {
        System.out.println("\nSearch Volunteers by Skill");
        System.out.println(DIV);
        System.out.println("  1. Medical");
        System.out.println("  2. Language");
        System.out.println("  3. Trade");
        int c = promptInt("Choice", 1, 3);
        String[] cats = {"medical", "language", "trade"};
        String category = cats[c - 1];

        List<DisasterVictim> results =
            controller.searchBySkillCategory(category);
        System.out.println(
            "\n  Victims with " + category + " skills");
        System.out.println(DIV);
        if (results.isEmpty()) {
            System.out.println("  No active victims found.");
            return;
        }
        for (DisasterVictim v : results) {
            System.out.println("  " + v);
            for (Skill s : v.getSkillsByCategory(category)) {
                System.out.println("    - " + s);
            }
        }
    }

    private void viewLogs() {
        System.out.println("\nLog Files");
        System.out.println(DIV);
        System.out.println(
            "  Action log : data/action_log.txt");
        System.out.println(
            "  Error log  : data/error_log.txt");
    }

    private void setGenderInteractive(DisasterVictim v) {
        Integer age = v.getAge();
        boolean adult = age != null && age >= 18;
        boolean minor = age != null && age < 18;

        List<String> options = new ArrayList<>();
        if (adult || age == null) {
            options.add("Man");
            options.add("Woman");
        }
        if (minor || age == null) {
            options.add("Boy");
            options.add("Girl");
        }
        options.add("non-binary person");
        options.add("Please specify (custom)");

        String choice = pickFromList("Gender", options);
        if (choice == null) {
            return;
        }

        try {
            if ("Please specify (custom)".equals(choice)) {
                v.setGender("Please specify");
                String custom = promptString("Enter custom gender");
                v.setGender(custom);
            } else {
                v.setGender(choice);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void manageSoftDeleted() {
        System.out.println("\n  Archived Victims");
        System.out.println(DIV);
        List<DisasterVictim> deleted =
            controller.getSoftDeletedVictims();
        if (deleted.isEmpty()) {
            System.out.println("  No archived victims.");
            return;
        }
        boolean back = false;
        while (!back) {
            for (int i = 0; i < deleted.size(); i++) {
                System.out.printf(
                    "  %d. %s%n", i + 1, deleted.get(i));
            }
            System.out.println("  0. Back");
            int index = promptInt("Choice", 0, deleted.size());
            if (index == 0) {
                back = true;
                continue;
            }

            DisasterVictim v = deleted.get(index - 1);
            System.out.println("\n" + SUB);
            System.out.println("  1. Restore");
            System.out.println("  2. Permanently Delete");
            System.out.println("  0. Cancel");
            int action = promptInt("Choice", 0, 2);
            if (action == 1) {
                controller.restoreVictim(v);
                deleted.remove(v);
                System.out.println("Restored: " + v);
            } else if (action == 2) {
                System.out.println(
                    "WARNING: This will permanently delete "
                    + v + " and ALL associated data.");
                System.out.print("Type 'DELETE' to confirm: ");
                String confirm = scanner.nextLine().trim();
                if ("DELETE".equals(confirm)) {
                    controller.hardDeleteVictim(v);
                    deleted.remove(v);
                    System.out.println("Deleted.");
                } else {
                    System.out.println("Cancelled.");
                }
            }
            if (deleted.isEmpty()) {
                back = true;
            }
        }
    }

    private int promptInt(String prompt, int min, int max) {
        while (true) {
            if ("Choice".equals(prompt)) {
                System.out.println("\n" + SUB);
            }
            System.out.print(
                "  " + prompt + " (" + min + "-" + max + "): ");
            String line = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(line);
                if (val >= min && val <= max) {
                    System.out.println();
                    return val;
                }
                System.out.println(
                    "  Please enter a number between "
                    + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println(
                    "\n  Invalid input. Please enter a whole number.\n");
            }
        }
    }

    private String promptString(String prompt) {
        while (true) {
            System.out.print("  " + prompt + ": ");
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("  This field cannot be blank.");
        }
    }

    private String promptStringNullable(String prompt) {
        System.out.print("  " + prompt + ": ");
        String line = scanner.nextLine().trim();
        return line.isEmpty() ? null : line;
    }

    private LocalDate promptDate(String prompt) {
        while (true) {
            System.out.print("  " + prompt + ": ");
            String line = scanner.nextLine().trim();
            try {
                return LocalDate.parse(line);
            } catch (DateTimeParseException e) {
                System.out.println(
                    "  Invalid date. Use YYYY-MM-DD "
                    + "(e.g., 2025-01-18).");
            }
        }
    }

    private LocalDate promptDateDefault(
            String prompt, LocalDate defaultValue) {
        System.out.print("  " + prompt + ": ");
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(line);
        } catch (DateTimeParseException e) {
            System.out.println(
                "  Invalid date, using default: " + defaultValue);
            return defaultValue;
        }
    }

    private boolean yesNo() {
        while (true) {
            String line = scanner.nextLine().trim().toLowerCase();
            if (line.equals("y") || line.equals("yes")) {
                return true;
            }
            if (line.equals("n") || line.equals("no")) {
                return false;
            }
            System.out.print("  Please enter y or n: ");
        }
    }

    private DisasterVictim pickVictim(String prompt) {
        List<DisasterVictim> victims = controller.getActiveVictims();
        if (victims.isEmpty()) {
            System.out.println("  No active victims available.");
            return null;
        }
        List<String> names = new ArrayList<>();
        for (DisasterVictim v : victims) {
            names.add(v.toString());
        }
        int idx = pickIndex(prompt, names);
        return idx >= 0 ? victims.get(idx) : null;
    }

    private Location pickLocation(String prompt) {
        List<Location> locs = controller.getLocations();
        if (locs.isEmpty()) {
            System.out.println("  No locations available.");
            return null;
        }
        List<String> names = new ArrayList<>();
        for (Location l : locs) {
            names.add(l.getName() + " - " + l.getAddress());
        }
        int idx = pickIndex(prompt, names);
        return idx >= 0 ? locs.get(idx) : null;
    }

    private Inquirer pickInquirer() {
        List<Inquirer> inquirers = controller.getInquirers();
        if (inquirers.isEmpty()) {
            return null;
        }
        List<String> names = new ArrayList<>();
        for (Inquirer i : inquirers) {
            names.add(i.toString());
        }
        int idx = pickIndex("Select inquirer", names);
        return idx >= 0 ? inquirers.get(idx) : null;
    }

    private String pickFromList(String prompt, List<String> options) {
        System.out.println("  " + prompt + ":");
        for (int i = 0; i < options.size(); i++) {
            System.out.printf("    %d. %s%n", i + 1, options.get(i));
        }
        int idx = promptInt("Choice", 1, options.size());
        return options.get(idx - 1);
    }

    private int pickIndex(String prompt, List<String> options) {
        System.out.println("  " + prompt + ":");
        for (int i = 0; i < options.size(); i++) {
            System.out.printf("    %d. %s%n", i + 1, options.get(i));
        }
        System.out.println("    0. Cancel");
        int choice = promptInt("Choice", 0, options.size());
        return choice == 0 ? -1 : choice - 1;
    }

    private void addLocation() {
        System.out.println("\nAdd Location");
        System.out.println(DIV);

        String name = promptString("Location name");
        String address = promptString("Location address");

        try {
            Location location = new Location(name, address);
            controller.addLocation(location);
            System.out.println("Location added: " + location.getName());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void editLocation() {
        Location loc = pickLocation("Select location");
        if (loc == null) {
            return;
        }

        boolean back = false;
        while (!back) {
            System.out.println("\nEdit Location - " + loc.getName());
            System.out.println(DIV);
            System.out.println("  1. Name      [" + loc.getName() + "]");
            System.out.println("  2. Address   [" + loc.getAddress() + "]");
            System.out.println("  0. Back");
            int c = promptInt("Choice", 0, 2);

            switch (c) {
                case 1 -> {
                    String oldName = loc.getName();
                    String newName = promptString("New name");
                    loc.setName(newName);
                    controller.updateLocation(loc,
                        "Name: " + oldName + " -> " + newName);
                    System.out.println("Updated.");
                }
                case 2 -> {
                    String oldAddress = loc.getAddress();
                    String newAddress = promptString("New address");
                    loc.setAddress(newAddress);
                    controller.updateLocation(loc,
                        "Address: " + oldAddress + " -> " + newAddress);
                    System.out.println("Updated.");
                }
                case 0 -> back = true;
            }
        }
    }

    private void editSupply() {
        warnExpiredSupplies();

        Location loc = pickLocation("Select location");
        if (loc == null) {
            return;
        }

        List<Supply> supplies = loc.getSupplies();
        if (supplies.isEmpty()) {
            System.out.println("  No supplies at this location.");
            return;
        }

        List<String> names = new ArrayList<>();
        for (Supply s : supplies) {
            names.add(s.toString());
        }
        int idx = pickIndex("Select supply to edit", names);
        if (idx < 0) {
            return;
        }

        Supply supply = supplies.get(idx);

        boolean back = false;
        while (!back) {
            System.out.println("\nEdit Supply - " + supply);
            System.out.println(DIV);

            String expiryVal = supply.getExpiryDate() != null
                ? supply.getExpiryDate().toString() : "(none)";
            String descVal = supply.getDescription() != null
                ? supply.getDescription() : "(none)";

            System.out.println("  1. Type         [" + supply.getType() + "]");
            System.out.println("  2. Description  [" + descVal + "]");
            System.out.println("  3. Expiry Date  [" + expiryVal + "]");
            System.out.println("  0. Back");

            int c = promptInt("Choice", 0, 3);
            switch (c) {
                case 1 -> editSupplyType(supply, loc);
                case 2 -> editSupplyDescription(supply, loc);
                case 3 -> editSupplyExpiry(supply, loc);
                case 0 -> back = true;
            }
        }
    }

    private void editSupplyType(Supply supply, Location loc) {
        String oldType = supply.getType();
        String newType = promptString(
            "New supply type (e.g., blanket, water, food ration)");

        try {
            supply.setType(newType);

            System.out.print("Is this supply perishable? (y/n): ");
            boolean perishable = yesNo();
            supply.setPerishable(perishable);

            if (perishable) {
                LocalDate expiry = null;
                while (expiry == null) {
                    expiry = promptDate("Expiry date (YYYY-MM-DD)");
                    if (expiry == null) {
                        System.out.println(
                            "An expiry date is required "
                            + "for perishable supplies.");
                    }
                }
                supply.setExpiryDate(expiry);
            }

            controller.updateSupply(
                supply,
                loc.getDbId(),
                null,
                "Type: " + oldType + " -> " + supply.getType()
            );
            System.out.println("Updated.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void editSupplyDescription(Supply supply, Location loc) {
        String oldDesc = supply.getDescription();
        String newDesc = promptStringNullable(
            "Description ["
            + (oldDesc != null ? oldDesc : "")
            + "] (blank to clear)");

        try {
            supply.setDescription(newDesc);
            controller.updateSupply(
                supply,
                loc.getDbId(),
                null,
                "Description: "
                + (oldDesc != null ? oldDesc : "(none)")
                + " -> "
                + (newDesc != null ? newDesc : "(none)")
            );
            System.out.println("Updated.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void editSupplyExpiry(Supply supply, Location loc) {
        if (!supply.isPerishable()) {
            System.out.println(
                "This supply is not perishable. Change the type first.");
            return;
        }

        LocalDate oldExpiry = supply.getExpiryDate();
        LocalDate newExpiry = promptDate(
            "New expiry date (YYYY-MM-DD)");

        try {
            supply.setExpiryDate(newExpiry);
            controller.updateSupply(
                supply,
                loc.getDbId(),
                null,
                "Expiry date: "
                + (oldExpiry != null ? oldExpiry : "(none)")
                + " -> " + newExpiry
            );
            System.out.println("Updated.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Prompts the user to select an inquiry and opens its detail view.
     */
    private void selectAndViewInquiry() {
        ReliefService inquiry = pickInquiry("Select inquiry");
        if (inquiry == null) {
            return;
        }
        viewInquiry(inquiry);
    }

    /**
     * Displays a single inquiry and allows the user to edit it.
     *
     * @param inquiry the inquiry to view
     */
    private void viewInquiry(ReliefService inquiry) {
        boolean back = false;
        while (!back) {
            printInquiry(inquiry);
            System.out.println("\n" + SUB);
            System.out.println("  1. Edit Inquiry");
            System.out.println("  0. Back");
            int c = promptInt("Choice", 0, 1);
            if (c == 1) {
                editInquiry(inquiry);
            } else back = true;
        }
    }

    /**
     * Prints the details of a single inquiry.
     *
     * @param inquiry the inquiry to display
     */
    private void printInquiry(ReliefService inquiry) {
        System.out.println("\n  Inquiry Details");
        System.out.println(DIV);

        String inquirerName = inquiry.getInquirer() != null
            ? inquiry.getInquirer().toString()
            : "(none)";
        String subjectName = inquiry.getMissingPerson() != null
            ? inquiry.getMissingPerson().toString()
            : "(none)";
        String date = inquiry.getDateOfInquiry() != null
            ? inquiry.getDateOfInquiry().toString()
            : "(not set)";
        String details = inquiry.getInfoProvided() != null
            ? inquiry.getInfoProvided()
            : "(none)";

        System.out.println("  ID        : " + inquiry.getDbId());
        System.out.println("  Inquirer  : " + inquirerName);
        System.out.println("  Subject   : " + subjectName);
        System.out.println("  Date      : " + date);
        System.out.println("  Details   : " + details);
    }

    /**
     * Prompts the user to select an inquiry from the current inquiry list.
     *
     * @param prompt the selection prompt to display
     * @return the selected inquiry, or {@code null} if cancelled
     */
    private ReliefService pickInquiry(String prompt) {
        List<ReliefService> inquiries = controller.getVisibleInquiries();
        if (inquiries.isEmpty()) {
            System.out.println("  No inquiries on record.");
            return null;
        }

        List<String> names = new ArrayList<>();
        for (ReliefService inquiry : inquiries) {
            names.add(inquiry.getLogDetails());
        }

        int idx = pickIndex(prompt, names);
        return idx >= 0 ? inquiries.get(idx) : null;
    }

    /**
     * Allows the user to edit an existing inquiry.
     *
     * @param inquiry the inquiry to edit
     */
    private void editInquiry(ReliefService inquiry) {
        boolean back = false;
        while (!back) {
            System.out.println("\nEdit Inquiry");
            System.out.println(DIV);

            String inquirerVal = inquiry.getInquirer() != null
                ? inquiry.getInquirer().toString()
                : "(none)";
            String subjectVal = inquiry.getMissingPerson() != null
                ? inquiry.getMissingPerson().toString()
                : "(none)";
            String dateVal = inquiry.getDateOfInquiry() != null
                ? inquiry.getDateOfInquiry().toString()
                : "(not set)";
            String detailsVal = inquiry.getInfoProvided() != null
                ? inquiry.getInfoProvided()
                : "(none)";

            System.out.println("  1. Inquirer   [" + inquirerVal + "]");
            System.out.println("  2. Subject    [" + subjectVal + "]");
            System.out.println("  3. Date       [" + dateVal + "]");
            System.out.println("  4. Details    [" + detailsVal + "]");
            System.out.println("  0. Back");

            int c = promptInt("Choice", 0, 4);
            switch (c) {
                case 1 -> editInquiryInquirer(inquiry);
                case 2 -> editInquirySubject(inquiry);
                case 3 -> editInquiryDate(inquiry);
                case 4 -> editInquiryDetails(inquiry);
                case 0 -> back = true;
            }
        }
    }

    /**
     * Updates the inquirer attached to an inquiry.
     *
     * @param inquiry the inquiry to update
     */
    private void editInquiryInquirer(ReliefService inquiry) {
        System.out.println("\n" + SUB);
        System.out.println("  1. Select existing inquirer");
        System.out.println("  2. Create new inquirer");
        int c = promptInt("Choice", 1, 2);

        Inquirer newInquirer = (c == 1) ? pickInquirer() : createInquirer();
        if (newInquirer == null) {
            return;
        }

        inquiry.setInquirer(newInquirer);
        try {
            controller.updateInquiry(inquiry);
            System.out.println("Updated.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Updates the subject person attached to an inquiry.
     *
     * @param inquiry the inquiry to update
     */
    private void editInquirySubject(ReliefService inquiry) {
        System.out.println("\n" + SUB);
        DisasterVictim subject = pickVictim("Select subject");
        if (subject == null) {
            return;
        }

        inquiry.setMissingPerson(subject);
        try {
            controller.updateInquiry(inquiry);
            System.out.println("Updated.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Updates the inquiry date.
     *
     * @param inquiry the inquiry to update
     */
    private void editInquiryDate(ReliefService inquiry) {
        System.out.println("\n" + SUB);
        LocalDate newDate = promptDate("New inquiry date (YYYY-MM-DD)");
        inquiry.setDateOfInquiry(newDate);
        try {
            controller.updateInquiry(inquiry);
            System.out.println("Updated.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Updates the inquiry details text.
     *
     * @param inquiry the inquiry to update
     */
    private void editInquiryDetails(ReliefService inquiry) {
        System.out.println("\n" + SUB);
        String details = promptString("New details / information provided");
        inquiry.setInfoProvided(details);
        try {
            controller.updateInquiry(inquiry);
            System.out.println("Updated.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Displays and manages the medical records for a disaster victim.
     *
     * @param victim the {@link DisasterVictim} whose medical records are managed
     */
    private void manageMedicalRecords(DisasterVictim victim) {
        boolean back = false;
        while (!back) {
            System.out.println("\n  Medical Records - " + victim);
            System.out.println(DIV);

            List<MedicalRecord> records = victim.getMedicalRecords();
            if (records.isEmpty()) {
                System.out.println("  (none)");
            } else {
                for (int i = 0; i < records.size(); i++) {
                    MedicalRecord r = records.get(i);
                    System.out.printf(
                        "  %d. %s at %s: %s%n",
                        i + 1,
                        r.getDateOfTreatment(),
                        r.getLocation().getName(),
                        r.getTreatmentDetails()
                    );
                }
            }

            System.out.println("\n" + SUB);
            System.out.println("  1. Add medical record");
            System.out.println("  2. Edit medical record");
            System.out.println("  0. Back");
            int c = promptInt("Choice", 0, 2);

            switch (c) {
                case 1 -> addMedicalRecord(victim);
                case 2 -> {
                    MedicalRecord record = pickMedicalRecord(victim);
                    if (record != null) {
                        editMedicalRecord(record);
                    }
                }
                case 0 -> back = true;
            }
        }
    }

    /**
     * Prompts the user to select a medical record belonging to a victim.
     *
     * @param victim the {@link DisasterVictim} whose records are available
     * @return the selected {@link MedicalRecord}, or {@code null} if cancelled
     */
    private MedicalRecord pickMedicalRecord(DisasterVictim victim) {
        List<MedicalRecord> records = victim.getMedicalRecords();
        if (records.isEmpty()) {
            System.out.println("  No medical records on file.");
            return null;
        }

        List<String> options = new ArrayList<>();
        for (MedicalRecord r : records) {
            options.add(
                r.getDateOfTreatment()
                + " at " + r.getLocation().getName()
                + ": " + r.getTreatmentDetails()
            );
        }

        int idx = pickIndex("Select medical record", options);
        return idx >= 0 ? records.get(idx) : null;
    }

    /**
     * Allows the user to edit an existing medical record.
     *
     * @param record the {@link MedicalRecord} to edit
     */
    private void editMedicalRecord(MedicalRecord record) {
        boolean back = false;
        while (!back) {
            System.out.println("\n  Edit Medical Record");
            System.out.println(DIV);
            System.out.println(
                "  1. Location          [" + record.getLocation().getName() + "]");
            System.out.println(
                "  2. Treatment Details [" + record.getTreatmentDetails() + "]");
            System.out.println(
                "  3. Date of Treatment [" + record.getDateOfTreatment() + "]");
            System.out.println("  0. Back");

            int c = promptInt("Choice", 0, 3);
            switch (c) {
                case 1 -> editMedicalRecordLocation(record);
                case 2 -> editMedicalRecordDetails(record);
                case 3 -> editMedicalRecordDate(record);
                case 0 -> back = true;
            }
        }
    }

    /**
     * Updates the treatment location of a medical record.
     *
     * @param record the {@link MedicalRecord} to update
     */
    private void editMedicalRecordLocation(MedicalRecord record) {
        Location oldLoc = record.getLocation();
        Location newLoc = pickLocation("New treatment location");
        if (newLoc == null) {
            return;
        }

        try {
            record.setLocation(newLoc);
            controller.updateMedicalRecord(
                record,
                "Location: " + oldLoc.getName() + " -> " + newLoc.getName()
            );
            System.out.println("Updated.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Updates the treatment details of a medical record.
     *
     * @param record the {@link MedicalRecord} to update
     */
    private void editMedicalRecordDetails(MedicalRecord record) {
        String oldDetails = record.getTreatmentDetails();
        String newDetails = promptString("New treatment details");

        try {
            record.setTreatmentDetails(newDetails);
            controller.updateMedicalRecord(
                record,
                "Treatment details: " + oldDetails + " -> " + newDetails
            );
            System.out.println("Updated.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Updates the treatment date of a medical record.
     *
     * @param record the {@link MedicalRecord} to update
     */
    private void editMedicalRecordDate(MedicalRecord record) {
        LocalDate oldDate = record.getDateOfTreatment();
        LocalDate newDate = promptDate("New treatment date (YYYY-MM-DD)");

        try {
            record.setDateOfTreatment(newDate);
            controller.updateMedicalRecord(
                record,
                "Date of treatment: " + oldDate + " -> " + newDate
            );
            System.out.println("Updated.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Displays and manages the family relationships for a disaster victim.
     *
     * @param victim the {@link DisasterVictim} whose relationships are managed
     */
    private void manageFamilyRelations(DisasterVictim victim) {
        boolean back = false;
        while (!back) {
            System.out.println("\n  Family Relationships - " + victim);
            System.out.println(DIV);

            List<FamilyRelation> relations = victim.getFamilyConnections();
            if (relations.isEmpty()) {
                System.out.println("  (none)");
            } else {
                for (int i = 0; i < relations.size(); i++) {
                    FamilyRelation r = relations.get(i);
                    DisasterVictim other =
                        r.getPersonOne().equals(victim)
                            ? r.getPersonTwo() : r.getPersonOne();
                    System.out.printf(
                        "  %d. %s of %s%n",
                        i + 1,
                        r.getRelationshipTo(),
                        other
                    );
                }
            }

            System.out.println("\n" + SUB);
            System.out.println("  1. Add family relationship");
            System.out.println("  2. Edit family relationship");
            System.out.println("  3. Remove family relationship");
            System.out.println("  0. Back");
            int c = promptInt("Choice", 0, 3);

            switch (c) {
                case 1 -> addFamilyRelation(victim);
                case 2 -> {
                    FamilyRelation relation = pickFamilyRelation(victim);
                    if (relation != null) {
                        editFamilyRelation(victim, relation);
                    }
                }
                case 3 -> {
                    FamilyRelation relation = pickFamilyRelation(victim);
                    if (relation != null) {
                        removeFamilyRelationInteractive(relation);
                    }
                }
                case 0 -> back = true;
            }
        }
    }

    /**
     * Prompts the user to select a family relationship belonging to a victim.
     *
     * @param victim the {@link DisasterVictim} whose relationships are available
     * @return the selected {@link FamilyRelation}, or {@code null} if cancelled
     */
    private FamilyRelation pickFamilyRelation(DisasterVictim victim) {
        List<FamilyRelation> relations = victim.getFamilyConnections();
        if (relations.isEmpty()) {
            System.out.println("  No family relationships on file.");
            return null;
        }

        List<String> options = new ArrayList<>();
        for (FamilyRelation r : relations) {
            DisasterVictim other =
                r.getPersonOne().equals(victim)
                    ? r.getPersonTwo() : r.getPersonOne();
            options.add(r.getRelationshipTo() + " of " + other);
        }

        int idx = pickIndex("Select family relationship", options);
        return idx >= 0 ? relations.get(idx) : null;
    }

    /**
     * Removes an existing family relationship after confirmation.
     *
     * @param relation the {@link FamilyRelation} to remove
     */
    private void removeFamilyRelationInteractive(FamilyRelation relation) {
        System.out.print("Confirm removal of this relationship? (y/n): ");
        if (!yesNo()) {
            return;
        }

        try {
            controller.removeFamilyRelation(relation);
            System.out.println("Removed.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Allows the user to edit an existing family relationship.
     *
     * @param victim the {@link DisasterVictim} currently being edited
     * @param relation the {@link FamilyRelation} to edit
     */
    private void editFamilyRelation(DisasterVictim victim,
                                    FamilyRelation relation) {
        boolean back = false;
        while (!back) {
            DisasterVictim other =
                relation.getPersonOne().equals(victim)
                    ? relation.getPersonTwo() : relation.getPersonOne();

            System.out.println("\n  Edit Family Relationship");
            System.out.println(DIV);
            System.out.println(
                "  1. Relationship Type [" + relation.getRelationshipTo() + "]");
            System.out.println("  2. Related Person     [" + other + "]");
            System.out.println("  0. Back");

            int c = promptInt("Choice", 0, 2);
            switch (c) {
                case 1 -> editFamilyRelationType(relation);
                case 2 -> editFamilyRelationPerson(victim, relation);
                case 0 -> back = true;
            }
        }
    }

    /**
     * Updates the relationship type of a family relationship.
     *
     * @param relation the {@link FamilyRelation} to update
     */
    private void editFamilyRelationType(FamilyRelation relation) {
        String oldType = relation.getRelationshipTo();
        String newType = promptString(
            "New relationship type (e.g., parent, sibling, spouse)");

        try {
            relation.setRelationshipTo(newType);
            controller.updateFamilyRelation(
                relation,
                "Relationship type: " + oldType + " -> " + newType
            );
            System.out.println("Updated.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Updates the related person in a family relationship.
     *
     * @param victim the {@link DisasterVictim} currently being edited
     * @param relation the {@link FamilyRelation} to update
     */
    private void editFamilyRelationPerson(DisasterVictim victim,
                                        FamilyRelation relation) {
        DisasterVictim newOther = pickVictim("Select new related person");
        if (newOther == null || newOther.equals(victim)) {
            System.out.println("Invalid selection.");
            return;
        }

        DisasterVictim oldOther =
            relation.getPersonOne().equals(victim)
                ? relation.getPersonTwo() : relation.getPersonOne();

        try {
            if (relation.getPersonOne().equals(victim)) {
                relation.setPersonTwo(newOther);
            } else {
                relation.setPersonOne(newOther);
            }

            controller.updateFamilyRelation(
                relation,
                "Related person: " + oldOther + " -> " + newOther
            );
            System.out.println("Updated.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}