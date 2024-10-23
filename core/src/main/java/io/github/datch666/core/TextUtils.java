package io.github.datch666.core;

public class TextUtils {
    // 将字符串转换为Unicode序列
    public static String stringToUnicode(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            sb.append("\\u").append(Integer.toHexString(c));
        }
        return sb.toString();
    }

    // 将Unicode序列转换回原始字符串
    public static String unicodeToString(String unicodeStr) {
        String[] hex = unicodeStr.split("\\\\u");
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < hex.length; i++) {
            int data = Integer.parseInt(hex[i], 16);
            sb.append((char) data);
        }
        return sb.toString();
    }
}
