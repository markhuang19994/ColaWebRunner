package com.webrunner.generator.impl;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.application.impl.ApplicationInfoImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.mchange.v2.c3p0.impl.PoolBackedDataSourceBase;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.webrunner.generator.CommandLineGenerator;
import com.webrunner.model.WebRunnerConfiguration;
import com.webrunner.util.FileUtil;
import com.webrunner.util.ProjectInfoUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author MarkHuang
 * @version <ul>
 * <li>2018/9/27, MarkHuang,new
 * </ul>
 * @since 2018/9/27
 */
public abstract class AbstractPomCommandLineGenerator implements CommandLineGenerator {
    ExecutionEnvironment environment;
    WebRunnerConfiguration runConf;

    AbstractPomCommandLineGenerator(@NotNull ExecutionEnvironment environment, WebRunnerConfiguration runConf) {
        this.environment = environment;
        this.runConf = runConf;
    }

    public void configure(JavaParameters javaParams) throws ExecutionException {
        Project project = this.environment.getProject();
        JettyPluginEnvGenerator templateProvider = JettyPluginEnvGenerator.getInstance(runConf.getWebContainerIndex(), runConf.getWebContainerVersionIndex());
        javaParams.setWorkingDirectory(templateProvider.getPluginBasePath(project));
        ProjectRootManager manager = ProjectRootManager.getInstance(project);
        javaParams.setJdk(manager.getProjectSdk());
        javaParams.getVMParametersList().addParametersString("-Dmaven.multiModuleProjectDirectory=" + templateProvider.getPluginBasePath(project));

        File jetBrainPluginClassFile = ProjectInfoUtil.getJetBrainPluginClassFile();
        if (jetBrainPluginClassFile == null) {
            throw new ExecutionException(String.format("can't found jetBrain plugin class file:%s", jetBrainPluginClassFile));
        }

        File jetBrainLibDir = FileUtil.getParentDir(jetBrainPluginClassFile, "lib");
        if (jetBrainLibDir == null) {
            throw new ExecutionException(String.format("can't found jetBrain lib dir at:%s", jetBrainPluginClassFile.getAbsolutePath()));
        }

        File jetBrainBaseDir = jetBrainLibDir.getParentFile();
        File pluginDir = FileUtil.getChildDirWithCache(jetBrainBaseDir, "plugins");
        if (pluginDir == null) {
            throw new ExecutionException(String.format("can't found plugins dir at:%s", jetBrainBaseDir.getAbsolutePath()));
        }

        javaParams.setJarPath(FileUtil.getJarPathForClass(PoolBackedDataSourceBase.class));
        javaParams.setJarPath(FileUtil.getJarPathForClass(SQLServerDataSource.class));

        File mavenPluginHome = FileUtil.getChildDirWithCache(pluginDir, "maven3");
        if (mavenPluginHome == null) {
            throw new ExecutionException("can't found maven plugin dir");
        }

        String mvnHomePath = mavenPluginHome.getAbsolutePath();
        javaParams.getVMParametersList().addParametersString("\"-Dmaven.home=" + mvnHomePath + "\"");
        javaParams.getVMParametersList().addParametersString("\"-Dclassworlds.conf=" + mvnHomePath + "/bin/m2.conf\"");
        javaParams.getVMParametersList().addParametersString("-Didea.version=" + ApplicationInfoImpl.getShadowInstance().getMajorVersion() + "." + ApplicationInfoImpl.getShadowInstance().getMinorVersion());

        File mavenBootDir = new File(mvnHomePath, "boot");
        File[] mavenBootFiles = mavenBootDir.listFiles();
        if (mavenBootFiles != null) {
            javaParams.getClassPath().addAll(Arrays.stream(mavenBootFiles)
                    .map(File::getAbsolutePath)
                    .collect(Collectors.toList()));
        }
        javaParams.setMainClass("org.codehaus.classworlds.Launcher");
//        javaParams.getProgramParametersList().add("clean:clean");
    }


}
