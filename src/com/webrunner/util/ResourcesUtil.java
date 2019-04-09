package com.webrunner.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @author MarkHuang
 * @version <ul>
 * <li>2018/9/26, MarkHuang,new
 * </ul>
 * @since 2018/9/26
 */
public class ResourcesUtil {
    private static final Class cls = ResourcesUtil.class;
    private static final Logger LOGGER = LoggerFactory.getLogger(cls);

    public static InputStream getResourceAsStream(String path) {
        return cls.getResourceAsStream(path);
    }

    public static URL getResourceURL(String path) {
        return cls.getResource(path);
    }

    public static String getResourceText(String path) {
        String result = "";
        try (InputStream in = cls.getResourceAsStream(path);
             Scanner sca = new Scanner(in, StandardCharsets.UTF_8.name())) {
            StringBuilder sb = new StringBuilder(500);
            while (sca.hasNextLine()) {
                sb.append(sca.nextLine()).append("\n");
            }
            result = sb.toString();
        } catch (IOException e) {
            LOGGER.error("", e);
        }
        return result;
    }

    public static Icon getIconResource(String path) {
        URL resource = cls.getResource(path);
        return new ImageIcon(resource);
    }
}
