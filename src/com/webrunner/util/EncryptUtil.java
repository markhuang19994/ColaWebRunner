package com.webrunner.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author MarkHuang
 * @version <ul>
 * <li>2018/9/25, MarkHuang,new
 * </ul>
 * @since 2018/9/25
 */
public class EncryptUtil {
    public static String encryptMd5(String s){
        String result = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(s.getBytes());
            result = new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return result;
    }
}
