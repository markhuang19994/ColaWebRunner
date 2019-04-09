package com.webrunner.util;

import org.apache.velocity.runtime.directive.Parse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author MarkHuang
 * @version <ul>
 * <li>2018/9/26, MarkHuang,new
 * </ul>
 * @since 2018/9/26
 */
public class ParseUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParseUtil.class);

    public static int saveParseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException | NullPointerException e) {
            LOGGER.error("", e);
            return 0;
        }
    }
}
