package com.webrunner.action;

import com.intellij.execution.*;
import com.intellij.execution.compound.ConfigurationSelectionUtil;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.webrunner.generator.impl.DefaultPluginEnvGenerator;
import com.webrunner.model.RunnerActionEnum;
import com.webrunner.model.WebRunnerConfiguration;
import com.webrunner.runner.WebProgramRunner;
import com.webrunner.util.ResourcesUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.Objects;

/**
 * @author MarkHuang
 * @version <ul>
 * <li>2018/10/16, MarkHuang,new
 * </ul>
 * @since 2018/10/16
 */
public class CleanBuildAction extends AnAction {

    static final Icon ICON = ResourcesUtil.getIconResource("/clean.png");

    public CleanBuildAction() {
        super(ICON);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        ConfigurationType webRunner = ConfigurationTypeUtil.findConfigurationType("WebRunner");
        if (webRunner != null) {
            Project project = Objects.requireNonNull(e.getProject());
            RunConfiguration runConf = RunManager.getInstance(project).getConfigurationsList(webRunner).get(0);
            if (runConf instanceof WebRunnerConfiguration) {
                WebRunnerConfiguration webRunConf = (WebRunnerConfiguration) runConf;
                if (!isArtifactTargetExist(webRunConf)) {
                    showNotification(runConf);
                    return;
                }
                ConfigurationFactory configurationFactory = webRunner.getConfigurationFactories()[0];
                WebRunnerConfiguration cloneRunConf = webRunConf.clone();
                Executor ex = ExecutorRegistryImpl.getInstance().getExecutorById("Run");
                ExecutionEnvironment env = ExecutionEnvironmentBuilder.create(ex, cloneRunConf).build();
                cloneRunConf.setAction(RunnerActionEnum.CLEAN.action);
                ProgramRunner runner = RunnerRegistry.getInstance().findRunnerById(WebProgramRunner.ID);
                if (runner != null) {
                    executeRunner(runner, env);
                }
            }
        }
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        super.update(e);
    }

    private boolean isArtifactTargetExist(WebRunnerConfiguration webRunConf) {
        DefaultPluginEnvGenerator envGenerator = new DefaultPluginEnvGenerator(webRunConf.getWebContainerIndex(), webRunConf.getWebContainerVersionIndex());
        String basePath = envGenerator.getPluginBasePath(webRunConf.getProject());
        return FileUtil.exists(basePath + File.separator + "target");
    }

    private void showNotification(RunConfiguration runConf) {
        Notifications.Bus.notify(new Notification("Web Runner", "Web Runner", String.format("Can't find '%s' artifact target", runConf.getName()), NotificationType.ERROR), runConf.getProject());
    }

    private void executeRunner(@NotNull ProgramRunner runner, @NotNull ExecutionEnvironment env) {
        try {
            runner.execute(env);
        } catch (ExecutionException e1) {
            e1.printStackTrace();
        }
    }
}
