/**
 * 
 */
package drill;

import java.awt.Font;
import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import static drill.ItemListIO.*;

/**
 * This program can be modified at will to correct any problems in an ItemList.
 * @author David Matuszek
 */
public class Repair {
    
    private double difficulty;
    private ItemList itemList;

    /**
     * Reads in a file, repairs it, and writes the original
     * file to a similarly named .bak file.
     * @param args Unused.
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws UnsupportedLookAndFeelException
     */
    public static void main(String[] args) throws ClassNotFoundException, 
                                                  InstantiationException,
                                                  IllegalAccessException,
                                                  UnsupportedLookAndFeelException {
        javax.swing.UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        UIManager.put("TextField.font", new Font(Font.SERIF, Font.PLAIN, 24));     
        showInstructions();
        new Repair().run();

    }

    private void run() {   
        File infile = chooseInputFile();
        if (infile == null) return;
        itemList = readOneList(infile);
        
        String path = infile.getAbsolutePath();
        File bakfile = new File(path + ".bak");
        saveFile(itemList, bakfile);
        
        difficulty = ItemList.getDifficulty();
        repair(itemList);
        
        saveFile(itemList, infile);
        run();
    }

    private void repair(ItemList items) {
        for (Item item : items) {
            repair(item);
        }
    }

    /**
     * Corrects outrageous values for item parameters.
     * @param item The item to be repaired.
     */
    private void repair(Item item) {
        if (repairVirgin(item)) return;
        repairResponseCounts(item);
        repairIntervalAndDate(item);
    }

    /**
     * If an item has no recorded responses, it should be marked as a virgin, and
     * no other corrections can or should be done.
     * @param item The Item to check.
     * @return <code>true</code> if a virgin.
     */
    private static boolean repairVirgin(Item item) {
        if (item.getTimesCorrect() == 0 && item.getTimesIncorrect() == 0) {
            item.setVirgin(true);
            return true;
        }
        return false;
    }
    
    /**
     * Ensures counts are consistent and within reasonable bounds.
     * @param item The item to be repaired.
     */
    private static void repairResponseCounts(Item item) {
        if (item.getTimesCorrect() < 0) item.setTimesCorrect(0);
        if (item.getTimesIncorrect() < 0) item.setTimesIncorrect(0);
        if (item.getConsecutiveTimesCorrect() < 0) item.setConsecutiveTimesCorrect(0);
        
        
        if (item.getConsecutiveTimesCorrect() > item.getTimesCorrect()) {
            item.setConsecutiveTimesCorrect(item.getTimesCorrect());
        }
    }

    /**
     * Resets the items level to the level as currently computed, and
     * ensures the interval and display date are within reasonable bounds.
     * @param item The item to be repaired.
     */
    private void repairIntervalAndDate(Item item) {
        int level = item.getLevel();
        if (level < 0) level = 0;
        if (level > 20) level = 20;
        
        int interval = ItemList.intervalForLevel(level, difficulty);
        item.setInterval(interval);
        
        int maxDate = 25000;
        int date = item.getDisplayDate();
        if (date > maxDate) date = reduceBySteps(date, 100, maxDate);
        if (date > interval) date = reduceBySteps(date, 25, interval);
    }
    
    private static int reduceBySteps(int largeNumber, int step, int limit) {
        int smallerNumber = largeNumber;
        while (smallerNumber > limit) {
            smallerNumber -= step;
        }
        return smallerNumber;
    }

    /**
     * Shows instructions.
     */
    private static void showInstructions() {
        String message =
                "This program reads in a vocabulary file, saves a copy of it\n" + 
                "in a similarly-named file with the '.bak' extension, and writes\n" +
                "the repaired result back onto the original file. No particular\n" + 
                "feedback is provided to the user to indicate this has occurred.\n" + 
                "\n" + 
                "You will be repeatedly asked for files to repair. To end the\n" +
                "program, cancel the file request.";
        JOptionPane.showMessageDialog(null, message);
    }


}
