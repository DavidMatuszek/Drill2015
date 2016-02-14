/**
 * 
 */
package drill;

import static drill.ItemListIO.chooseInputFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * @author David Matuszek
 */
public class ItemListIO {

    static ItemList chooseAndReadInputFile() {
        File file = chooseInputFile();
        return readOneList(file);
    }

    static File chooseInputFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Load which file?");
        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    /**
     * Reads and returns one ItemList, or returns null if there is an
     * error reading the file.
     * @param file The file to read.
     * @return The ItemList, or null.
     */
    static ItemList readOneList(File file) {
        try {
            ItemList newList = new ItemList();
            newList.load(new BufferedReader(new FileReader(file)));
            return newList;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            return null;
        }
    }


    static void chooseAndSaveOutputFile(ItemList baseList) {
        File file = chooseOutputFile();
        try {
            baseList.saveOnFile(file);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage() + "\nTry again.");
            chooseAndSaveOutputFile(baseList);
        }
        System.exit(0);
    }
    
    static File chooseOutputFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Load which file?");
        int result = chooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

}
