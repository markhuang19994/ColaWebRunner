package com.webrunner.util;

import com.intellij.execution.BeforeRunTask;
import com.intellij.openapi.application.impl.ApplicationInfoImpl;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.impl.run.BuildArtifactsBeforeRunTask;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.webrunner.model.WebRunnerConfiguration;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author MarkHuang
 * @version <ul>
 * <li>2018/9/25, MarkHuang,new
 * </ul>
 * @since 2018/9/25
 */
public class ProjectInfoUtil {
    public static File getJetBrainPluginClassFile() {
        try {
            String osName = System.getProperty("os.name");
            boolean isWindows = osName.contains("Windows");
            String pluginClassPath = URLDecoder.decode(ApplicationInfoImpl.class.getResource("").getPath(), "UTF-8");
            return new File(pluginClassPath
                    .replaceAll(isWindows ? "^(file:/)" : "^(file:)", ""));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File getWebRunnerPluginDir() {
        return FileUtil.getParentDirWithCache(new File(FileUtil.getJarPathForClass(SQLServerDataSource.class)), "MyWebRunner");
    }

    public static String getArtifactOutputFileDir(WebRunnerConfiguration runConf) {
        List<BeforeRunTask<?>> beforeRunTasks = runConf.getBeforeRunTasks();
        BuildArtifactsBeforeRunTask brt = (BuildArtifactsBeforeRunTask) beforeRunTasks
                .stream()
                .filter(x -> x.getProviderId().toString().equals("BuildArtifacts"))
                .collect(Collectors.toList())
                .get(0);
        Artifact artifact = brt.getArtifactPointers().get(0).getArtifact();
        return artifact != null ? artifact.getOutputFilePath() : "";
    }
}
