package edu.ucalgary.oop;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.*;

/**
 * Live PostgreSQL implementation of {@link DatabaseQueryInterface}. Executes
 * SQL statements against the application's database using the provided JDBC
 * {@link Connection}.
 *
 * <p>All load methods populate and return domain-object lists. All mutating
 * methods use {@link PreparedStatement} to prevent SQL injection. Runtime
 * failures are wrapped in {@link RuntimeException} so callers are not forced
 * to handle checked SQL exceptions.</p>
 *
 * @author Eric Hallett (30117108)
 * @version 1.0
 * @since 2026-03-27
 */
public class DatabaseQuery implements DatabaseQueryInterface {

    private final Connection conn;

    /**
     * Constructs a {@code DatabaseQuery} that uses the given JDBC connection.
     *
     * @param conn an open {@link Connection} to the PostgreSQL database
     */
    public DatabaseQuery(Connection conn) {
        this.conn = conn;
    }

    /**
     * Loads all locations from the {@code Location} table.
     *
     * @return a list of {@link Location} objects with database IDs set
     * @throws RuntimeException if the query fails
     */
    @Override
    public List<Location> loadLocations() {
        List<Location> list = new ArrayList<>();
        String sql = "SELECT id, name, address FROM Location";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Location location = new Location(
                    rs.getString("name"), rs.getString("address"));
                location.setDbId(rs.getInt("id"));
                list.add(location);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load locations", e);
        }
        return list;
    }

    /**
     * Loads all disaster victims from the {@code Person} and
     * {@code DisasterVictim} tables and associates each with their location.
     *
     * @param locations the already-loaded locations used for association
     * @return a list of {@link DisasterVictim} objects
     * @throws RuntimeException if the query fails
     */
    @Override
    public List<DisasterVictim> loadDisasterVictims(List<Location> locations) {
        List<DisasterVictim> list = new ArrayList<>();
        String sql =
            "SELECT p.id, p.first_name, p.last_name, p.comments, " +
            "d.date_of_birth, d.approximate_age, d.gender, d.entry_date, " +
            "d.location_id, d.is_soft_deleted " +
            "FROM Person p JOIN DisasterVictim d ON p.id = d.person_id";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                LocalDate entryDate = rs.getDate("entry_date").toLocalDate();
                DisasterVictim v = new DisasterVictim(
                    rs.getString("first_name"), entryDate);
                v.setDbId(rs.getInt("id"));
                v.setLastName(rs.getString("last_name"));
                v.setComments(rs.getString("comments"));
                v.setSoftDeleted(rs.getBoolean("is_soft_deleted"));

                java.sql.Date dob = rs.getDate("date_of_birth");
                if (dob != null) {
                    v.setDateOfBirth(dob.toLocalDate());
                }

                int approxAge = rs.getInt("approximate_age");
                if (!rs.wasNull()) {
                    v.setApproximateAge(approxAge);
                }

                String gender = rs.getString("gender");
                if (gender != null) {
                    v.setGenderDirect(gender);
                }

                int locationId = rs.getInt("location_id");
                if (!rs.wasNull()) {
                    for (Location loc : locations) {
                        if (loc.getDbId() == locationId) {
                            v.setLocation(loc);
                            loc.addOccupant(v);
                            break;
                        }
                    }
                }
                list.add(v);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load disaster victims", e);
        }
        return list;
    }

    /**
     * Loads all inquirers from the {@code Person} table, excluding any person
     * who is also a disaster victim.
     *
     * @return a list of {@link Inquirer} objects
     * @throws RuntimeException if the query fails
     */
    @Override
    public List<Inquirer> loadInquirers() {
        List<Inquirer> list = new ArrayList<>();
        String sql =
            "SELECT p.id, p.first_name, p.last_name, p.comments " +
            "FROM Person p " +
            "WHERE p.id NOT IN (SELECT person_id FROM DisasterVictim)";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Inquirer inq = new Inquirer(
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    null,
                    rs.getString("comments"));
                inq.setDbId(rs.getInt("id"));
                list.add(inq);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load inquirers", e);
        }
        return list;
    }

    /**
     * Loads all inquiries from the {@code Inquiry} table and links each to
     * its inquirer and optional missing-person victim. The
     * {@code inquiry_date} column is a {@code TIMESTAMP} in the database;
     * it is converted to a {@link LocalDate} by extracting the date part.
     * Rows whose inquirer cannot be matched in the provided list are skipped.
     *
     * @param victims   the already-loaded victims used for association
     * @param inquirers the already-loaded inquirers used for association
     * @param locations unused; retained for interface compatibility
     * @return a list of {@link ReliefService} inquiry objects
     * @throws RuntimeException if the query fails
     */
    @Override
    public List<ReliefService> loadInquiries(List<DisasterVictim> victims,
                                              List<Inquirer> inquirers,
                                              List<Location> locations) {
        List<ReliefService> list = new ArrayList<>();
        String sql =
            "SELECT id, inquirer_id, subject_person_id, "
            + "inquiry_date, details FROM Inquiry";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int inquirerId = rs.getInt("inquirer_id");
                int subjectId  = rs.getInt("subject_person_id");
                java.sql.Timestamp ts = rs.getTimestamp("inquiry_date");
                LocalDate date = ts.toLocalDateTime().toLocalDate();
                String details = rs.getString("details");

                Inquirer inq = findById(inquirers, inquirerId);
                if (inq == null) {
                    continue;
                }

                DisasterVictim subject =
                    findVictimById(victims, subjectId);

                ReliefService inquiry = new ReliefService(
                    inq, subject, date, details);
                inquiry.setDbId(rs.getInt("id"));
                list.add(inquiry);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load inquiries", e);
        }
        return list;
    }

    /**
     * Loads all supplies from the {@code Supply} table and associates each with
     * its location or allocated victim.
     *
     * @param locations the already-loaded locations used for association
     * @param victims   the already-loaded victims used for association
     * @return a list of {@link Supply} objects
     * @throws RuntimeException if the query fails
     */
    @Override
    public List<Supply> loadSupplies(List<Location> locations,
                                     List<DisasterVictim> victims) {
        List<Supply> list = new ArrayList<>();
        String sql =
            "SELECT id, supply_type, location_id, victim_id, " +
            "expiry_date, allocation_date, description FROM Supply";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                java.sql.Date expDate   = rs.getDate("expiry_date");
                java.sql.Date allocDate = rs.getDate("allocation_date");
                Supply s = new Supply(
                    rs.getInt("id"),
                    rs.getString("supply_type"),
                    expDate   != null ? expDate.toLocalDate()   : null,
                    allocDate != null ? allocDate.toLocalDate() : null,
                    rs.getString("description"));
                list.add(s);

                int locId = rs.getInt("location_id");
                if (!rs.wasNull()) {
                    for (Location loc : locations) {
                        if (loc.getDbId() == locId) {
                            loc.addSupply(s);
                            break;
                        }
                    }
                }
                int vicId = rs.getInt("victim_id");
                if (!rs.wasNull()) {
                    DisasterVictim v = findVictimById(victims, vicId);
                    if (v != null) {
                        v.addPersonalBelonging(s);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load supplies", e);
        }
        return list;
    }

    /**
     * Loads all medical records from the {@code MedicalRecord} table and
     * attaches each to the appropriate victim.
     *
     * @param victims   the already-loaded victims used for association
     * @param locations the already-loaded locations used for treatment location
     * @return a list of {@link MedicalRecord} objects
     * @throws RuntimeException if the query fails
     */
    @Override
    public List<MedicalRecord> loadMedicalRecords(List<DisasterVictim> victims,
                                                   List<Location> locations) {
        List<MedicalRecord> list = new ArrayList<>();
        String sql =
            "SELECT id, victim_id, treatment_details, " +
            "treatment_date, location_id FROM MedicalRecord";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                LocalDate date = rs.getDate("treatment_date").toLocalDate();
                if (date.isAfter(LocalDate.now())) {
                    continue;
                }

                int locId = rs.getInt("location_id");
                Location loc = findLocationById(locations, locId);
                if (loc == null) {
                    continue;
                }

                MedicalRecord rec = new MedicalRecord(
                    loc, rs.getString("treatment_details"), date);
                rec.setDbId(rs.getInt("id"));

                DisasterVictim v =
                    findVictimById(victims, rs.getInt("victim_id"));
                if (v != null) {
                    v.addMedicalRecord(rec);
                }
                list.add(rec);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load medical records", e);
        }
        return list;
    }

    /**
     * Loads all family relationships from the {@code FamilyRelationship} table
     * and attaches each to both involved victims.
     *
     * @param victims the already-loaded victims used for association
     * @return a list of {@link FamilyRelation} objects
     * @throws RuntimeException if the query fails
     */
    @Override
    public List<FamilyRelation> loadFamilyRelations(
            List<DisasterVictim> victims) {
        List<FamilyRelation> list = new ArrayList<>();
        String sql =
            "SELECT id, person_one_id, person_two_id, " +
            "relationship_type FROM FamilyRelationship";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                DisasterVictim one =
                    findVictimById(victims, rs.getInt("person_one_id"));
                DisasterVictim two =
                    findVictimById(victims, rs.getInt("person_two_id"));
                if (one == null || two == null) {
                    continue;
                }
                FamilyRelation rel = new FamilyRelation(
                    one, rs.getString("relationship_type"), two);
                rel.setDbId(rs.getInt("id"));
                one.addFamilyConnection(rel);
                two.addFamilyConnection(rel);
                list.add(rel);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load family relations", e);
        }
        return list;
    }

    /**
     * Loads all cultural requirements from the {@code CulturalRequirement}
     * table and attaches each to the appropriate victim.
     *
     * @param victims the already-loaded victims used for association
     * @return a list of {@link CulturalRequirement} objects
     * @throws RuntimeException if the query fails
     */
    @Override
    public List<CulturalRequirement> loadCulturalRequirements(
            List<DisasterVictim> victims) {
        List<CulturalRequirement> list = new ArrayList<>();
        String sql =
            "SELECT id, victim_id, requirement_category, " +
            "requirement_option FROM CulturalRequirement";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                CulturalRequirement req = new CulturalRequirement(
                    rs.getString("requirement_category"),
                    rs.getString("requirement_option"));
                req.setDbId(rs.getInt("id"));
                DisasterVictim v =
                    findVictimById(victims, rs.getInt("victim_id"));
                if (v != null) {
                    addCulturalReqDirectly(v, req);
                }
                list.add(req);
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to load cultural requirements", e);
        }
        return list;
    }

    /**
     * Loads all victim skills from the {@code VictimSkill} and {@code Skill}
     * tables and attaches each to the appropriate victim.
     *
     * @param victims the already-loaded victims used for association
     * @return a list of {@link Skill} objects
     * @throws RuntimeException if the query fails
     */
    @Override
    public List<Skill> loadSkills(List<DisasterVictim> victims) {
        List<Skill> list = new ArrayList<>();
        String sql =
            "SELECT vs.id AS vs_id, s.id AS s_id, s.skill_name, " +
            "s.category, vs.victim_id, vs.proficiency_level, " +
            "vs.details, vs.language_capabilities, " +
            "vs.certification_expiry " +
            "FROM VictimSkill vs JOIN Skill s ON vs.skill_id = s.id";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String name     = rs.getString("skill_name");
                String category = rs.getString("category");
                String level    = rs.getString("proficiency_level");
                Skill skill;

                if ("medical".equals(category)) {
                    java.sql.Date expDate =
                        rs.getDate("certification_expiry");
                    if (expDate == null) {
                        throw new IllegalArgumentException(
                            "Medical skill '" + name
                            + "' has no certification expiry date");
                    }
                    skill = new MedicalSkill(
                        name, level, name, expDate.toLocalDate());
                } else if ("language".equals(category)) {
                    String caps = rs.getString("language_capabilities");
                    Set<String> capSet = new HashSet<>();
                    if (caps != null) {
                        for (String c : caps.split(",")) {
                            String trimmed = c.trim();
                            if (!trimmed.isEmpty()) {
                                capSet.add(trimmed);
                            }
                        }
                    }
                    if (capSet.isEmpty()) {
                        capSet.add("speak/listen");
                    }
                    skill = new LanguageSkill(name, level, capSet);
                } else {
                    skill = new TradeSkill(name, level);
                }

                skill.setDbId(rs.getInt("s_id"));
                skill.setVictimSkillDbId(rs.getInt("vs_id"));

                DisasterVictim v =
                    findVictimById(victims, rs.getInt("victim_id"));
                if (v != null) {
                    try { v.addSkill(skill);
                    } catch (IllegalArgumentException ignored) {}
                }
                list.add(skill);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load skills", e);
        }
        return list;
    }

    /**
     * Inserts a new disaster victim into the {@code Person} and
     * {@code DisasterVictim} tables, using the lowest available person ID.
     *
     * @param victim the {@link DisasterVictim} to persist
     * @return the database-assigned person ID
     * @throws RuntimeException if any insert or ID-lookup fails
     */
    @Override
    public int insertDisasterVictim(DisasterVictim victim) {
        int personId;
        String nextIdSql =
            "SELECT s.id FROM generate_series(1, " +
            "(SELECT COALESCE(MAX(id), 0) + 1 FROM Person)) AS s(id) " +
            "LEFT JOIN Person p ON p.id = s.id " +
            "WHERE p.id IS NULL ORDER BY s.id LIMIT 1";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(nextIdSql)) {
            rs.next();
            personId = rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to determine next available person ID", e);
        }

        String personSql =
            "INSERT INTO Person (id, first_name, last_name, comments)"
            + " VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(personSql)) {
            ps.setInt(1, personId);
            ps.setString(2, victim.getFirstName());
            ps.setString(3, victim.getLastName());
            ps.setString(4, victim.getComments());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert person row", e);
        }

        String dvSql =
            "INSERT INTO DisasterVictim " +
            "(person_id, date_of_birth, approximate_age, gender, " +
            "entry_date, location_id, is_soft_deleted) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(dvSql)) {
            ps.setInt(1, personId);
            if (victim.getDateOfBirth() != null) {
                ps.setDate(2, java.sql.Date.valueOf(victim.getDateOfBirth()));
            } else {
                ps.setNull(2, Types.DATE);
            }
            if (victim.getApproximateAge() != null) {
                ps.setInt(3, victim.getApproximateAge());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, victim.getGender());
            ps.setDate(5, java.sql.Date.valueOf(victim.getEntryDate()));
            if (victim.getLocation() != null) {
                ps.setInt(6, victim.getLocation().getDbId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setBoolean(7, victim.isSoftDeleted());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to insert disaster victim row", e);
        }

        try (Statement st = conn.createStatement()) {
            st.execute("SELECT setval('person_id_seq', " +
                "(SELECT COALESCE(MAX(id), 1) FROM Person))");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to sync person_id_seq", e);
        }

        return personId;
    }

    /**
     * Updates an existing disaster victim's {@code Person} and
     * {@code DisasterVictim} rows with current field values.
     *
     * @param victim the {@link DisasterVictim} with updated values
     * @throws RuntimeException if the update fails
     */
    @Override
    public void updateDisasterVictim(DisasterVictim victim) {
        try {
            String personSql =
                "UPDATE Person SET first_name=?, "
                + "last_name=?, comments=? WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(personSql)) {
                ps.setString(1, victim.getFirstName());
                ps.setString(2, victim.getLastName());
                ps.setString(3, victim.getComments());
                ps.setInt(4, victim.getDbId());
                ps.executeUpdate();
            }
            String dvSql =
                "UPDATE DisasterVictim SET date_of_birth=?, " +
                "approximate_age=?, gender=?, location_id=?, " +
                "is_soft_deleted=? WHERE person_id=?";
            try (PreparedStatement ps = conn.prepareStatement(dvSql)) {
                if (victim.getDateOfBirth() != null) {
                    ps.setDate(1,
                        java.sql.Date.valueOf(victim.getDateOfBirth()));
                } else {
                    ps.setNull(1, Types.DATE);
                }
                if (victim.getApproximateAge() != null) {
                    ps.setInt(2, victim.getApproximateAge());
                } else {
                    ps.setNull(2, Types.INTEGER);
                }
                ps.setString(3, victim.getGender());
                if (victim.getLocation() != null) {
                    ps.setInt(4, victim.getLocation().getDbId());
                } else {
                    ps.setNull(4, Types.INTEGER);
                }
                ps.setBoolean(5, victim.isSoftDeleted());
                ps.setInt(6, victim.getDbId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update disaster victim", e);
        }
    }

    /**
     * Sets or clears the {@code is_soft_deleted} flag for a victim.
     *
     * @param victimDbId  the person ID of the victim
     * @param softDeleted {@code true} to archive, {@code false} to restore
     * @throws RuntimeException if the update fails
     */
    @Override
    public void setSoftDeleted(int victimDbId, boolean softDeleted) {
        String sql =
            "UPDATE DisasterVictim SET is_soft_deleted=? WHERE person_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, softDeleted);
            ps.setInt(2, victimDbId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set soft delete flag", e);
        }
    }

    /**
     * Permanently deletes a disaster victim and their associated supplies and
     * inquiries by removing the {@code Person} row (which cascades to
     * {@code DisasterVictim}).
     *
     * @param victimDbId the person ID of the victim to delete
     * @throws RuntimeException if any delete step fails
     */
    @Override
    public void hardDeleteDisasterVictim(int victimDbId) {
        String deleteSupplies = "DELETE FROM Supply WHERE victim_id=?";
        try (PreparedStatement ps = conn.prepareStatement(deleteSupplies)) {
            ps.setInt(1, victimDbId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to delete supplies for victim during hard delete", e);
        }

        String deleteInquiries =
            "DELETE FROM Inquiry WHERE subject_person_id=? OR inquirer_id=?";
        try (PreparedStatement ps = conn.prepareStatement(deleteInquiries)) {
            ps.setInt(1, victimDbId);
            ps.setInt(2, victimDbId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to delete inquiries for victim during hard delete", e);
        }

        String sql = "DELETE FROM Person WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, victimDbId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to hard delete disaster victim", e);
        }

        try (Statement st = conn.createStatement()) {
            st.execute("SELECT setval('person_id_seq', " +
                "(SELECT COALESCE(MAX(id), 1) FROM Person))");
        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to sync person_id_seq after hard delete", e);
        }
    }

    /**
     * Inserts a new supply record into the {@code Supply} table.
     *
     * @param supply     the {@link Supply} to persist
     * @param locationId the database ID of the holding location
     * @return the database-assigned supply ID
     * @throws RuntimeException if the insert fails
     */
    @Override
    public int insertSupply(Supply supply, int locationId) {
        String sql =
            "INSERT INTO Supply (supply_type, location_id, " +
            "expiry_date, description) VALUES (?,?,?,?) RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, supply.getType());
            if (locationId > 0) {
                ps.setInt(2, locationId);
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            if (supply.getExpiryDate() != null) {
                ps.setDate(3, java.sql.Date.valueOf(supply.getExpiryDate()));
            } else {
                ps.setNull(3, Types.DATE);
            }
            ps.setString(4, supply.getDescription());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert supply", e);
        }
    }

    /**
     * Allocates a supply to a victim by setting {@code victim_id} and
     * {@code allocation_date} in the {@code Supply} table.
     *
     * @param supplyDbId the database ID of the supply
     * @param victimDbId the database ID of the victim
     * @throws RuntimeException if the update fails
     */
    @Override
    public void allocateSupplyToVictim(int supplyDbId, int victimDbId) {
        String sql =
            "UPDATE Supply SET victim_id=?, "
            + "allocation_date=CURRENT_DATE WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, victimDbId);
            ps.setInt(2, supplyDbId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to allocate supply to victim", e);
        }
    }

    /**
     * Inserts a new medical record into the {@code MedicalRecord} table.
     *
     * @param record     the {@link MedicalRecord} to persist
     * @param victimDbId the database ID of the victim
     * @return the database-assigned medical record ID
     * @throws RuntimeException if the insert fails
     */
    @Override
    public int insertMedicalRecord(MedicalRecord record, int victimDbId) {
        String sql =
            "INSERT INTO MedicalRecord (victim_id, treatment_details, " +
            "treatment_date, location_id) VALUES (?,?,?,?) RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, victimDbId);
            ps.setString(2, record.getTreatmentDetails());
            ps.setDate(3, Date.valueOf(record.getDateOfTreatment()));
            ps.setInt(4, record.getLocation().getDbId());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert medical record", e);
        }
    }

    /**
     * Inserts a new family relationship into the {@code FamilyRelationship}
     * table.
     *
     * @param relation the {@link FamilyRelation} to persist
     * @return the database-assigned relationship ID
     * @throws RuntimeException if the insert fails
     */
    @Override
    public int insertFamilyRelation(FamilyRelation relation) {
        String sql =
            "INSERT INTO FamilyRelationship (person_one_id, " +
            "person_two_id, relationship_type) VALUES (?,?,?) RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, relation.getPersonOne().getDbId());
            ps.setInt(2, relation.getPersonTwo().getDbId());
            ps.setString(3, relation.getRelationshipTo());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert family relation", e);
        }
    }

    /**
     * Deletes a family relationship from the {@code FamilyRelationship} table.
     *
     * @param relationDbId the database ID of the relationship to delete
     * @throws RuntimeException if the delete fails
     */
    @Override
    public void deleteFamilyRelation(int relationDbId) {
        String sql = "DELETE FROM FamilyRelationship WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, relationDbId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete family relation", e);
        }
    }

    /**
     * Inserts a new inquirer as a {@code Person} row in the database, using
     * the lowest available person ID.
     *
     * @param inquirer the {@link Inquirer} to persist
     * @return the database-assigned person ID
     * @throws RuntimeException if any insert or ID-lookup fails
     */
    @Override
    public int insertInquirer(Inquirer inquirer) {
        int personId;
        String nextIdSql =
            "SELECT s.id FROM generate_series(1, " +
            "(SELECT COALESCE(MAX(id), 0) + 1 FROM Person)) AS s(id) " +
            "LEFT JOIN Person p ON p.id = s.id " +
            "WHERE p.id IS NULL ORDER BY s.id LIMIT 1";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(nextIdSql)) {
            rs.next();
            personId = rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to determine next available person ID for inquirer", e);
        }

        String sql =
            "INSERT INTO Person (id, first_name, last_name, comments) " +
            "VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, personId);
            ps.setString(2, inquirer.getFirstName());
            ps.setString(3, inquirer.getLastName());
            ps.setString(4, inquirer.getInfo());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert inquirer", e);
        }

        try (Statement st = conn.createStatement()) {
            st.execute("SELECT setval('person_id_seq', " +
                "(SELECT COALESCE(MAX(id), 1) FROM Person))");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to sync person_id_seq", e);
        }

        return personId;
    }

    /**
     * Inserts a new inquiry into the {@code Inquiry} table.
     *
     * @param inquiry    the {@link ReliefService} inquiry to persist
     * @param inquirerId the database ID of the inquirer
     * @return the database-assigned inquiry ID
     * @throws RuntimeException if the insert fails
     */
    @Override
    public int insertInquiry(ReliefService inquiry, int inquirerId) {
        String sql =
            "INSERT INTO Inquiry (inquirer_id, subject_person_id, " +
            "details) VALUES (?,?,?) RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inquirerId);
            DisasterVictim subject = inquiry.getMissingPerson();
            if (subject != null) {
                ps.setInt(2, subject.getDbId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, inquiry.getInfoProvided());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert inquiry", e);
        }
    }

    /**
     * Inserts a new cultural requirement into the {@code CulturalRequirement}
     * table.
     *
     * @param req        the {@link CulturalRequirement} to persist
     * @param victimDbId the database ID of the victim
     * @return the database-assigned requirement ID
     * @throws RuntimeException if the insert fails
     */
    @Override
    public int insertCulturalRequirement(
            CulturalRequirement req, int victimDbId) {
        String sql =
            "INSERT INTO CulturalRequirement (victim_id, " +
            "requirement_category, requirement_option) "
            + "VALUES (?,?,?) RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, victimDbId);
            ps.setString(2, req.getCategory());
            ps.setString(3, req.getOption());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to insert cultural requirement", e);
        }
    }

    /**
     * Updates the selected option of an existing cultural requirement.
     *
     * @param req the {@link CulturalRequirement} with the updated option
     * @throws RuntimeException if the update fails
     */
    @Override
    public void updateCulturalRequirement(CulturalRequirement req) {
        String sql =
            "UPDATE CulturalRequirement SET requirement_option=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, req.getOption());
            ps.setInt(2, req.getDbId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to update cultural requirement", e);
        }
    }

    /**
     * Deletes a cultural requirement from the database.
     *
     * @param reqDbId the database ID of the requirement to delete
     * @throws RuntimeException if the delete fails
     */
    @Override
    public void deleteCulturalRequirement(int reqDbId) {
        String sql = "DELETE FROM CulturalRequirement WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reqDbId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to delete cultural requirement", e);
        }
    }

    /**
     * Inserts a skill assignment for a victim. Upserts the skill definition in
     * {@code Skill} if needed, then inserts a {@code VictimSkill} row.
     *
     * @param skill      the {@link Skill} to persist
     * @param victimDbId the database ID of the victim
     * @return the database-assigned VictimSkill row ID
     * @throws RuntimeException if any insert or lookup fails
     */
    @Override
    public int insertVictimSkill(Skill skill, int victimDbId) {
        try {
            String upsert =
                "INSERT INTO Skill (skill_name, category) VALUES (?,?) " +
                "ON CONFLICT (skill_name, category) DO NOTHING";
            try (PreparedStatement ps = conn.prepareStatement(upsert)) {
                ps.setString(1, skill.getSkillName());
                ps.setString(2, skill.getCategory());
                ps.executeUpdate();
            }

            int skillId;
            String selectSkill =
                "SELECT id FROM Skill WHERE skill_name=? AND category=?";
            try (PreparedStatement ps = conn.prepareStatement(selectSkill)) {
                ps.setString(1, skill.getSkillName());
                ps.setString(2, skill.getCategory());
                ResultSet rs = ps.executeQuery();
                rs.next();
                skillId = rs.getInt(1);
            }

            String vsSql =
                "INSERT INTO VictimSkill (victim_Id, skill_id, " +
                "details, language_capabilities, certification_expiry, " +
                "proficiency_level) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
            try (PreparedStatement ps = conn.prepareStatement(vsSql)) {
                ps.setInt(1, victimDbId);
                ps.setInt(2, skillId);
                if (skill instanceof MedicalSkill ms) {
                    ps.setString(3, ms.getCertType());
                    ps.setNull(4, Types.VARCHAR);
                    ps.setDate(5, Date.valueOf(ms.getCertificationExpiry()));
                } else if (skill instanceof LanguageSkill ls) {
                    ps.setNull(3, Types.VARCHAR);
                    ps.setString(4, ls.getCapabilitiesAsString());
                    ps.setNull(5, Types.DATE);
                } else {
                    ps.setNull(3, Types.VARCHAR);
                    ps.setNull(4, Types.VARCHAR);
                    ps.setNull(5, Types.DATE);
                }
                ps.setString(6, skill.getProficiencyLevel());
                ResultSet rs = ps.executeQuery();
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert victim skill", e);
        }
    }

    /**
     * Deletes a victim-skill association row from the
     * {@code VictimSkill} table.
     *
     * @param victimSkillDbId the database ID of the VictimSkill row to delete
     * @throws RuntimeException if the delete fails
     */
    @Override
    public void deleteVictimSkill(int victimSkillDbId) {
        String sql = "DELETE FROM VictimSkill WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, victimSkillDbId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete victim skill", e);
        }
    }

    /**
     * Finds an {@link Inquirer} in the given list by database ID.
     *
     * @param list the list to search
     * @param id   the database ID to match
     * @return the matching {@link Inquirer}, or {@code null} if not found
     */
    private Inquirer findById(List<Inquirer> list, int id) {
        for (Inquirer i : list) {
            if (i.getDbId() == id) {
                return i;
            }
        }
        return null;
    }

    /**
     * Finds a {@link DisasterVictim} in the given list by database ID.
     *
     * @param list the list to search
     * @param id   the database ID to match
     * @return the matching {@link DisasterVictim}, or {@code null} if not found
     */
    private DisasterVictim findVictimById(List<DisasterVictim> list, int id) {
        for (DisasterVictim v : list) {
            if (v.getDbId() == id) {
                return v;
            }
        }
        return null;
    }

    /**
     * Finds a {@link Location} in the given list by database ID.
     *
     * @param list the list to search
     * @param id   the database ID to match
     * @return the matching {@link Location}, or {@code null} if not found
     */
    private Location findLocationById(List<Location> list, int id) {
        for (Location l : list) {
            if (l.getDbId() == id) {
                return l;
            }
        }
        return null;
    }

    /**
     * Attaches a cultural requirement to a victim using the victim's own
     * {@code setCulturalRequirement} method and then copies the database ID
     * back onto the stored object. Used during DB loading to avoid triggering
     * duplicate-category validation.
     *
     * @param v   the victim to attach the requirement to
     * @param req the requirement to attach
     */
    private void addCulturalReqDirectly(DisasterVictim v,
                                         CulturalRequirement req) {
        v.setCulturalRequirement(req.getCategory(), req.getOption());
        for (CulturalRequirement r : v.getCulturalRequirements()) {
            if (r.getCategory().equalsIgnoreCase(req.getCategory())) {
                r.setDbId(req.getDbId());
                break;
            }
        }
    }

    /**
     * Inserts a new location record into the database.
     *
     * @param location the location to insert
     * @return the generated database ID
     * @throws RuntimeException if the insert operation fails
     */
    @Override
    public int insertLocation(Location location) {
        String sql =
            "INSERT INTO Location (name, address) VALUES (?, ?) RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, location.getName());
            ps.setString(2, location.getAddress());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert location", e);
        }
    }

    /**
     * Updates an existing location record in the database.
     *
     * @param location the location with updated values
     * @throws RuntimeException if the update operation fails
     */
    @Override
    public void updateLocation(Location location) {
        String sql = "UPDATE Location SET name=?, address=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, location.getName());
            ps.setString(2, location.getAddress());
            ps.setInt(3, location.getDbId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update location", e);
        }
    }

    /**
     * Updates an existing supply record in the database.
     *
     * @param supply the supply with updated values
     * @param locationId the location ID, or {@code null}
     * @param victimId the victim ID, or {@code null}
     * @throws RuntimeException if the update operation fails
     */
    @Override
    public void updateSupply(Supply supply,
            Integer locationId, Integer victimId) {
        String sql =
            "UPDATE Supply SET supply_type=?, location_id=?, victim_id=?, "
            + "expiry_date=?, allocation_date=?, description=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, supply.getType());

            if (locationId != null) {
                ps.setInt(2, locationId);
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            if (victimId != null) {
                ps.setInt(3, victimId);
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            if (supply.getExpiryDate() != null) {
                ps.setDate(4, Date.valueOf(supply.getExpiryDate()));
            } else {
                ps.setNull(4, Types.DATE);
            }

            if (supply.getAllocationDate() != null) {
                ps.setDate(5, Date.valueOf(supply.getAllocationDate()));
            } else {
                ps.setNull(5, Types.DATE);
            }

            ps.setString(6, supply.getDescription());
            ps.setInt(7, supply.getDbId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update supply", e);
        }
    }

    /**
     * Updates an existing inquiry in the {@code Inquiry} table.
     * The {@code inquiry_date} column is a {@code TIMESTAMP}; the stored
     * {@link LocalDate} is converted to midnight of that date before writing.
     *
     * @param inquiry the inquiry object containing the updated values
     * @throws RuntimeException if the update fails
     */
    @Override
    public void updateInquiry(ReliefService inquiry) {
        String sql =
            "UPDATE Inquiry SET inquirer_id = ?, subject_person_id = ?, "
            + "inquiry_date = ?, details = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inquiry.getInquirer().getDbId());

            if (inquiry.getMissingPerson() != null) {
                ps.setInt(2, inquiry.getMissingPerson().getDbId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            ps.setTimestamp(3, java.sql.Timestamp.valueOf(
                inquiry.getDateOfInquiry().atStartOfDay()));
            ps.setString(4, inquiry.getInfoProvided());
            ps.setInt(5, inquiry.getDbId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update inquiry", e);
        }
    }

    /**
     * Updates an existing medical record in the database.
     *
     * @param record the {@link MedicalRecord} with updated field values
     * @throws RuntimeException if the update fails
     */
    @Override
    public void updateMedicalRecord(MedicalRecord record) {
        String sql =
            "UPDATE MedicalRecord SET location_id = ?, "
            + "treatment_details = ?, treatment_date = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, record.getLocation().getDbId());
            ps.setString(2, record.getTreatmentDetails());
            ps.setDate(3, Date.valueOf(record.getDateOfTreatment()));
            ps.setInt(4, record.getDbId());

            int rows = ps.executeUpdate();
            if (rows != 1) {
                throw new RuntimeException(
                    "Expected to update 1 medical record row, updated " + rows);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update medical record", e);
        }
    }

    /**
     * Updates an existing family relationship in the database.
     *
     * @param relation the {@link FamilyRelation} with updated field values
     * @throws RuntimeException if the update fails
     */
    @Override
    public void updateFamilyRelation(FamilyRelation relation) {
        String sql =
            "UPDATE FamilyRelationship SET person_one_id = ?, " +
            "relationship_type = ?, person_two_id = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, relation.getPersonOne().getDbId());
            ps.setString(2, relation.getRelationshipTo());
            ps.setInt(3, relation.getPersonTwo().getDbId());
            ps.setInt(4, relation.getDbId());

            int rows = ps.executeUpdate();
            if (rows != 1) {
                throw new RuntimeException(
                    "Expected to update 1 family relationship row, updated "
                    + rows);
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to update family relationship", e);
        }
    }
}