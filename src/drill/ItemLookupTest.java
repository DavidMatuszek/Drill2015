/**
 * 
 */
package drill;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * @author David Matuszek
 */
public class ItemLookupTest {
    ItemLookup lookup;
    Item xy =   new Item("x", "y");
    Item adef = new Item("a", "d; e; f");
    Item bef =  new Item("b", "e; f");
    Item abdf = new Item("a; b", "d; f");
    Item cf =   new Item("c", "f");
    Item acde = new Item("a; c", "d; e");
    Item bce =  new Item("b; c", "e");
    Item abcd = new Item("a; b; c", "d");

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        lookup = new ItemLookup();
        addItems();
    }

    /**
     * Test method for {@link drill.ItemLookup#addItem(drill.Item)}.
     */
    @Test
    public final void testAddItem() {
        assertEquals(null, lookup.itemsContainingStimulus.get("None"));
        assertEquals(4, lookup.itemsContainingStimulus.get("a").size());
        assertEquals(1, lookup.itemsContainingStimulus.get("x").size());
        assertEquals(null, lookup.itemsContainingResponse.get("None"));
        assertEquals(4, lookup.itemsContainingResponse.get("f").size());
        assertEquals(1, lookup.itemsContainingResponse.get("y").size());
    }
    
    private void addItems() {
        lookup.addItem(new Item("x", "y"));
        lookup.addItem(new Item("a", "d; e; f"));
        lookup.addItem(new Item("b", "e; f"));
        lookup.addItem(new Item("a; b", "d; f"));
        lookup.addItem(new Item("c", "f"));
        lookup.addItem(new Item("a; c", "d; e"));
        lookup.addItem(new Item("b; c", "e"));
        lookup.addItem(new Item("a; b; c", "d"));
    }

    /**
     * Test method for {@link drill.ItemLookup#getItemsWithStimulus(java.lang.String)}.
     */
    @Test
    public final void testGetItemsWithStimulus() {
        Set<Item> actual = new HashSet(lookup.getItemsWithStimulus("f"));
        Set<Item> expected = new HashSet();
        assertEquals(expected, actual);
        
        actual = new HashSet(lookup.getItemsWithStimulus("a"));
        expected.add(adef);
        expected.add(abdf);
        expected.add(acde);
        expected.add(abcd);
        assertEquals(expected, actual);
    }

    /**
     * Test method for {@link drill.ItemLookup#getItemsWithResponse(java.lang.String)}.
     */
    @Test
    public final void testGetItemsWithResponse() {
        Set<Item> actual = new HashSet(lookup.getItemsWithResponse("a"));
        Set<Item> expected = new HashSet();
        assertEquals(actual, expected);
        
        actual = new HashSet(lookup.getItemsWithResponse("y"));
        expected.add(xy);
        assertEquals(actual, expected);
               
        actual = new HashSet(lookup.getItemsWithResponse("d"));
        expected = new HashSet();
        expected.add(adef);
        expected.add(abdf);
        expected.add(acde);
        expected.add(abcd);
        assertEquals(expected, actual);
    }

    /**
     * Test method for {@link drill.ItemLookup#extractResponsesFrom(java.util.List)}.
     */
    @Test
    public final void testExtractResponsesFrom() {
        List<Item> list = new ArrayList<Item>();
        list.add(xy);
        list.add(bce);
        list.add(acde);
        Set<String> actual = lookup.extractResponsesFrom(list);
        Set<String> expected = new HashSet();
        expected.add("y");
        expected.add("d");
        expected.add("e");
        assertEquals(expected, actual);
    }

    /**
     * Test method for {@link drill.ItemLookup#extractStimuliFrom(java.util.List)}.
     */
    @Test
    public final void testExtractStimuliFrom() {
        List<Item> list = new ArrayList<Item>();
        list.add(xy);
        list.add(adef);
        list.add(acde);
        Set<String> actual = lookup.extractStimuliFrom(list);
        Set<String> expected = new HashSet();
        expected.add("x");
        expected.add("a");
        expected.add("c");
        assertEquals(expected, actual);
    }

    /**
     * Test method for {@link drill.ItemLookup#isValidPair(java.lang.String, java.lang.String)}.
     */
    @Test
    public final void testIsValidPair() {
        assertTrue(lookup.isValidPair("a", "d"));
        assertTrue(lookup.isValidPair("b", "f"));
        assertTrue(lookup.isValidPair("x", "y"));
        assertFalse(lookup.isValidPair("a", "b"));
        assertFalse(lookup.isValidPair("x", "b"));
    }

}
