package com.webrunner.conf;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.icons.AllIcons;
import com.webrunner.util.ResourcesUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author MarkHuang
 * @version <ul>
 * <li>2018/9/19, MarkHuang,new
 * </ul>
 * @since 2018/9/19
 */
public class WebRunnerConfigurationType implements ConfigurationType {
    private static final Icon ICON = ResourcesUtil.getIconResource("/jetty-icon.png");

    private WebRunnerConfigurationType(){
        super();
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Web Runner";
    }

    @Nls
    @Override
    public String getConfigurationTypeDescription() {
        return "IntelliJ IDEA Web Runner";
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @NotNull
    @Override
    public String getId() {
        return "WebRunner";
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        WebRunnerConfigurationFactory factory = new WebRunnerConfigurationFactory(this);
        return new ConfigurationFactory[]{factory};
    }
}
