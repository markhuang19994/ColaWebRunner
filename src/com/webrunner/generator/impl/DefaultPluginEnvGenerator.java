package com.webrunner.generator.impl;

/**
 * @author MarkHuang
 * @version <ul>
 * <li>2018/10/16, MarkHuang,new
 * </ul>
 * @since 2018/10/16
 */
public class DefaultPluginEnvGenerator extends AbstractMavenPluginEnvGenerator {
    public DefaultPluginEnvGenerator(int webContainerIndex, int webContainerVersionIndex) {
        super(webContainerIndex, webContainerVersionIndex);
    }

    @Override
    public String getTemplateFileText() {
        throw new UnsupportedOperationException();
    }
}
