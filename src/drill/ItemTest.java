package drill;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

/*
 * Created on Jan 28, 2007
 */

/**
 * @author David Matuszek
 * @version March 2, 2015
 */
public class ItemTest {
    Item item0, item1, item2, item3, item4, item5;
    ItemList itemList = new ItemList();
    double difficulty = itemList.getDifficulty();
    /**
     * @throws java.lang.Exception
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        item1 = new Item("one", "eins");
        item2 = new Item("two", "zwei");
        // stimulus, response, timesCorrect, timesIncorrect, interval, displayDate
        item3 = new Item("three", "drei", 3, 7, 5, 23);
        item4 = new Item("four", "vier", 3, 7, 7, 33);
        item5 = new Item("five", "funf", 3, 7, 9, 33);

    }

    /**
     * Test method for {@link Item#Item(java.lang.String, java.lang.String)}.<br>
     * Testing new Item(stimulus, response).
     */
    @Test
    public final void testItemStringString() {
        Item item = new Item("ten", "zehn");
        assertEquals("ten", item.getStimulus());
        assertEquals("zehn", item.getResponse());
        assertEquals(Integer.MAX_VALUE, item.getDisplayDate());
        assertTrue(item.isVirgin());
        assertEquals(0, item.getTimesCorrect());
        assertEquals(0, item.getTimesIncorrect());
        assertEquals(Item.minInterval, item.getInterval());
        assertEquals(Integer.MAX_VALUE, item.getDisplayDate());
    }

    /**
     * Test method for {@link Item#Item(java.lang.String, java.lang.String, int, int, int)}.
     * Testing new Item(stimulus, response, timesCorrect, interval, displayDate).
     */
    @Test
    public final void testItemStringStringIntIntIntInt() {
        Item item = new Item("eleven", "elf", 8, 1, 9, 10);
        checkAllFieldsOfItem(item);
    }

    /**
     * Test method for {@link Item#modify()}.
     */
    @Test
    public final void testModifyStringStringIntIntInt() {
        Item item = new Item("foo", "bar", 666, 666, 666, 666, 666);
        assertTrue(item.modify("eleven", "elf", 8, 1, 3, 9, 10));
        assertFalse(item.modify("eleven", "elf", 8, 1, 3, 9, 10));
        checkAllFieldsOfItem(item);
    }
    
