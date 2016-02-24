/**
 * 
 */
package drill;

import static drill.ItemListIO.chooseInputFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * @author David Matuszek
 */
public class ItemListIO {
    
    static File file;

    static ItemList chooseAndReadInputFile() {
        file = chooseInputFile();
        return readOneList(file);
    }
    
    static String getInputFileName() {
        return file.getPath();
    }

    /**
     * Asks the user to choose an input file, and returns it (if chosen)
     * or null (if the user cancelled the request).
     * @return A file to read, or null.
     */
    static File chooseInputFile() {
        

        Preferences userPrefs = Preferences.userNodeForPackage(Drill.class);
        String key = Drill.class.getSimpleName() + "Directory";
        String parent = userPrefs.get(key, null);
        
        
        JFileChooser chooser = new JFileChooser(parent);
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
