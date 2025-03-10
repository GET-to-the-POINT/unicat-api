package gettothepoint.unicatapi.common.util;

import java.nio.charset.StandardCharsets;

public class CharacterUtil {
    public static String convertToUTF8(String value) {
        return new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }
}
