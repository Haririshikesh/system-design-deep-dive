package com.sd.url_shortener_service.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Base62Util {
    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String encode(long value) {
        StringBuilder sb = new StringBuilder();
        if (value == 0) {
            return String.valueOf(BASE62.charAt(0));
        }
        while (value > 0) {
            int digit = (int) (value % 62);
            sb.append(BASE62.charAt(digit));
            value /= 62;
        }
        return sb.reverse().toString();
    }
}
