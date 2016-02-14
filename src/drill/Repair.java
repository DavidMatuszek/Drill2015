/**
 * 
 */
package drill;

import java.awt.Font;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import static drill.ItemListIO.*;

/**
 * This program can be modified at will to correct any problems in an ItemList.
 * @author David Matuszek
 */
public class Repair {

    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        javax.swing.UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        UIManager.put("TextField.font", new Font(Font.SERIF, Font.PLAIN, 24));
        new Repair().run();

    }

    private void run() {
        
        showInstructions();
        ItemList items = chooseAndReadInputFile();
        repair(items);
        chooseAndSaveOutputFile(items);
    }

    private void repair(ItemList items) {
        for (Item item : items) {
            repair(item);
        }
    }

    /**
     * @param item
     */
    private void repair(Item item) {
        if (item.getDisplayDate() == 524224) {
            item.setVirgin(true);
        }
    }

    private void showInstructions() {
        String message =
                "This program reads in a vocabulary file, makes changes to it,\n" + 
                "and writes the result on a new file. The changes to be made\n" + 
                "are hard-wired into the program, in the repair method.\n" + 
                "\n" + 
                "You will be prompted, first for the file to read and repair,\n" + 
                "then for the output file. The program will then stop.";
        JOptionPane.showMessageDialog(null, message);
    }


}
