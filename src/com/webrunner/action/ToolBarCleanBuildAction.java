package com.webrunner.action;

import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.project.Project;
import com.webrunner.conf.WebRunnerConfigurationType;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author MarkHuang
 * @version <ul>
 * <li>2018/10/16, MarkHuang,new
 * </ul>
 * @since 2018/10/16
 */
public class ToolBarCleanBuildAction extends AnAction {
    public ToolBarCleanBuildAction() {
        super(CleanBuildAction.ICON);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        ActionManager.getInstance().getAction("Webrunner.CleanBuild").actionPerformed(e);
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        Project project = e.getProject();
        if (project == null) return;
        RunnerAndConfigurationSettings conf = RunManagerEx.getInstanceEx(project).getSelectedConfiguration();
        if (conf != null) {
            ConfigurationType type = conf.getType();
            Presentation presentation = e.getPresentation();
            presentation.setText(String.format("Clean '%s' target", conf.getName()));
            boolean isEnabledAndVisible = presentation.isEnabledAndVisible();
            if (isEnabledAndVisible != type instanceof WebRunnerConfigurationType) {
                presentation.setEnabledAndVisible(!isEnabledAndVisible);
            }
        }
    }
}
