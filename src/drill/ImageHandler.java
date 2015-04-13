package drill;

import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 * Loads images (<code>.gif</code>, <code>.jpg</code>, and/or <code>.jpeg</code>
 * from a file. To use:
 * <ol>
 *   <li>Construct an ImageHandler, passing in a (mandatory) <code>JFrame</code>.</li>
 *   <li>Call <code>loadImages()</code> or <code>loadImages(File directory)</code>;
 *       this will return an <code>ArrayList</code> of <code>Image</code>s.</li>
 *   <li>You can also call <code>getPictures()</code> to get again the list of
 *       images that were returned by <code>loadImages()</code>.</li>
 *   <li>Call <code>getFileNames()</code> to get the "cleaned" names of the files
 *       from which the images were loaded. The i-th file name corresponds to the
 *       i-th image.</li>
 * </ol>
 */
public class ImageHandler {
    Toolkit toolkit;
    MediaTracker tracker;
    ArrayList<String> fileNames;
    ArrayList<Image> pictures;
    private JFrame owner;
    
    /**
     * Constructor for ImageHandler objects.
     * 
     * @param owner The controlling JFrame (may not be <code>null</code>).
     * @throws IllegalArgumentException If a <code>null</code> parameter was given.
     */
    public ImageHandler(JFrame owner) throws IllegalArgumentException {
        if (owner == null) throw new IllegalArgumentException();
        this.owner = owner;
    }
    
    /**
     * After images have been loaded, returns a list of the "cleaned up" file
     * names of the image files. These names are suitable for presentation
     * to the user, but not for referring to the files.
     * 
     * @return The names of the files containing the pictures.
     */
    protected ArrayList<String> getFileNames() {
        return fileNames;
    }

    /**
     * @return The pictures.
     */
    protected ArrayList<Image> getPictures() {
        return pictures;
    }

    /**
     * Asks the user for a directory, and returns it, or returns <code>null</code>
     * if the user cancels the selection.
     * 
     * @return The chosen directory, or <code>null</code>.
     */
    private File getDirectory() {
        // Display a JChooser load file dialog
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        File file = null;
        chooser.setDialogTitle("Load images from which directory?");
        // Get the file chosen in a JChooser load file dialog
        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
        }
        return file;
    }

    /**
     * Asks the user for a directory, and returns loads images from it,
     * or returns <code>null</code> if the user cancels the selection.
     * 
     * @return A list of the images most recently loaded, or <code>null</code>
     * if none have been loaded.
     */
    ArrayList<Image> loadImages() throws IOException {
        return loadImages(getDirectory());
    }

    /**
     * Returns a list of the images in the given directory..
     * 
     * @return A list of the images that were loaded.
     */
    ArrayList<Image> loadImages(File directory) throws IOException {
        fileNames = new ArrayList<String>();
        pictures = new ArrayList<Image>();
        File[] allFiles = directory.listFiles();
        toolkit = Toolkit.getDefaultToolkit();
        tracker = new MediaTracker(owner);

        for (File file : allFiles) {
            String fileName = file.getCanonicalPath();
            if (fileName.endsWith(".gif") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                Image image = toolkit.getImage(fileName);
                pictures.add(image);
                fileNames.add(cleanFileName(fileName));
                tracker.addImage(image, 0);
            }
        }
        try {
            tracker.waitForAll();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return pictures;
    }
    
    /**
     * Given a file name or a complete path,
     * <ul><li>Removes the path information,</li>
     *     <li>Removes the file extension, and</li>
     *     <li>Replaces any underscores with blanks.</li>
     * </ul>
     * @param fileName The name to be normalized.
     * @return The simplified file name.
     */
    protected String cleanFileName(String fileName) {
        int lastSlash = fileName.lastIndexOf('/');
        int lastDot = fileName.lastIndexOf('.');
        
        if (lastSlash < 0) {
            lastSlash = fileName.lastIndexOf('\\');
        }
        if (lastDot >= 0) {
            fileName = fileName.substring(lastSlash + 1, lastDot);
        }
        fileName = fileName.replaceAll("_", " ");
        return fileName;
    }
}
