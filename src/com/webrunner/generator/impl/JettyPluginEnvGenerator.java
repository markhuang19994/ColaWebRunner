package com.webrunner.generator.impl;

/**
 * @author MarkHuang
 * @version <ul>
 * <li>2018/9/24, MarkHuang,new
 * </ul>
 * @since 2018/9/24
 */
public class JettyPluginEnvGenerator extends AbstractMavenPluginEnvGenerator {

    private JettyPluginEnvGenerator(int webContainerIndex, int webContainerVersionIndex) {
        super(webContainerIndex, webContainerVersionIndex);
    }

    public static JettyPluginEnvGenerator getInstance(int webContainerIndex, int webContainerVersionIndex) {
        return new JettyPluginEnvGenerator(webContainerIndex, webContainerVersionIndex);
    }

    public String getTemplateFileText() {
        if (super.container.name().equalsIgnoreCase("jetty")) {
            if (super.version.charAt(0) == '6') {
                return JETTY6_TEMPLATE_FILE_TEXT;
            } else {
                return JETTY9_TEMPLATE_FILE_TEXT;
            }
        } else {
            try {
                throw new IllegalArgumentException("not sup container " + container.name() + " in jetty generator");
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return "";
            }
        }
    }
}
