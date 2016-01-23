package drill;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFileChooser;

/**
 * A collection of methods for doing routine I/O.
 * <dl>
 *   <dt><code>public static File getInputFile()</code></dt>
 *   <dd>Asks the user to select an input file.</dd>
 *   <dt><code>public static File getOutputFile()</code></dt>
 *   <dd>Asks the user to select an output file.</dd>
 *   <dt><code>public static BufferedReader getReader()</code></dt>
 *   <dd>Returns a reader that has a readLine() method.</dd>
 *   <dt><code>public static PrintWriter getWriter()</code></dt>
 *   <dd>Returns a writer that has print(anything) and println(anything) methods.</dd>
 *   <dt><code>public static byte[] loadAsBinaryFile(File file) throws IOException</code></dt>
 *   <dd>Reads in an entire binary file.</dd>
 *   <dt><code>public static ArrayList<String> loadAsTextFile(File file) throws IOException</code></dt>
 *   <dd>Reads in an entire text file.</dd>
 * </dl>
 *   
 * @author David Matuszek
 * @version Jan 3, 2007
 */
public class IO implements Runnable {

    @Override
    public void run() {
        // TODO Auto-generated method stub
        
    }

    /**
     * Returns an input File chosen by the user, or <code>null</code> if
     * no file was chosen.
     * 
     * @return An existing file, suitable for input (or <code>null</code>).
     * @throws IOException If file could not be read.
     */
    public static File getInputFile() throws IOException {
        return getInputFile(null);
    }

    /**
     * Returns an input File chosen by the user, starting from the given
     * directory, or <code>null</code> if no file was chosen.
     * 
     * @param parent The directory from which to start.
     * @return An existing file, suitable for input (or <code>null</code>).
     * @throws IOException If file could not be read.
     */
    public static File getInputFile(String parent) throws IOException {
        JFileChooser chooser = new JFileChooser(parent);
        chooser.setDialogTitle("Load which file?");
        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        else  if (result == JFileChooser.ERROR_OPTION) {
                throw new IOException("Error in loading file.");
            }
        else {
            return null;
        }
    }


    /**
     * Returns a BufferedReader (which has a readLine() method) for
     * an input file chosen by the user, or <code>null</code> if
     * no file was chosen.
     * 
     * @return An reader for an existing file (or <code>null</code>).
     */
    public static BufferedReader getReader() {
        try {
            File file = getInputFile();
            if (file != null) {
                String fileName = file.getCanonicalPath();
                return new BufferedReader(new FileReader(fileName));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Loads a file as text, returning a list of lines.
     * 
     * @param file The file to load.
     * @return A list of lines.
     * @throws IOException If the file isn't found or can't be read.
     */
    public static ArrayList<String> loadAsTextFile(File file) throws IOException {
        ArrayList<String> lines = new ArrayList<String> ();
        BufferedReader reader = null;
        reader = new BufferedReader(new FileReader(file));

        String line = reader.readLine();
        while (line != null) {
            lines.add(line);
            line = reader.readLine();
        }
        reader.close();
        return lines;
    }

    /**
     * Loads a file as binary data. If the file is not binary data,
     * no harm is done.
     * 
     * @param file The file to be loaded.
     * @return The contents of the file.
     * @throws IOException If the file isn't found or can't be read.
     */
    public static byte[] loadAsBinaryFile(File file) throws IOException {
        long length = file.length();
        assert length <= Integer.MAX_VALUE;
        byte[] buffer = new byte[(int) length];

        FileInputStream fin = new FileInputStream(file);
        synchronized (fin) {
            fin.read(buffer);
            fin.close();
        }
        return buffer;
    }

    /**
     * Returns an output File chosen by the user, or <code>null</code> if
     * no file was chosen.
     * 
     * @return The chosen output file.
     * @throws IOException If the file could not be saved.
     */
    public static File getOutputFile() throws IOException {
        return getOutputFile(null);
    }

    /**
     * Returns an output File chosen by the user, or <code>null</code> if
     * no file was chosen.
     * 
     * @param parent The directory from which to start.
     * @return The chosen output file.
     * @throws IOException If the file could not be saved.
     */
    public static File getOutputFile(String parent) throws IOException {
        JFileChooser chooser = new JFileChooser(parent);
        chooser.setDialogTitle("Save file as?");
        int result = chooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        else  if (result == JFileChooser.ERROR_OPTION) {
                throw new IOException("Unable to save file.");
            }
        else {
            return null;
        }
    }

    /**
     * Returns a PrintWriter for an output file chosen by the user,
     * or <code>null</code> if no file is chosen..
     * 
     * @return A writer for the chosen file.
     */
    public static PrintWriter getWriter() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save file as?");
        int result = chooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            String fileName;
            try {
                if (file != null) {
                    fileName = file.getCanonicalPath();
                    return new PrintWriter(new FileOutputStream(fileName), true);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                return null;
            }       
        }
        return null;
    }


    /**
     * Gets a list of acceptable files in the given directory.
     * 
     * @param directory
     *            The directory from which to choose files.
     * @param filter
     *            Used to determine which files are acceptable.
     * @return A list of files in the given directory.
     */
    public static ArrayList<String> loadDirectory(File directory, FileFilter filter) {
        ArrayList<String> lines = new ArrayList<String>();
        File[] contents = directory.listFiles(filter);
        if (contents.length == 0) {
            lines.add("[No acceptable files in this directory]");
            return lines;
        }
        Arrays.sort(contents);
        for (int i = 0; i < contents.length; i++) {
            String suffix = (contents[i].isDirectory() ? "/" : "");
            lines.add(contents[i].getName() + suffix);
        }
        return lines;
    }
}
