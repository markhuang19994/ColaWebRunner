package com.webrunner.runner;

import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.webrunner.model.WebRunnerConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * @author MarkHuang
 * @version <ul>
 * <li>2018/9/19, MarkHuang,new
 * </ul>
 * @since 2018/9/19
 */
public class WebProgramRunner extends DefaultProgramRunner {

    private static final String RUN = "Run";
    public static final String ID = "WebRunnerForColaProject";

    public WebProgramRunner(){
        super();
    }

    @NotNull
    public String getRunnerId() {
        return ID;
    }

    @Override
    public boolean canRun(@NotNull String value, @NotNull RunProfile runProfile) {
        // It can only run WebRunnerConfigurations
        if(!(runProfile instanceof WebRunnerConfiguration)){
            return false;
        }
        // Values passed are: Run or Debug
        return RUN.equals(value);
    }

    @Override
    public RunContentDescriptor doExecute(@NotNull RunProfileState state,
                                          @NotNull ExecutionEnvironment env) throws com.intellij.execution.ExecutionException {
        return super.doExecute(state, env);
    }
}
