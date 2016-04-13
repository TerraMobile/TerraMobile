package br.org.funcate.terramobile.util;

/**
 * Created by bogo on 27/05/15.
 */
public class DevUtil {
    public static boolean isNull(String str)
    {
        return str == null;
    }

    public static String removeUnprintableCharacters(String str) {

        int len = str.length();
        StringBuffer buf = new StringBuffer();
        try {
            for (int i = 0; i < len; i++) {
                String rep = "";
                char cp = str.charAt(i);// the code point
                // Replace invisible control characters and unused code points
                switch (Character.getType(cp)) {
                    case Character.CONTROL:     // \p{Cc}
                    case Character.FORMAT:      // \p{Cf}
                    case Character.PRIVATE_USE: // \p{Co}
                    case Character.SURROGATE:   // \p{Cs}
                    case Character.UNASSIGNED:  // \p{Cn}
                        buf = buf.append(rep);
                        break;
                    default:
                        char[] chars = Character.toChars(cp);
                        buf = buf.append(chars);
                        break;
                }
            }
        }catch (NullPointerException e) {
            e.printStackTrace();
            System.err.println("Confused: " + e);
        }catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.err.println("Confused: " + e);
        }catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            System.err.println("Confused: " + e);
        }catch (Exception e) {
            e.printStackTrace();
            System.err.println("Confused: " + e);
        }

        return buf.toString();
    }
}
