package drill;
/*
 * Created on Jul 6, 2007
 */

public class German implements SpecialCharacters {
    static final String capital_A_umlaut = "\u00C4";
    static final String capital_O_umlaut = "\u00D6";
    static final String capital_U_umlaut = "\u00DC";
    static final String lowercase_A_umlaut = "\u00E4";
    static final String lowercase_O_umlaut = "\u00F6";
    static final String lowercase_U_umlaut = "\u00FC";
    static final String lowercase_ess_zed = "\u00FC";
    
    public String addSpecialCharacters(String ascii) {
        String unicode = ascii;
        unicode = unicode.replaceAll("A:", capital_A_umlaut);
        unicode = unicode.replaceAll("O:", capital_O_umlaut);
        unicode = unicode.replaceAll("U:", capital_U_umlaut);
        unicode = unicode.replaceAll("a:", lowercase_A_umlaut);
        unicode = unicode.replaceAll("o:", lowercase_O_umlaut);
        unicode = unicode.replaceAll("u:", lowercase_U_umlaut);
        unicode = unicode.replaceAll("s:", lowercase_ess_zed);
        return unicode;
    }

    public String removeSpecialCharacters(String unicode) {
        String ascii = unicode;
        ascii = ascii.replaceAll(capital_A_umlaut, "A:");
        ascii = ascii.replaceAll(capital_O_umlaut, "O:");
        ascii = ascii.replaceAll(capital_U_umlaut, "U:");
        ascii = ascii.replaceAll(lowercase_A_umlaut, "a:");
        ascii = ascii.replaceAll(lowercase_O_umlaut, "o:");
        ascii = ascii.replaceAll(lowercase_U_umlaut, "u:");
        ascii = ascii.replaceAll(lowercase_ess_zed, "s:");
        return ascii;
    }

}
