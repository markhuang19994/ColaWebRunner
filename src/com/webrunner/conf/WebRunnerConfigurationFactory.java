package com.webrunner.conf;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import com.webrunner.model.WebRunnerConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * @author MarkHuang
 * @version <ul>
 * <li>2018/9/19, MarkHuang,new
 * </ul>
 * @since 2018/9/19
 */
public class WebRunnerConfigurationFactory extends ConfigurationFactory {
    public WebRunnerConfigurationFactory(@NotNull ConfigurationType type) {
        super(type);
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new WebRunnerConfiguration(project, this, "Web Runner");
    }

    @Override
    public boolean isConfigurationSingletonByDefault() {
        return true;
    }

}
