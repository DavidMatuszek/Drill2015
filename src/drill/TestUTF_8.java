/**
 * 
 */
package drill;

/**
 * @author David Matuszek
 */
public class TestUTF_8 {

    /**
     * Umlauts are correctly displayed in Drill, but not in Build.
     * This program provides strong evidence that data input is read in the
     * same way in both.
     * 
     * Solution:
     * Run -> Run Configurations -> Class containing main method -> 
     *   Arguments -> vm arguments -> -Dencoding=UTF-8
     * 
     * @param args Unused
     */
//    public static void main(String[] args) {
//        Drill drill = new Drill();
//        Build build = new Build();
//        drill.load();
//        build.load();
//        ItemList drillList = drill.itemList;
//        ItemList buildList = build.list;
//        System.out.println(drillList == buildList);       // should be false (and is)
//        System.out.println(drillList.equals(buildList));  // should be true (and is)
//        System.out.println("u-umlaut is hex 00fc, that is, \u00fc");
//        for (Item it : drillList) {
//            if (it.getStimulus().contains("\u00fc")) {
//                System.out.println("drillList contains u-umlaut");
//                inspectItem(it);
//                break;
//            }
//        }
//        for (Item it : buildList) {
//            if (it.getStimulus().contains("\u00fc")) {
//                System.out.println("buildList contains u-umlaut");
//                inspectItem(it);
//                break;
//            }
//        }
//        System.out.println("Done.");
//        System.exit(0);
//    }

    private static void inspectItem(Item item) {
            String s = item.toString();
            System.out.println();
            System.out.println(s + "  ");
            for (int j = 0; j < s.length(); j++) {
                System.out.print(" " + Integer.toHexString(s.charAt(j)));
            }
            System.out.println();

    }
}
