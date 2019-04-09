package com.webrunner.generator.impl;

/**
 * @author MarkHuang
 * @version <ul>
 * <li>2018/9/27, MarkHuang,new
 * </ul>
 * @since 2018/9/27
 */
public class TomcatPluginEnvGenerator extends AbstractMavenPluginEnvGenerator {

    private TomcatPluginEnvGenerator(int webContainerIndex, int webContainerVersionIndex) {
        super(webContainerIndex, webContainerVersionIndex);
    }

    public static TomcatPluginEnvGenerator getInstance(int webContainerIndex, int webContainerVersionIndex) {
        return new TomcatPluginEnvGenerator(webContainerIndex, webContainerVersionIndex);
    }


    @Override
    public String getTemplateFileText() {
        if (super.container.name().equalsIgnoreCase("tomcat")) {
            return TOMCAT_TEMPLATE_FILE_TEXT;
        } else {
            try {
                throw new IllegalArgumentException("not sup container " + container.name() + " in tomcat generator");
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return "";
            }
        }
    }
}
