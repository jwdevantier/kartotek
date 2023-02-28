package it.defmacro.kartotek.jartotek.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Checksum {
    public static String toHexString(byte[] barr) {
        char[] cb = new char[barr.length * 2];
        for (int i = 0; i < barr.length; i++) {
            byte b = barr[i];
            int cb_off = i * 2;
            cb[cb_off] = Character.forDigit((b >> 4) & 0xf, 16);
            cb[cb_off+1] = Character.forDigit(b & 0xf, 16);
        }
        return String.valueOf(cb);
    }

    public static String md5Sum(String contents) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(contents.getBytes());
            return toHexString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("no MD5 algorithm?!");
        }
    }
}
