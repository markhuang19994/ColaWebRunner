package com.webrunner.generator.impl;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.PsiShortNamesCache;
import com.webrunner.model.WebRunnerConfiguration;
import com.webrunner.ui.WebRunnerConfPanel;
import com.webrunner.util.FileUtil;
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
public class TomcatPomCommandLineGenerator extends AbstractPomCommandLineGenerator {
    private TomcatPluginEnvGenerator generator;

    private TomcatPomCommandLineGenerator(@NotNull ExecutionEnvironment environment, WebRunnerConfiguration runConf) {
        super(environment, runConf);
        generator = TomcatPluginEnvGenerator.getInstance(runConf.getWebContainerIndex(), runConf.getWebContainerVersionIndex());
    }

    public static TomcatPomCommandLineGenerator getInstance(@NotNull ExecutionEnvironment environment, WebRunnerConfiguration model) {
        return new TomcatPomCommandLineGenerator(environment, model);
    }

    @Override
    public void configure(JavaParameters javaParams) throws ExecutionException {
        writeTomcatPom();
        copyServerXml();
        copyContextXml();
        super.configure(javaParams);
        javaParams.getProgramParametersList().add("tomcat7:run");
    }

    private void writeTomcatPom() {
        Project project = super.environment.getProject();
        String tomcatPom = generateTomcatPom();
        TomcatPluginEnvGenerator templateProvider = TomcatPluginEnvGenerator.getInstance(runConf.getWebContainerIndex(), runConf.getWebContainerVersionIndex());
        templateProvider.writeTemplateText(project, tomcatPom);
    }

    /**
     * Generate tomcat pom.xml from template
     *
     * @return String
     */
    private String generateTomcatPom() {
        String target = ProjectInfoUtil.getArtifactOutputFileDir(runConf);
        int versionIndex = runConf.getWebContainerVersionIndex();
        WebRunnerConfPanel.SupportWebContainer supContainer = getContainerByIndex(runConf.getWebContainerIndex());
        TomcatPluginEnvGenerator tempGenerator = TomcatPluginEnvGenerator.getInstance(runConf.getWebContainerIndex(), versionIndex);
        String template = tempGenerator.getTemplateFileText();
        template = template.replace("${version}", getContainerVersionByIndex(supContainer, versionIndex));
        template = template.replace("${serverXmlPath}", generator.getPluginBasePath(runConf.getProject()) + File.separator + "server.xml");
        return template;
    }

    private void copyServerXml() throws ExecutionException {
        PsiShortNamesCache psnc = PsiShortNamesCache.getInstance(runConf.getProject());
        PsiFile[] serverXml = psnc.getFilesByName("server.xml");
        if (serverXml.length == 0) {
            throw new ExecutionException("can't found server.xml in project, please add one!");
        }
        FileUtil.copyFile(new File(serverXml[0].getVirtualFile().getPresentableUrl()), generator.getPluginBasePath(runConf.getProject()) + File.separator + "server.xml");
    }

    private void copyContextXml() throws ExecutionException {
        PsiShortNamesCache psnc = PsiShortNamesCache.getInstance(runConf.getProject());
        PsiFile[] contextXml = psnc.getFilesByName("context.xml");
        if (contextXml.length == 0) {
            throw new ExecutionException("can't found context.xml in project, please add one!");
        }
        String contextXmlPath = generator.getPluginBasePath(runConf.getProject()) + "\\target" + "\\tomcat" + "\\conf" + "\\context.xml";
        FileUtil.makeDirs(new File(contextXmlPath).getParentFile());
        FileUtil.copyFile(new File(contextXml[0].getVirtualFile().getPresentableUrl()), contextXmlPath);
    }
}
