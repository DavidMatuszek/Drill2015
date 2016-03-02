package drill;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;

/**
 * @version March 6, 2015
 * @author dave
 *
 */
public class ItemListTest {
    ItemList list;

    @Before
    public void setUp() throws Exception {
        // stimulus, response, timesCorrect, timesIncorrect, interval, displayDate
        String input =
            "zero || null\n"                          // Display second
            + "one || eins\n"
            + "two || zwei || 2 || 4 || 5 || 4\n"     // Display fourth
            + "three || drei\n"
            + "four || vier\n"
            + "five || funf\n"
            + "six || sechs || 0 || 1 || 5 || 1  \n " // Display first
            + "seven || sieben\n"
            + "eight || acht || 5 || 2 || 6 || 3  \n" // Display third
            + "nine || neun\n"
            + "ten || zehn\n";
        createGlobalList(input);
    }

    /**
     * Reads in an ItemList from the given string instead of from a file.
     * @param input The vocabulary list.
     */
    private void createGlobalList(String input) {
        BufferedReader reader = new BufferedReader(new StringReader(input));
        list = new ItemList();
        try {
            list.load(reader);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public final void testSearchForward() {
        assertEquals(1, list.searchForward(0, "one"));
        assertEquals(10, list.searchForward(5, "ten"));
        assertEquals(10, list.searchForward(10, "zehn"));
        assertEquals(7, list.searchForward(1, "eve"));
        assertEquals(4, list.searchForward(8, "four"));
        assertEquals(0, list.searchForward(8, "null"));
        assertEquals(4, list.searchForward(3, "f.*"));
        assertEquals(5, list.searchForward(4, "f.*"));
        assertEquals(5, list.searchForward(4, "f.*"));
        assertEquals(-1, list.searchForward(4, "aardvark"));
    }
    
    @Test
    public final void testSetDifficulty() {
        String input = "\n// difficulty 2\n" +
                "null || zero || 5 || 1 || 666 || 30\n" +
                "eins || one || 5 || 1 || 666 || 60\n" +
                "drei || three || 6 || 1 || 666 || 100\n";
        createGlobalList(input);
        
        // Correct initial interval
        for (Item item : list) {
            int level = item.getLevel();
            item.setInterval(ItemList.intervalForLevel(level, 2));
        }
        // Now perform checks for current item
        for (Item item : list) {
            int level = item.getLevel();
            int date2 = item.getDisplayDate();
            int interval2 = ItemList.intervalForLevel(level, 2);
            list.setDifficulty(list.getDifficulty()); // should have no effect
            assertEquals(interval2, item.getInterval());
            assertEquals(date2, item.getDisplayDate());

            list.setDifficulty(3.0);
            int interval3 = ItemList.intervalForLevel(level, 3);
            int date3 = date2 - interval2 + interval3;
            assertEquals(interval3, item.getInterval());
            assertEquals(date3, item.getDisplayDate());
                 
            list.setDifficulty(2.0);
            assertEquals(interval2, item.getInterval());
            assertEquals(date2, item.getDisplayDate());
            
            list.setDifficulty(3.0);
            assertEquals(interval3, item.getInterval());
            assertEquals(date3, item.getDisplayDate());

            list.setDifficulty(2.0);
        }
    }

    @Test
    public final void testSearchBackward() {
        assertEquals(5, list.searchBackward(6, "f.*"));
        assertEquals(4, list.searchBackward(5, "f.*"));
        assertEquals(0, list.searchBackward(3, "null"));
        assertEquals(4, list.searchBackward(3, "four"));
        assertEquals(7, list.searchBackward(10, "eve"));
        assertEquals(10, list.searchBackward(10, "zehn"));
        assertEquals(10, list.searchBackward(5, "ten"));
        assertEquals(1, list.searchBackward(5, "one"));
        assertEquals(-1, list.searchForward(4, "aardvark"));
    }

    @Test
    public final void testMoveItem() {
        list.moveItem(1, 3);
        assertEquals("two", list.get(1).getStimulus());
        assertEquals("one", list.get(3).getStimulus());
        list.moveItem(10, 0);
        assertEquals("ten", list.get(0).getStimulus());
        assertEquals("nine", list.get(10).getStimulus());
    }

    @Test
    public final void testGetFromQueue() {
        expectAndAdvance("six");
        expectAndAdvance("zero");
        expectAndAdvance("eight");
        expectAndAdvance("two");
        expectAndAdvance("one");
        expectAndAdvance("three");
        expectAndAdvance("four");
        expectAndAdvance("five");
        expectAndAdvance("seven");
        expectAndAdvance("nine");
        expectAndAdvance("ten");
        assertNull(list.chooseNextItemToDisplay(false));
    }
    
    /**
     * Test if the next stimulus is as expected, and advance
     * the time clock.
     * 
     * @param expectedStimulus The expected stimulus.
     */
    private void expectAndAdvance(String expectedStimulus) {
        Item item = list.chooseNextItemToDisplay(false);
        assertEquals(expectedStimulus, item.getStimulus());
        item.setVirgin(false);
    }

    @Test
    public final void testSaveAndLoad() throws IOException {
        File file = new File("TempFileForDrill_2007.txt");
        int size = list.size();
        int mid = size / 2;
        Item first = list.get(0);
        Item middle = list.get(mid);
        Item last = list.get(size - 1);
        list.saveOnFile(file);
        
        ItemList list2 = new ItemList();
        list2.load(file);
        file.delete();
        assertEquals(first, list2.get(0));
        assertEquals(middle, list2.get(mid));
        assertEquals(last, list2.get(size - 1));
    }

    @Test
    public final void testQueueOperation() {
//        double oldPromotionFactor = promotionFactor;
//        promotionFactor = 3;
//        int vp = virginPromotion;
        try {
            expectAndEvaluate("six", true, 3, 18);    // interval was 5
            expectAndEvaluate("zero", false, 4, 10);  // virgin item
            expectAndEvaluate("eight", false, 5, 9);  // interval was 6, min is 4
            expectAndEvaluate("two", true, 5, 11);    // interval was 5
            expectAndEvaluate("one", false, 7, 13);   // virgin item
            expectAndEvaluate("three", false, 10, 14);// virgin item
            expectAndEvaluate("eight", true, 9, 13);  // interval stays at 6
            expectAndEvaluate("zero", true, 10, 16);  // demoted from virgin
            
            expectAndEvaluate("two", true, 11, 16);   // still demoted from virgin
//            expectAndEvaluate("four", true, 12, 12 + vp);  // virgin, correct
            expectAndEvaluate("one", true, 13, 19);   // demoted from virgin
            expectAndEvaluate("three", true, 14, 0);  // 14
            expectAndEvaluate("eight", true, 13, 26); // interval was 6
        }
        finally {
//            promotionFactor = oldPromotionFactor;
        }
    }

    /**
     * Tests if the given stimulus is as expected, and promotes
     * or demotes the item.
     * 
     * @param expectedStimulus The expected stimulus.
     * @param correct Whether the item is to be marked right (hence promoted).
     * @param date 
     */
    private void expectAndEvaluate(String expectedStimulus,
                                   boolean correct,
                                   int expectedDate,
                                   int newExpectedDate) {
        Item item = list.chooseNextItemToDisplay(false);
        if (!item.isVirgin()) {
            assertEquals("date", expectedDate, item.getDisplayDate());
        }
        assertEquals(expectedStimulus, item.getStimulus());
        if (correct) item.promote();
        else item.demote();
        list.put(item);
        assertEquals("new date", newExpectedDate, item.getDisplayDate());
    }

    @Test
    public final void testForNegativeDates() throws Exception {
        String input = "good morning || konnichi wa\n"
                + "good evening || komban wa\n";
        createGlobalList(input);
        Item item1 = list.chooseNextItemToDisplay(false);
        item1.demote();
        Item item2 = list.chooseNextItemToDisplay(false);
        item2.demote();
        for (Item item : list) {
            assertTrue(item.getDisplayDate() > 0);
        }
    }

    @Test
    public final void testSearchForStimulus() {
        assertEquals(1, list.searchForStimulus("one"));
        assertEquals(3, list.searchForStimulus("three"));
        assertEquals(10, list.searchForStimulus("ten"));
        assertEquals(-1, list.searchForStimulus("gadzooks!"));
    }

    @Test
    public final void testSearchForResponse() {
        String input =
                "zero || null\n"
                + "one || eins\n"
                + "three || drei\n"
                + "four || vier ; quatro\n";
            createGlobalList(input);
        assertEquals("one", list.searchForResponse("eins"));
        assertEquals("three", list.searchForResponse("drei"));
        assertEquals("four", list.searchForResponse("vier"));
        assertEquals("four", list.searchForResponse("quatro"));
    }

    @Test
    public final void testAdjust() {
        int size = list.size();
        assertEquals(0, list.adjust(0));
        assertEquals(size - 1, list.adjust(size - 1));
        assertEquals(0, list.adjust(size));
        assertEquals(5, list.adjust(5));
        assertEquals(size - 1, list.adjust(-1));
        assertEquals(size - 2, list.adjust(-2));
        assertEquals(2, list.adjust(size + 2));
    }

    @Test
    public final void testVirginizeList() {
        int size = list.size();
        int virgins = 0;
        for (int i = 0; i < size; i++) {
            if (list.get(i).isVirgin()) virgins++;
        }
        assertTrue(virgins < size);
        list.virginizeList();
        assertEquals(size, list.size());
        virgins = 0;
        for (int i = 0; i < size; i++) {
            if (list.get(1).isVirgin()) virgins++;
        }
        assertEquals(size, virgins);
    }

    @Test
    public final void testVirginList() {
        String input =
            "zero || null\n"
            + "one || eins\n"
            + "two || zwei\n"
            + "three || drei\n";
        Item item;
        
        createGlobalList(input);
        item = list.chooseNextItemToDisplay(false);
        assertEquals("zero", item.getStimulus());
        item.promote();
        list.put(item);
        item = list.chooseNextItemToDisplay(false);
        assertEquals("one", item.getStimulus());
        
        createGlobalList(input);
        item = list.chooseNextItemToDisplay(false);
        assertEquals("zero", item.getStimulus());
        item.demote();
        list.put(item);
        item = list.chooseNextItemToDisplay(false);
        assertEquals("one", item.getStimulus());
    }
}
