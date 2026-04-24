package edu.ucalgary.oop;

import org.junit.*;
import static org.junit.Assert.*;
import java.time.LocalDate;

/**
 * Unit tests for {@link Supply}.
 */
public class SupplyTest {

    @Test
    public void testNewSupplyIsNotPerishableByDefault() {
        Supply supply = new Supply("blanket", 1);
        assertFalse("New supply should not be perishable by default",
            supply.isPerishable());
    }

    @Test
    public void testNewSupplyQuantitySet() {
        Supply supply = new Supply("blanket", 5);
        assertEquals("Quantity should be set by constructor",
            5, supply.getQuantity());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewSupplyNegativeQuantityThrows() {
        new Supply("blanket", -1);
    }

    @Test
    public void testDbLoadedSupplyWithExpiryIsPerishable() {
        Supply supply = new Supply(1, "water",
            LocalDate.now().plusDays(10), null, "Case of 24");
        assertTrue("Supply loaded with expiry date should be perishable",
            supply.isPerishable());
    }

    @Test
    public void testDbLoadedSupplyWithoutExpiryIsNotPerishable() {
        Supply supply = new Supply(2, "blanket", null, null, "Wool");
        assertFalse("Supply loaded without expiry should not be perishable",
            supply.isPerishable());
    }

    @Test
    public void testNonPerishableIsNeverExpired() {
        Supply supply = new Supply("blanket", 1);
        assertFalse("Non-perishable supply should never be expired",
            supply.isExpired());
    }

    @Test
    public void testPerishableWithFutureExpiryIsNotExpired() {
        Supply supply = new Supply(1, "water",
            LocalDate.now().plusDays(5), null, null);
        assertFalse("Supply with future expiry should not be expired",
            supply.isExpired());
    }

    @Test
    public void testPerishableWithPastExpiryIsExpired() {
        Supply supply = new Supply(1, "food ration",
            LocalDate.of(2020, 1, 1), null, null);
        assertTrue("Supply with past expiry should be expired",
            supply.isExpired());
    }

    @Test
    public void testSetExpiryDateOnPerishableSucceeds() {
        Supply supply = new Supply("water", 1);
        supply.setPerishable(true);
        LocalDate date = LocalDate.now().plusDays(30);
        supply.setExpiryDate(date);
        assertEquals(date, supply.getExpiryDate());
    }

    @Test(expected = IllegalStateException.class)
    public void testSetExpiryDateOnNonPerishableThrows() {
        Supply supply = new Supply("blanket", 1);
        supply.setExpiryDate(LocalDate.now().plusDays(10));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetExpiryDateNullThrows() {
        Supply supply = new Supply("water", 1);
        supply.setPerishable(true);
        supply.setExpiryDate(null);
    }

    @Test
    public void testSetPerishableFalseClearsExpiryDate() {
        Supply supply = new Supply(1, "water",
            LocalDate.now().plusDays(10), null, null);
        supply.setPerishable(false);
        assertNull("Expiry date should be cleared when set non-perishable",
            supply.getExpiryDate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetQuantityNegativeThrows() {
        Supply supply = new Supply("blanket", 1);
        supply.setQuantity(-5);
    }

    @Test
    public void testSetQuantityZeroSucceeds() {
        Supply supply = new Supply("blanket", 1);
        supply.setQuantity(0);
        assertEquals(0, supply.getQuantity());
    }

    @Test
    public void testToStringContainsType() {
        Supply supply = new Supply("blanket", 1);
        assertTrue("toString should contain supply type",
            supply.toString().contains("blanket"));
    }

    @Test
    public void testToStringContainsExpiredLabel() {
        Supply supply = new Supply(1, "food",
            LocalDate.of(2020, 1, 1), null, null);
        assertTrue("toString should contain EXPIRED label for expired supply",
            supply.toString().contains("EXPIRED"));
    }
}