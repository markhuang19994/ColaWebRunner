package com.webrunner.model;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.encoding.EncodingProjectManager;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.elements.CompositePackagingElement;
import com.intellij.packaging.elements.PackagingElement;
import com.intellij.packaging.elements.PackagingElementFactory;
import com.intellij.packaging.impl.elements.ProductionModuleOutputPackagingElement;
import com.webrunner.generator.impl.DefaultCommendLineGenerator;
import com.webrunner.generator.impl.JettyPomCommandLineGenerator;
import com.webrunner.generator.impl.TomcatPomCommandLineGenerator;
import com.webrunner.ui.WebRunnerConfPanel;
import com.webrunner.ui.WebRunnerEditor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author MarkHuang
 * @version <ul>
 * <li>2018/9/19, MarkHuang,new
 * </ul>
 * @since 2018/9/19
 */
public class WebRunnerCommandLine extends JavaCommandLineState {


    private ExecutionEnvironment environment;
    private WebRunnerConfiguration runConf;
    private boolean isDebugMode;

    WebRunnerCommandLine(@NotNull ExecutionEnvironment environment, WebRunnerConfiguration runConf) {
        super(environment);
        this.environment = environment;
        this.runConf = runConf;
        this.isDebugMode = environment.getExecutor().getId().toLowerCase().equals("debug");
    }

    @Override
    public JavaParameters createJavaParameters() throws ExecutionException {
//        updateArtifactModules(runConf.getProject());//會出錯誤
        JavaParameters javaParams = new JavaParameters();
        // Use the same JDK as the project
        Project project = this.environment.getProject();
        javaParams.setCharset(EncodingProjectManager.getInstance(project).getDefaultCharset());
        if (!isDebugMode) {
            String hotSwapPort = runConf.getHotSwapPort();
            if ("random".equals(hotSwapPort)) {
                hotSwapPort = String.valueOf((int) Math.floor(Math.random() * 10000 + 10001));
            }
            javaParams.getVMParametersList().addParametersString(
                    "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=" + hotSwapPort
            );
        }

        // VM Args
        String vmArgs = this.getVmArgs();
        if (vmArgs != null) {
            javaParams.getVMParametersList().addParametersString(vmArgs);
        }
        // Env Vars
        Map<String, String> environmentVariables = this.getEnvVars();
        if (!environmentVariables.isEmpty()) {
            Set<String> keys = environmentVariables.keySet();
            for (String key : keys) {
                String value = environmentVariables.get(key);
                javaParams.getVMParametersList().addProperty(key, value);
            }
        }

        if (runConf.getAction() != null) {
            if (RunnerActionEnum.CLEAN.action.equals(runConf.getAction())) {
                DefaultCommendLineGenerator.getInstance(environment, runConf).configure(javaParams);
                javaParams.getProgramParametersList().add("clean:clean");
                return javaParams;
            }
        }

        if (runConf.getBeforeRunTasks().size() < 2) {
            new WebRunnerEditor(runConf).initBeforeRunTask(runConf);
        }

        WebRunnerConfPanel.SupportWebContainer container = WebRunnerConfPanel.SupportWebContainer.getContainerByIndex(runConf.getWebContainerIndex());
        if (container.name().equalsIgnoreCase("tomcat")) {
            TomcatPomCommandLineGenerator.getInstance(environment, runConf).configure(javaParams);
        } else {
            JettyPomCommandLineGenerator.getInstance(environment, runConf).configure(javaParams);
        }
        return javaParams;
    }

    /**
     * Retrieves the "VM Args" parameter
     *
     * @return String
     */
    private String getVmArgs() {
        String vmArgs = runConf.getVmArgs();
        return vmArgs != null && !vmArgs.isEmpty() ? vmArgs : null;
    }

    /**
     * Retrieves the Env Vars
     *
     * @return Map<String, String>
     */
    private Map<String, String> getEnvVars() {
        Map<String, String> environmentVariables = runConf.getEnvironmentVariables();
        if (environmentVariables != null && !environmentVariables.isEmpty()) {
            return runConf.getEnvironmentVariables();
        }
        return new HashMap<>(0);
    }

    private static List<Module> nowUseModules = new ArrayList<>();

    private boolean isModulesAddOrRemove(List<Module> modules) {
        if (nowUseModules.size() != modules.size()) {
            return true;
        }
        List<String> moduleNames = modules.stream().map(Module::getName).collect(Collectors.toList());
        List<String> nowUseModuleNames = nowUseModules.stream().map(Module::getName).collect(Collectors.toList());

        for (String nowName : nowUseModuleNames) {
            moduleNames.remove(nowName);
        }
        return moduleNames.size() != 0;
    }

    private void updateArtifactModules(Project project) {
        List<Module> modules =
                Arrays.asList(ModuleManager.getInstance(project).getModules());
        if (nowUseModules.size() > 0 && !isModulesAddOrRemove(modules)) {
            return;
        }
        List<Module> inLibModules = new ArrayList<>();
        List<Module> inClassesModules = new ArrayList<>();

        for (Module module : modules) {
            if (WebRunnerEditor.isModuleNeedPackageToJar(module)) {
                inLibModules.add(module);
                continue;
            }
            inClassesModules.add(module);
        }

        Artifact artifact = WebRunnerEditor.getRunnerArtifact(project);
        if (artifact == null) return;
        CompositePackagingElement<?> rootElement = artifact.getRootElement();
        CompositePackagingElement<?> webInf = null;
        for (PackagingElement<?> child : rootElement.getChildren()) {
            if (child instanceof CompositePackagingElement) {
                CompositePackagingElement cChild = (CompositePackagingElement) child;
                if (cChild.getName().equals("WEB-INF")) {
                    webInf = cChild;
                }
            }
        }
        if (webInf == null) return;
        CompositePackagingElement<?> classes = null;
        CompositePackagingElement<?> lib = null;
        for (PackagingElement<?> child : webInf.getChildren()) {
            if (child instanceof CompositePackagingElement) {
                CompositePackagingElement<?> cChild = (CompositePackagingElement<?>) child;
                switch (cChild.getName()) {
                    case "classes":
                        classes = cChild;
                        break;
                    case "lib":
                        lib = cChild;
                        break;
                    default:
                        break;
                }
            }
        }

        PackagingElementFactory factory = PackagingElementFactory.getInstance();
        if (classes != null) {
            for (PackagingElement<?> child : classes.getChildren()) {
                if (child instanceof ProductionModuleOutputPackagingElement) {
                    ProductionModuleOutputPackagingElement pChild =
                            (ProductionModuleOutputPackagingElement) child;
                    inClassesModules.removeIf(module -> module.getName().equals(pChild.getModuleName()));
                }
            }
            for (Module module : inClassesModules) {
                classes.addFirstChild(factory.createModuleOutput(module));
            }
        }

        if (lib != null) {
            for (PackagingElement<?> child : lib.getChildren()) {
                if (child instanceof CompositePackagingElement) {
                    CompositePackagingElement cChild = (CompositePackagingElement) child;
                    if (cChild.getName().contains(".jar")) {
                        inLibModules.removeIf(module ->
                                module.getName().equals(cChild.getName().replace(".jar", "")));
                    }
                }
            }
            for (Module module : inLibModules) {
                CompositePackagingElement cpe = factory.createArchive(module.getName() + ".jar");
                cpe.addFirstChild(factory.createModuleOutput(module));
                lib.addFirstChild(cpe);
            }
        }
        nowUseModules = new ArrayList<>(modules);
    }
}
