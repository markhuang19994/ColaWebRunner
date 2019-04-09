package com.webrunner.generator;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.JavaParameters;

/**
 * @author MarkHuang
 * @version <ul>
 * <li>2018/9/27, MarkHuang,new
 * </ul>
 * @since 2018/9/27
 */
public interface CommandLineGenerator {
    void configure(JavaParameters javaParams) throws ExecutionException;
}
