package com.webrunner.generator.impl;

import com.intellij.openapi.project.Project;
import com.webrunner.generator.PluginGenerator;
import com.webrunner.ui.WebRunnerConfPanel;
import com.webrunner.util.FileUtil;
import com.webrunner.util.EncryptUtil;
import com.webrunner.util.ProjectInfoUtil;
import com.webrunner.util.ResourcesUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.webrunner.ui.WebRunnerConfPanel.SupportWebContainer.getContainerByIndex;
import static com.webrunner.ui.WebRunnerConfPanel.SupportWebContainer.getContainerVersionByIndex;

/**
 * @author MarkHuang
 * @version <ul>
 * <li>2018/9/27, MarkHuang,new
 * </ul>
 * @since 2018/9/27
 */
public abstract class AbstractMavenPluginEnvGenerator implements PluginGenerator {
    static final String JETTY6_TEMPLATE_FILE_TEXT;
    static final String JETTY9_TEMPLATE_FILE_TEXT;
    static final String TOMCAT_TEMPLATE_FILE_TEXT;
    String version;
    WebRunnerConfPanel.SupportWebContainer container;

    static {
        JETTY6_TEMPLATE_FILE_TEXT = ResourcesUtil.getResourceText("/template/maven/jetty/jettyPomPluginTemplate.txt");
        JETTY9_TEMPLATE_FILE_TEXT = ResourcesUtil.getResourceText("/template/maven/jetty/jetty9PomPluginTemplate.txt");
        TOMCAT_TEMPLATE_FILE_TEXT = ResourcesUtil.getResourceText("/template/maven/tomcat/tomcatPomPluginTemplate.txt");
    }

    public abstract String getTemplateFileText();

    public AbstractMavenPluginEnvGenerator(int webContainerIndex, int webContainerVersionIndex){
        WebRunnerConfPanel.SupportWebContainer container = getContainerByIndex(webContainerIndex);
        this.container = container;
        this.version =  getContainerVersionByIndex(container, webContainerVersionIndex);
    }

    public void writeTemplateText(Project project, String text) {
        File tempDir = new File(getPluginBasePath(project));
        FileUtil.makeDirs(tempDir);
        try (FileOutputStream fos = new FileOutputStream(tempDir.getAbsolutePath() + File.separator + "pom.xml");
             BufferedOutputStream bos = new BufferedOutputStream(fos)
        ) {
            bos.write(text.getBytes());
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPluginBasePath(Project project) {
        return ProjectInfoUtil.getWebRunnerPluginDir().getAbsolutePath()+ File.separator + "temp" + File.separator + EncryptUtil.encryptMd5(project.getBaseDir().getPresentableUrl() + File.separator + container.name() + version);
    }
}
