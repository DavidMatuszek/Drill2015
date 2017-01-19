package drill;

import java.io.File;
import java.io.IOException;

/**
 * @author David Matuszek
 */
public class Analyze {
    private File itemFile;
    private ItemList list;

    public Analyze() {
        // TODO Auto-generated constructor stub
    }

    public static void main(String[] args) {
        new Analyze().run();
    }
    
    void run() {
        loadList();
        System.out.println("Current level of difficulty: " + list.difficulty);
        int virgins = countVirgins();
        int active = list.size() - virgins;
        System.out.println(list.size() + " items; " + active +
                           " active, " + virgins + " virgins.");
    }

    private void loadList() {
        try {
            list = new ItemList();
            list.load(Analyze.class);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int countVirgins() {
        int virginItems = 0;
        for (Item item: list) {
            if (item.isVirgin()) virginItems += 1;
        }
        return virginItems;
    }

}
