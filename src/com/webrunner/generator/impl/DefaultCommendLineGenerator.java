package com.webrunner.generator.impl;

import com.intellij.execution.runners.ExecutionEnvironment;
import com.webrunner.model.WebRunnerConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * @author MarkHuang
 * @version <ul>
 * <li>2018/10/16, MarkHuang,new
 * </ul>
 * @since 2018/10/16
 */
public class DefaultCommendLineGenerator extends AbstractPomCommandLineGenerator{
    private DefaultCommendLineGenerator(@NotNull ExecutionEnvironment environment, WebRunnerConfiguration runConf) {
        super(environment, runConf);
    }

    public static DefaultCommendLineGenerator getInstance(@NotNull ExecutionEnvironment environment, WebRunnerConfiguration model) {
        return new DefaultCommendLineGenerator(environment, model);
    }
}
