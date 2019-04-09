package com.webrunner.generator.impl;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import com.webrunner.model.WebRunnerConfiguration;
import com.webrunner.ui.WebRunnerConfPanel;
import com.webrunner.util.ProjectInfoUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static com.webrunner.ui.WebRunnerConfPanel.SupportWebContainer.getContainerByIndex;
import static com.webrunner.ui.WebRunnerConfPanel.SupportWebContainer.getContainerVersionByIndex;

/**
 * @author MarkHuang
 * @version <ul>
 * <li>2018/9/27, MarkHuang,new
 * </ul>
 * @since 2018/9/27
 */
public class JettyPomCommandLineGenerator extends AbstractPomCommandLineGenerator {
    private JettyPomCommandLineGenerator(@NotNull ExecutionEnvironment environment, WebRunnerConfiguration runConf) {
        super(environment, runConf);
    }

    public static JettyPomCommandLineGenerator getInstance(@NotNull ExecutionEnvironment environment, WebRunnerConfiguration model) {
        return new JettyPomCommandLineGenerator(environment, model);
    }

    @Override
    public void configure(JavaParameters javaParams) throws ExecutionException {
        writeJettyPom();
        super.configure(javaParams);
        javaParams.getProgramParametersList().add("jetty:run");
    }

    private void writeJettyPom() {
        Project project = super.environment.getProject();
        String jettyPom = generateJettyPom();
        JettyPluginEnvGenerator templateGenerator = JettyPluginEnvGenerator.getInstance(runConf.getWebContainerIndex(), runConf.getWebContainerVersionIndex());
        templateGenerator.writeTemplateText(project, jettyPom);
    }

    /**
     * Generate jetty pom.xml from template
     *
     * @return String
     */
    private String generateJettyPom() {
        String target = ProjectInfoUtil.getArtifactOutputFileDir(runConf);
        String contextPath = runConf.getWebappPaths();
        String webXmlDirectory = runConf.getWebappFolders().equals("") ? target + File.separator + "WEB-INF" : runConf.getWebappFolders();
        String classesDirectory = runConf.getClassesDirectories().equals("") ? target + File.separator + "WEB-INF" + File.separator + "classes" : runConf.getClassesDirectories();
        WebRunnerConfPanel.SupportWebContainer supContainer = getContainerByIndex(runConf.getWebContainerIndex());
        int versionIndex = runConf.getWebContainerVersionIndex();
        JettyPluginEnvGenerator tempGenerator = JettyPluginEnvGenerator.getInstance(runConf.getWebContainerIndex(), versionIndex);
        String template = tempGenerator.getTemplateFileText();
        template = template.replace("${version}", getContainerVersionByIndex(supContainer, versionIndex));
        template = template.replace("${webAppSourceDirectory}", target.replaceAll("\\\\","/"));
        template = template.replace("${webXmlDirectory}", webXmlDirectory.replaceAll("\\\\","/"));
        template = template.replace("${contextPath}", contextPath);
        template = template.replace("${classesDirectory}", classesDirectory.replaceAll("\\\\","/"));
        template = template.replace("${port}", runConf.getRunningOnPort());
        return template;
    }
}
