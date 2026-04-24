package edu.ucalgary.oop;

import org.junit.*;
import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link DisasterReliefController}.
 * Uses {@link MockDatabaseQuery} so no live database is required.
 * Cultural options are injected via
 * {@link DisasterReliefController#setCulturalOptionsForTest}.
 */
public class DisasterReliefControllerTest {

    private DisasterReliefController controller;
    private CulturalOptions testOptions;

    @Before
    public void setUp() {
        MockDatabaseQuery mock = new MockDatabaseQuery();
        try {
            controller = new DisasterReliefController(mock);
        } catch (RuntimeException e) {
            controller = new DisasterReliefController(mock) {
                @Override
                public void loadCulturalOptions() {
                }
            };
        }

        HashMap<String, Set<String>> map = new HashMap<>();
        Set<String> dietary = new HashSet<>();
        dietary.add("halal");
        dietary.add("vegetarian");
        map.put("dietary restrictions", dietary);
        Set<String> safeSpace = new HashSet<>();
        safeSpace.add("LGBTQIA+ affirming");
        map.put("safe-space requirements", safeSpace);
        testOptions = new CulturalOptions(map);
        controller.setCulturalOptionsForTest(testOptions);
    }

    @Test
    public void testGetActiveVictimsExcludesSoftDeleted() {
        List<DisasterVictim> active = controller.getActiveVictims();
        for (DisasterVictim v : active) {
            assertFalse(
                "Active victim list must not contain soft-deleted victims",
                v.isSoftDeleted());
        }
    }

    @Test
    public void testGetActiveVictimsNotEmpty() {
        assertFalse("Active victims should not be empty",
            controller.getActiveVictims().isEmpty());
    }

    @Test
    public void testGetSoftDeletedVictimsContainsSoftDeleted() {
        List<DisasterVictim> deleted = controller.getSoftDeletedVictims();
        for (DisasterVictim v : deleted) {
            assertTrue("Soft-deleted list must only contain archived victims",
                v.isSoftDeleted());
        }
    }

    @Test
    public void testGetLocationsNotEmpty() {
        assertFalse("Locations should be loaded",
            controller.getLocations().isEmpty());
    }

    @Test
    public void testGetExpiredSuppliesOnlyContainsExpired() {
        List<Supply> expired = controller.getExpiredSupplies();
        for (Supply s : expired) {
            assertTrue("Expired supply list must only contain expired items",
                s.isExpired());
        }
    }

    @Test
    public void testGetAllocatableSuppliesExcludesExpired() {
        List<Supply> allocatable = controller.getAllocatableSupplies();
        for (Supply s : allocatable) {
            assertFalse("Allocatable supplies must not be expired",
                s.isExpired());
        }
    }

    @Test
    public void testGetAllocatableSuppliesExcludesAllocated() {
        List<Supply> allocatable = controller.getAllocatableSupplies();
        for (Supply s : allocatable) {
            assertNull(
                "Allocatable supplies must not have an allocation date",
                s.getAllocationDate());
        }
    }

    @Test
    public void testAddVictimAppearsInActiveList() {
        int before = controller.getActiveVictims().size();
        DisasterVictim newVictim =
            new DisasterVictim("Test", LocalDate.of(2025, 2, 1));
        controller.addVictim(newVictim);
        assertEquals("Active victim count should increase by one",
            before + 1, controller.getActiveVictims().size());
    }

    @Test
    public void testAddVictimAssignsDbId() {
        DisasterVictim newVictim =
            new DisasterVictim("IdTest", LocalDate.of(2025, 2, 1));
        controller.addVictim(newVictim);
        assertTrue("DB id should be assigned after add",
            newVictim.getDbId() > 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullVictimThrows() {
        controller.addVictim(null);
    }

    @Test
    public void testSoftDeleteVictimHidesFromActiveList() {
        DisasterVictim victim = controller.getActiveVictims().get(0);
        controller.softDeleteVictim(victim);
        assertFalse("Soft-deleted victim should not appear in active list",
            controller.getActiveVictims().contains(victim));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSoftDeleteNullThrows() {
        controller.softDeleteVictim(null);
    }

    @Test
    public void testHardDeleteVictimRemovedFromAllVictims() {
        DisasterVictim victim = controller.getActiveVictims().get(0);
        controller.hardDeleteVictim(victim);
        assertFalse("Hard-deleted victim should not appear in active list",
            controller.getActiveVictims().contains(victim));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHardDeleteNullThrows() {
        controller.hardDeleteVictim(null);
    }

    @Test
    public void testUpdateApproximateAgeUpdatesVictim() {
        DisasterVictim victim = null;
        for (DisasterVictim v : controller.getActiveVictims()) {
            if (v.getApproximateAge() != null) {
                victim = v;
                break;
            }
        }
        assertNotNull("Test requires a victim with approximate age", victim);
        controller.updateApproximateAge(victim, 10);
        assertEquals("Approximate age should be updated",
            Integer.valueOf(10), victim.getApproximateAge());
    }

    @Test(expected = IllegalStateException.class)
    public void testUpdateApproximateAgeOnDobVictimThrows() {
        DisasterVictim victim = null;
        for (DisasterVictim v : controller.getActiveVictims()) {
            if (v.getDateOfBirth() != null) {
                victim = v;
                break;
            }
        }
        assertNotNull("Test requires a victim with a date of birth", victim);
        controller.updateApproximateAge(victim, 30);
    }

    @Test
    public void testSetDateOfBirthFromAgeUpdatesVictim() {
        DisasterVictim victim = null;
        for (DisasterVictim v : controller.getActiveVictims()) {
            if (v.getApproximateAge() != null) {
                victim = v;
                break;
            }
        }
        assertNotNull("Test requires a victim with approximate age", victim);
        LocalDate dob = LocalDate.of(2018, 3, 1);
        controller.setDateOfBirthFromAge(victim, dob);
        assertEquals("Date of birth should be set", dob,
            victim.getDateOfBirth());
    }

    @Test
    public void testSetCulturalRequirementAddsNewEntry() {
        DisasterVictim victim = controller.getActiveVictims().get(0);
        int before = victim.getCulturalRequirements().size();
        controller.setCulturalRequirement(
            victim, "safe-space requirements", "LGBTQIA+ affirming");
        assertTrue("Cultural requirement count should increase",
            victim.getCulturalRequirements().size() > before);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetCulturalRequirementInvalidOptionThrows() {
        DisasterVictim victim = controller.getActiveVictims().get(0);
        controller.setCulturalRequirement(
            victim, "dietary restrictions", "vegan");
    }

    @Test
    public void testSearchBySkillCategoryExcludesSoftDeleted() {
        DisasterVictim deleted =
            controller.getSoftDeletedVictims().get(0);
        deleted.addSkill(new TradeSkill("carpentry", "beginner"));

        List<DisasterVictim> results =
            controller.searchBySkillCategory("trade");
        assertFalse("Soft-deleted victims should not appear in skill search",
            results.contains(deleted));
    }

    @Test
    public void testSearchBySkillCategoryFindsMatchingVictim() {
        DisasterVictim victim = controller.getActiveVictims().get(0);
        victim.addSkill(new TradeSkill("plumbing", "intermediate"));

        List<DisasterVictim> results =
            controller.searchBySkillCategory("trade");
        assertTrue("Victim with trade skill should appear in results",
            results.contains(victim));
    }

    @Test
    public void testAddSkillAppearsOnVictim() {
        DisasterVictim victim = controller.getActiveVictims().get(0);
        int before = victim.getSkills().size();
        controller.addSkill(victim,
            new TradeSkill("electricity", "advanced"));
        assertEquals("Victim skill count should increase",
            before + 1, victim.getSkills().size());
    }

    @Test
    public void testRemoveSkillRemovedFromVictim() {
        DisasterVictim victim = controller.getActiveVictims().get(0);
        Skill skill = new TradeSkill("carpentry", "beginner");
        controller.addSkill(victim, skill);
        controller.removeSkill(victim, skill);
        assertFalse("Skill should be removed from victim",
            victim.getSkills().contains(skill));
    }

    @Test
    public void testAddMedicalRecordAppearsOnVictim() {
        DisasterVictim victim = controller.getActiveVictims().get(0);
        Location loc = controller.getLocations().get(0);
        int before = victim.getMedicalRecords().size();
        MedicalRecord rec = new MedicalRecord(
            loc, "Treated burn", LocalDate.of(2025, 1, 20));
        controller.addMedicalRecord(rec, victim);
        assertEquals("Medical record count should increase",
            before + 1, victim.getMedicalRecords().size());
    }

    @Test
    public void testAddFamilyRelationAppearsOnBothVictims() {
        List<DisasterVictim> active = controller.getActiveVictims();
        DisasterVictim v1 = active.get(0);
        DisasterVictim v2 = active.get(1);
        FamilyRelation rel = new FamilyRelation(v1, "sibling", v2);
        controller.addFamilyRelation(rel);
        assertTrue("Relation should appear on personOne",
            v1.getFamilyConnections().contains(rel));
        assertTrue("Relation should appear on personTwo",
            v2.getFamilyConnections().contains(rel));
    }

    @Test
    public void testGetVisibleInquiriesExcludesSoftDeletedVictim() {
        DisasterVictim victim = null;
        for (DisasterVictim v : controller.getActiveVictims()) {
            if (!controller.getInquiries().isEmpty()) {
                ReliefService inq = controller.getInquiries().get(0);
                if (inq.getMissingPerson() != null
                        && inq.getMissingPerson().getDbId() == v.getDbId()) {
                    victim = v;
                    break;
                }
            }
        }
        if (victim != null) {
            controller.softDeleteVictim(victim);
            for (ReliefService inq : controller.getVisibleInquiries()) {
                if (inq.getMissingPerson() != null) {
                    assertFalse(
                        "Visible inquiries must not reference "
                        + "soft-deleted victims",
                        inq.getMissingPerson().isSoftDeleted());
                }
            }
        }
    }
}