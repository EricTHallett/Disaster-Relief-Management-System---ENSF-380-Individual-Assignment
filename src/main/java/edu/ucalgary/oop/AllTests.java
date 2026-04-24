package edu.ucalgary.oop;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite that runs all unit tests for the disaster relief
 * management system in a single invocation.
 *
 * @author Eric Hallett (30117108)
 * @version 1.0
 * @since 2026-04-08
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    DisasterVictimTest.class,
    LocationTest.class,
    SupplyTest.class,
    MedicalRecordTest.class,
    FamilyRelationTest.class,
    InquirerTest.class,
    ReliefServiceTest.class,
    SkillTest.class,
    MedicalSkillTest.class,
    LanguageSkillTest.class,
    TradeSkillTest.class,
    CulturalRequirementTest.class,
    CulturalOptionsTest.class,
    DisasterReliefControllerTest.class
})
public class AllTests {
}