package com.webrunner.util;

import com.intellij.util.PathUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author MarkHuang
 * @version <ul>
 * <li>2018/9/20, MarkHuang,new
 * </ul>
 * @since 2018/9/20
 */
public class FileUtil {

    private static Map<String, Object> resultCacheMap = new HashMap<>();

    /**
     * Get parent dir with figure name
     *
     * @param f       File
     * @param dirName dirName
     * @return File
     */
    public static File getParentDir(File f, String dirName) {
        File temp = f.getParentFile();
        while (temp != null) {
            if (temp.getName().equals(dirName)) {
                return temp;
            }
            temp = temp.getParentFile();
        }
        return null;
    }

    public static File getParentDirWithCache(File f, String dirName) {
        String cacheKey = "getParentDir" + f.getAbsolutePath() + dirName;
        return Optional
                .ofNullable((File) getCache(cacheKey))
                .orElseGet(() -> putValueAndReturn(cacheKey, getParentDir(f, dirName)));
    }

    /**
     * Get child dir with figure name
     *
     * @param f       File
     * @param dirName dirName
     * @return File
     */
    public static File getChildDir(File f, String dirName) {
        File result;
        File[] files = f.listFiles();
        if (files == null) {
            return null;
        }
        for (File file : files) {
            if (file.getName().equals(dirName)) {
                return file;
            } else {
                result = getChildDir(file, dirName);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public static File getChildDirWithCache(File f, String dirName) {
        String cacheKey = "getChildDirWithCache" + f.getAbsolutePath() + dirName;
        return Optional
                .ofNullable((File) getCache(cacheKey))
                .orElseGet(() -> putValueAndReturn(cacheKey, getChildDir(f, dirName)));
    }

    public static String getJarPathForClass(Class cls) {
        String cacheKey = "getJarForClass" + cls.getName();
        return Optional
                .ofNullable((String) getCache(cacheKey))
                .orElseGet(() -> putValueAndReturn(cacheKey, PathUtil.getJarPathForClass(cls)));
    }

    private static Object getCache(String cacheKey) {
        return resultCacheMap.get(EncryptUtil.encryptMd5(cacheKey));
    }

    public static boolean makeDirs(File f) {
        if (!f.exists()) {
            return f.mkdirs();
        }
        return true;
    }

    private static <T> T putValueAndReturn(String key, T val) {
        resultCacheMap.put(EncryptUtil.encryptMd5(key), val);
        return val;
    }

    public static void copyFile(File f, String targetDir) {
        try {
            Files.copy(Paths.get(f.getAbsolutePath()), new FileOutputStream(targetDir));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
