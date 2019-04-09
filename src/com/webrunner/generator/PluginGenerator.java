package com.webrunner.generator;

import com.intellij.openapi.project.Project;

/**
 * @author MarkHuang
 * @version <ul>
 * <li>2018/9/27, MarkHuang,new
 * </ul>
 * @since 2018/9/27
 */
public interface PluginGenerator {
    String getPluginBasePath(Project project);
}