    private void checkAllFieldsOfItem(Item item) {
     // stimulus, response, timesCorrect, timesIncorrect, interval, displayDate
        assertEquals("eleven", item.getStimulus());
        assertEquals("elf", item.getResponse());
        assertFalse(item.isVirgin());
        assertEquals(8, item.getTimesCorrect());
        assertEquals(1, item.getTimesIncorrect());
        assertEquals(9, item.getInterval());
        assertEquals(10, item.getDisplayDate());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public final void checkItemWithBlankStimulus() {
        new Item("", "y", 0, 5, 7, Integer.MAX_VALUE);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public final void checkItemWithBlankResponse() {
        new Item("y", "", 0, 5, 7, Integer.MAX_VALUE);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public final void checkItemWithNegativeTimesCorrect() {
        new Item("x", "", -1, 5, 7, Integer.MAX_VALUE);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public final void checkItemWithNegativeTimesIncorrect() {
        new Item("x", "", 0, -5, 7, Integer.MAX_VALUE);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public final void checkItemWithNegativeInterval() {
        new Item("x", "", 0, 5, -7, Integer.MAX_VALUE);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public final void checkItemWithNegativeDisplayDate() {
        new Item("x", "", 0, 5, 7, -1);
    }

    /**
     * Test method for {@link Item#promote()}.
     */
    @Test
    public final void testPromote() {
        // Powers of 2.5, truncated: 2, 6, 15, 39, 97, 244, 610, 1525, 3814
        int interval;
        
        Item virgin = new Item("ten", "zehn");
        assertTrue(virgin.isVirgin());

        virgin.promote();
        assertFalse(virgin.isVirgin());
        int level = itemList.virginPromotion;
        assertEquals(level, virgin.getTimesCorrect());
        interval = virgin.getInterval();
        assertEquals(ItemList.intervalForLevel(level, difficulty), interval);
        assertEquals(Time.now + interval, virgin.getDisplayDate());

        virgin.promote();
        level += 1;
        assertFalse(virgin.isVirgin());
        assertEquals(level, virgin.getTimesCorrect());
        interval = virgin.getInterval();
        assertEquals(ItemList.intervalForLevel(level, difficulty), interval);
        assertEquals(Time.now + interval, virgin.getDisplayDate());
        
        virgin.promote();
        level += 1;
        assertEquals(level, virgin.getTimesCorrect());
        interval = virgin.getInterval();
        assertEquals(ItemList.intervalForLevel(level, difficulty), interval);
        assertEquals(Time.now + interval, virgin.getDisplayDate());

        virgin.promote();
        level += 1;
        assertEquals(level, virgin.getTimesCorrect());
        interval = virgin.getInterval();
        assertEquals(ItemList.intervalForLevel(level, difficulty), interval);
        assertEquals(Time.now + interval, virgin.getDisplayDate());
    }

    /**
     * Test method for {@link Item#demote()}.
     */
    @Test
    public final void testDemote() {
        int actualInterval, computedInterval;
        
        Item virgin = new Item("ten", "zehn");
        assertTrue(virgin.isVirgin());

        virgin.demote();
        assertFalse(virgin.isVirgin());
        assertEquals(0, virgin.getTimesCorrect());
        assertEquals(1, virgin.getTimesIncorrect());
        assertEquals(0, virgin.getConsecutiveTimesCorrect());
        actualInterval = virgin.getInterval();
        computedInterval = ItemList.intervalForLevel(virgin.getLevel(), difficulty);
        assertEquals(computedInterval, actualInterval);
        assertEquals(Time.now + actualInterval, virgin.getDisplayDate());
        
        virgin.demote();
        assertFalse(virgin.isVirgin());
        assertEquals(0, virgin.getTimesCorrect());
        assertEquals(2, virgin.getTimesIncorrect());
        actualInterval = virgin.getInterval();
        computedInterval = ItemList.intervalForLevel(-2, difficulty);
        assertEquals(computedInterval, actualInterval);
        assertEquals(Time.now + actualInterval, virgin.getDisplayDate());        
        virgin.demote();
        
        assertEquals(0, virgin.getTimesCorrect());
        assertEquals(3, virgin.getTimesIncorrect());
        actualInterval = virgin.getInterval();
        computedInterval = ItemList.intervalForLevel(-3, difficulty);
        assertEquals(computedInterval, actualInterval);
        assertEquals(Time.now + actualInterval, virgin.getDisplayDate());
    }

    /**
     * Test method for {@link Item#promote() and {@link Item#demote()}.
     */
    @Test
    public final void testPromotionAndDemotion() {
        int interval;
        
        Item virgin = new Item("ten", "zehn");
        assertTrue(virgin.isVirgin());

        virgin.promote();
        int basis = itemList.virginPromotion;
        assertEquals(basis, virgin.getTimesCorrect());
        virgin.promote();
        virgin.promote();        
        virgin.promote();
        assertEquals(basis + 3, virgin.getTimesCorrect());
        interval = virgin.getInterval();
        
        virgin.demote();
        virgin.demote();
        assertFalse(virgin.isVirgin());
        Item nonvirgin = new Item("xxx", "yyy", 1, 1, 2, 10);
        assertEquals(1, nonvirgin.getTimesCorrect());
        assertEquals(1, nonvirgin.getTimesIncorrect());
        
        nonvirgin.promote();
        assertEquals(2, nonvirgin.getTimesCorrect());
    }

    /**
     * Test method for {@link Item#getStimulus()}.
     */
    @Test
    public final void testGetStimulus() {
        Item item = new Item("ten", "zehn");
        assertEquals("ten", item.getStimulus());
    }

    /**
     * Test method for {@link Item#getResponse()}.
     */
    @Test
    public final void testGetResponse() {
        Item item = new Item("ten", "zehn");
        assertEquals("zehn", item.getResponse());
    }

    /**
     * Test method for {@link Item#getTimesCorrect()} and {@link Item#getTimesIncorrect()}.
     */
    @Test
    public final void testGetTimesCorrectAndIncorrect() {
        Item item = new Item("ten", "zehn");
        assertEquals(0, item.getTimesCorrect());
        assertEquals(0, item.getTimesIncorrect());
        item.promote();
        assertEquals(ItemList.virginPromotion, item.getTimesCorrect());
        assertEquals(0, item.getTimesIncorrect());
        item.demote();
        item.promote();
        assertEquals(ItemList.virginPromotion + 1, item.getTimesCorrect());
        assertEquals(1, item.getTimesIncorrect());
        item.demote();
        assertEquals(ItemList.virginPromotion + 1, item.getTimesCorrect());
        assertEquals(2, item.getTimesIncorrect());
    }
    
    @Test
    public final void testGetLevel() {
        Item item = new Item("ten", "zehn");
        assertEquals(0, item.getLevel());
        item.promote();
        assertEquals(ItemList.virginPromotion, item.getLevel());
        item.demote();
        item.promote();
        assertEquals(1, item.getLevel());
        item.demote();
        assertEquals(0, item.getLevel());
    }
    

    @Test
    public final void testResponseIsCorrect() {
        Item item = new Item("ten", "zehn");
        assertTrue(item.responseIsCorrect("zehn"));
        assertFalse(item.responseIsCorrect("  "));
        
        item = new Item("ten", "zehn | 10");
        assertTrue(item.responseIsCorrect("zehn | 10"));
        assertTrue(item.responseIsCorrect("zehn"));
        assertTrue(item.responseIsCorrect("10"));
        assertFalse(item.responseIsCorrect("100"));
        
        item = new Item("ten", "zehn | 10 | juu");
        assertTrue(item.responseIsCorrect("zehn | 10 | juu "));
        assertTrue(item.responseIsCorrect("zehn"));
        assertTrue(item.responseIsCorrect("10"));
        assertTrue(item.responseIsCorrect("juu"));
        assertFalse(item.responseIsCorrect("100"));
        
        item = new Item("ten", "zehn, 10, juu");
        assertTrue(item.responseIsCorrect("zehn, 10, juu"));
        assertTrue(item.responseIsCorrect("zehn"));
        assertTrue(item.responseIsCorrect("10"));
        assertTrue(item.responseIsCorrect("juu"));
        assertFalse(item.responseIsCorrect("100"));
        
        item = new Item("ten", "zehn; 10 ; juu");
        assertTrue(item.responseIsCorrect("  zehn; 10 ; juu  "));
        assertTrue(item.responseIsCorrect("zehn"));
        assertTrue(item.responseIsCorrect("10"));
        assertTrue(item.responseIsCorrect("juu"));
        assertFalse(item.responseIsCorrect("100"));
    }
    
    @Test
    public final void testResponseIsCorrectAfterParenthesisRemoval() {
        Item item = new Item("ten", "zehn (10)");
        assertTrue(item.responseIsCorrect("zehn"));
        
        item = new Item("ten", "zehn (X in Roman numerals) | 10 (X)");
        assertTrue(item.responseIsCorrect("zehn"));
        assertTrue(item.responseIsCorrect("10"));
        assertTrue(item.responseIsCorrect("zehn | 10"));
        assertTrue(item.responseIsCorrect("10 | zehn"));
    }
    
    @Test
    public final void testGetParts() {
        Set<String> set = new HashSet<String>();
        set.add("foo");
        assertEquals(set, Item.getParts("foo"));
        set.add("bar");
        assertEquals(set, Item.getParts("foo; bar"));
        set.add("baz");
        assertEquals(set, Item.getParts(" baz ; foo; bar"));
        set.add("one two three");
        assertEquals(set, Item.getParts(" baz ; foo; bar; one two three"));
        set.clear();
        set.add("foo");
        assertEquals(set, Item.getParts(" foo "));
    }
    
    @Test
    public final void testBreakIntoAlternatives() {
        String s = "sogar";
        Set<String> alts = new HashSet<String>();
        alts.add("sogar");
        assertEquals(alts, Item.breakIntoAlternatives(s, " *; *"));
        
        s = "  sogar  ";
        assertEquals(alts, Item.breakIntoAlternatives(s, " *; *"));
        
        s = "to come into being; to arise; to originate";
        alts = new HashSet<String>();
        alts.add("to come into being");
        alts.add("to arise");
        alts.add("to originate");       
    }
    
    @Test
    public final void testSimilarity() {
        assertFalse(Item.similarity("to volunteer", "to come into being") > 0.90);
        assertFalse(Item.similarity("to volunteer", "to arise") > 0.90);
        assertFalse(Item.similarity("to volunteer", "to originate") > 0.90);
        assertFalse(Item.similarity("entstehen", "bestehen") > 0.90);
        assertTrue(Item.similarity("to volunteer", "to volunteer") > 0.90);
        assertTrue(Item.similarity("to vollunteer", "to volunteer") > 0.90);
    }

    @Test
    public final void testGetUnusedAlternatives() {
        Item item = new Item("ten", "zehn");
        assertTrue(item.responseIsCorrect("zehn"));
        
        item = new Item("ten", "zehn | 10");
        assertTrue(item.responseIsCorrect("zehn | 10"));
        assertTrue(item.responseIsCorrect("zehn"));
        assertTrue(item.responseIsCorrect("10"));
        assertFalse(item.responseIsCorrect("100"));
        
        item = new Item("seven", "sieben (vii) | 7 (VII)");
        assertTrue(item.responseIsCorrect("sieben (vii) | 7 (VII)"));
        assertTrue(item.responseIsCorrect("sieben"));
        assertTrue(item.responseIsCorrect("7"));
        assertFalse(item.responseIsCorrect("100"));
    }
    
    @Test
    public final void testRemoveParenthesizedGroups() {
        String before = "Nothing to remove";
        String after = before;
        assertEquals(after, Item.removeParenthesizedGroups(before));
        before = "you (singular)";
        after = "you";
        assertEquals(after, Item.removeParenthesizedGroups(before));
        before = "you (singular) | you (plural)";
        after = "you | you";
        assertEquals(after, Item.removeParenthesizedGroups(before));
    }

    /**
     * Test method for {@link Item#isVirgin()}.
     */
    @Test
    public final void testIsVirgin() {
        Item item1 = new Item("a", "a");
        assertTrue(item1.isVirgin());
        item1.promote();
        assertFalse(item1.isVirgin());
    }

    /**
     * Test method for {@link Item#setVirgin(boolean)}.
     */
    @Test
    public final void testSetVirgin() {
        Item item1 = new Item("a", "a");
        assertTrue(item1.isVirgin());
        item1.setVirgin(false);
        assertFalse(item1.isVirgin());
        item1.setVirgin(true);
        assertTrue(item1.isVirgin());
    }

    /**
     * Test method for {@link Item#getInterval()}.
     */
    @Test
    public final void testGetInterval() {
        // stimulus, response, timesCorrect, interval, displayDate
        Item item = new Item("a", "a", 1, 1, 80, 100);
        assertEquals(80, item.getInterval());
    }

    /**
     * Test method for {@link Item#getDisplayDate()}.
     */
    @Test
    public final void testGetDisplayDate() {
        // stimulus, response, timesCorrect, interval, displayDate
        Item item = new Item("a", "a", 1, 1, 80, 100);
        assertEquals(100, item.getDisplayDate());
    }

    /**
     * Test method for {@link Item#compareTo(Item)}.
     */
    @Test
    public final void testCompareTo() {
        // stimulus, response, timesCorrect, interval, displayDate
        Item soon = new Item("a", "a", 3, 1, 80, 2);
        Item soon2 = new Item("b", "b", 3, 1, 80, 2);
        Item late = new Item("c", "c", 3, 1, 80, 100);
        assertTrue(soon.compareTo(late) < 0);
        assertTrue(soon.compareTo(soon2) == 0);
        assertTrue(late.compareTo(soon) > 0);
    }

    @Test
    public final void testToString() {

        // stimulus, response, timesCorrect, timesIncorrect, interval, displayDate
        item3 = new Item("three", "drei", 3, 7, 5, 23, 99);
        String expected = "three || drei || 3 || 7 || 5 || 23 || 99";
        assertEquals(expected, item3.toString());
    }

    @Test
    public final void testEquals() {
        assertFalse(item1.equals("one"));
        assertFalse(item1.equals(item2));
        assertTrue(item1.equals(item1));
        assertEquals(new Item("one", "eins"), item1);
        assertEquals(new Item("three", "drei"), item3);
        assertEquals(new Item("three", "drei", 99, 99, 99, 99), item3);
        assertFalse(item3.equals(new Item("a", "b",  99, 7, 5, 23)));
        Item bar = new Item("foo", "bar");
        Item baz = new Item("foo", "baz");
        assertFalse(bar.equals(baz));
    }

    @Test
    public final void testAddMacrons() {
        //  Aa=\u0100\u0101  Ee=\u0112\u0113  Oo=\u014c\u014d  Uu=\u016a\u016b
        String expected, actual;

        expected = "aeio uAEIO U\u0100\u0101\u0112\u0113iiII\u014c\u014d\u016a\u016b";
        actual = Japanese.addMacrons("aeio uAEIO UAAaaEEeeiiIIOOooUUuu");
        assertEquals(expected, actual);
        
        expected = "\u014c \u014c \u014d \u014d";
        actual = Japanese.addMacrons("OO OU oo ou");
        assertEquals(expected, actual);
        
        expected = "\u0100\u0112Ii\u014c\u016a";
        actual = Japanese.addMacrons("AaEeIiOoUu");
        assertEquals(expected, actual);
    }
}
