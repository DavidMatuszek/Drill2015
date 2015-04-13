package drill;
/*
 * Created on Feb 18, 2007
 */

public class Japanese implements SpecialCharacters {

    public String addSpecialCharacters(String ascii) {
        return addMacrons(ascii);
    }

    public String removeSpecialCharacters(String unicode) {
        return removeMacrons(unicode);
    }

    static String removeMacrons(String s) {
        //  Aa=\u0100\u0101  Ee=\u0112\u0113  Oo=\u014c\u014d  Uu=\u016a\u016b
        s = s.replaceAll("\u0100", "Aa");
        s = s.replaceAll("\u0101","aa");
        s = s.replaceAll("\u0112", "Ee");
        s = s.replaceAll("\u0113", "ee");
        s = s.replaceAll("\u014c", "Oo");
        s = s.replaceAll("\u014d", "oo");
        s = s.replaceAll("\u016a","Uu");
        s = s.replaceAll("\u016b", "uu");
        return s;
    }

    static String addMacrons(String s) {
        //  Aa=\u0100\u0101  Ee=\u0112\u0113  Oo=\u014c\u014d  Uu=\u016a\u016b
        s = s.replaceAll("AA", "\u0100");
        s = s.replaceAll("Aa", "\u0100");
        s = s.replaceAll("aa", "\u0101");
        s = s.replaceAll("EE", "\u0112");
        s = s.replaceAll("Ee", "\u0112");
        s = s.replaceAll("ee", "\u0113");
        s = s.replaceAll("OO", "\u014c");
        s = s.replaceAll("Oo", "\u014c");
        s = s.replaceAll("oo", "\u014d");
        s = s.replaceAll("Ou", "\u014c");
        s = s.replaceAll("OU", "\u014c");
        s = s.replaceAll("ou", "\u014d");
        s = s.replaceAll("UU", "\u016a");
        s = s.replaceAll("Uu", "\u016a");
        s = s.replaceAll("uu", "\u016b");
        return s;
    }

}
