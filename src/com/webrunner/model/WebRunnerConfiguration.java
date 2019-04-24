package com.webrunner.model;

import com.intellij.execution.Executor;
import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.WriteExternalException;
import com.webrunner.ui.WebRunnerEditor;
import com.webrunner.util.ParseUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author MarkHuang
 * @version <ul>
 * <li>2018/9/19, MarkHuang,new
 * </ul>
 * @since 2018/9/19
 */
public class WebRunnerConfiguration extends LocatableConfigurationBase implements RunProfileWithCompileBeforeLaunchOption {

    public static final String PREFIX = "JettyRunnerV112-";
    private static final String WEBAPP_PATH_FIELD = PREFIX + "WebAppPath";
    private static final String WEBAPP_FOLDER_FIELD = PREFIX + "WebAppFolder";
    private static final String CLASSES_DIRECTORY_FIELD = PREFIX + "ClassesDirectory";
    private static final String RUN_PORT_FIELD = PREFIX + "RunOnPort";
    private static final String JETTY_XML_FIELD = PREFIX + "JettyXML";
    private static final String VM_ARGS_FIELD = PREFIX + "VmArgs";
    private static final String PASS_PARENT_ENV_VARS_FIELD = PREFIX + "PassParentEnvVars";
    private static final String USE_DEFAULT_ARTIFACT_FIELD = PREFIX + "DefaultArtifact";
    private static final String WEB_CONTAINER_INDEX = PREFIX + "WebContainerIndex";
    private static final String WEB_CONTAINER_VERSION_INDEX = PREFIX + "WebContainerVersionIndex";
    private static final String HOT_SWAP_PORT = PREFIX + "HotSwapPort";

    private String webappPaths;
    private String webappFolders;
    private String classesDirectories;

    private String runningOnPort;
    private String jettyXml;
    private String vmArgs;

    private Map<String, String> environmentVariables = new HashMap<>(0);
    private boolean passParentEnvironmentVariables = false;

    private boolean useDefaultArtifact = true;
    private int webContainerIndex = 0;
    private int webContainerVersionIndex = 0;
    private String hotSwapPort = "random";

    private Project project;
    private String action;

    public WebRunnerConfiguration(Project project, ConfigurationFactory factory, String name) {
        super(project, factory, name);
        this.project = project;
    }

    @Override
    @NotNull
    public SettingsEditor<WebRunnerConfiguration> getConfigurationEditor() {
        // Instantiates a new UI (Conf Window)
        return new WebRunnerEditor(this);
    }

    @Override
    @Nullable
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) {
        // Runner Model
        return new WebRunnerCommandLine(executionEnvironment, this);
    }

    // Persistence of values in disk

    @Override
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);
        // Reads the conf file into this class
        this.webappPaths = JDOMExternalizerUtil.readField(element, WEBAPP_PATH_FIELD);
        this.webappFolders = JDOMExternalizerUtil.readField(element, WEBAPP_FOLDER_FIELD);
        this.classesDirectories = JDOMExternalizerUtil.readField(element, CLASSES_DIRECTORY_FIELD);
        this.runningOnPort = JDOMExternalizerUtil.readField(element, RUN_PORT_FIELD);
        this.jettyXml = JDOMExternalizerUtil.readField(element, JETTY_XML_FIELD);
        this.vmArgs = JDOMExternalizerUtil.readField(element, VM_ARGS_FIELD);
        this.useDefaultArtifact = Boolean.valueOf(JDOMExternalizerUtil.readField(element, USE_DEFAULT_ARTIFACT_FIELD));
        this.webContainerIndex = ParseUtil.saveParseInt(JDOMExternalizerUtil.readField(element, WEB_CONTAINER_INDEX));
        this.webContainerVersionIndex = ParseUtil.saveParseInt(JDOMExternalizerUtil.readField(element, WEB_CONTAINER_VERSION_INDEX));
        this.useDefaultArtifact = Boolean.valueOf(JDOMExternalizerUtil.readField(element, USE_DEFAULT_ARTIFACT_FIELD));
        this.passParentEnvironmentVariables = Boolean.valueOf(JDOMExternalizerUtil.readField(element, PASS_PARENT_ENV_VARS_FIELD));
        this.hotSwapPort = JDOMExternalizerUtil.readField(element, HOT_SWAP_PORT);
        EnvironmentVariablesComponent.readExternal(element, this.environmentVariables);
    }

    @Override
    public void writeExternal(@NotNull Element element) throws WriteExternalException {
        super.writeExternal(element);
        // Stores the values of this class into the parent
        JDOMExternalizerUtil.writeField(element, WEBAPP_PATH_FIELD, this.getWebappPaths());
        JDOMExternalizerUtil.writeField(element, WEBAPP_FOLDER_FIELD, this.getWebappFolders());
        JDOMExternalizerUtil.writeField(element, CLASSES_DIRECTORY_FIELD, this.getClassesDirectories());
        JDOMExternalizerUtil.writeField(element, RUN_PORT_FIELD, this.getRunningOnPort());
        JDOMExternalizerUtil.writeField(element, JETTY_XML_FIELD, this.getJettyXml());
        JDOMExternalizerUtil.writeField(element, VM_ARGS_FIELD, this.getVmArgs());
        JDOMExternalizerUtil.writeField(element, PASS_PARENT_ENV_VARS_FIELD, "" + this.isPassParentEnvironmentVariables());
        JDOMExternalizerUtil.writeField(element, USE_DEFAULT_ARTIFACT_FIELD, "" + this.useDefaultArtifact);
        JDOMExternalizerUtil.writeField(element, WEB_CONTAINER_INDEX, "" + this.webContainerIndex);
        JDOMExternalizerUtil.writeField(element, WEB_CONTAINER_VERSION_INDEX, "" + this.webContainerVersionIndex);
        JDOMExternalizerUtil.writeField(element, HOT_SWAP_PORT, "" + this.getHotSwapPort());
        if (this.environmentVariables != null && !this.environmentVariables.isEmpty()) {
            EnvironmentVariablesComponent.writeExternal(element, this.getEnvironmentVariables());
        }
    }

    @Override
    @NotNull
    public Module[] getModules() {
        ModuleManager moduleManager = ModuleManager.getInstance(this.project);
        return moduleManager.getModules();
    }

    // Getters and Setters

    public String getWebappPaths() {
        return webappPaths;
    }

    public void setWebappPaths(String webappPaths) {
        this.webappPaths = webappPaths;
    }

    public String getWebappFolders() {
        return webappFolders;
    }

    public void setWebappFolders(String webappFolders) {
        this.webappFolders = webappFolders;
    }

    public String getClassesDirectories() {
        return classesDirectories;
    }

    public void setClassesDirectories(String classesDirectories) {
        this.classesDirectories = classesDirectories;
    }

    public String getRunningOnPort() {
        return runningOnPort;
    }

    public void setRunningOnPort(String runningOnPort) {
        this.runningOnPort = runningOnPort;
    }

    public String getJettyXml() {
        return jettyXml;
    }

    public void setJettyXml(String jettyXml) {
        this.jettyXml = jettyXml;
    }

    public String getVmArgs() {
        return vmArgs;
    }

    public void setVmArgs(String vmArgs) {
        this.vmArgs = vmArgs;
    }

    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public void setEnvironmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public boolean isPassParentEnvironmentVariables() {
        return passParentEnvironmentVariables;
    }

    public void setPassParentEnvironmentVariables(boolean passParentEnvironmentVariables) {
        this.passParentEnvironmentVariables = passParentEnvironmentVariables;
    }

    public boolean isUseDefaultArtifact() {
        return useDefaultArtifact;
    }

    public void setUseDefaultArtifact(boolean useDefaultArtifact) {
        this.useDefaultArtifact = useDefaultArtifact;
    }

    public int getWebContainerIndex() {
        return this.webContainerIndex;
    }

    public void setWebContainerIndex(int webContainerIndex) {
        this.webContainerIndex = webContainerIndex;
    }

    public int getWebContainerVersionIndex() {
        return this.webContainerVersionIndex;
    }

    public void setWebContainerVersionIndex(int webContainerVersionIndex) {
        this.webContainerVersionIndex = webContainerVersionIndex;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getHotSwapPort() {
        return hotSwapPort;
    }

    public void setHotSwapPort(String hotSwapPort) {
        this.hotSwapPort = hotSwapPort;
    }

    public WebRunnerConfiguration clone() {
        ConfigurationType webRunner = ConfigurationTypeUtil.findConfigurationType("WebRunner");
        if (webRunner == null) return null;
        ConfigurationFactory configurationFactory = webRunner.getConfigurationFactories()[0];
        WebRunnerConfiguration templateConf =
                (WebRunnerConfiguration) configurationFactory.createTemplateConfiguration(project);
        templateConf.setJettyXml(this.getJettyXml());
        templateConf.setClassesDirectories(this.getClassesDirectories());
        templateConf.setPassParentEnvironmentVariables(this.passParentEnvironmentVariables);
        templateConf.setRunningOnPort(this.getRunningOnPort());
        templateConf.setUseDefaultArtifact(this.useDefaultArtifact);
        templateConf.setVmArgs(this.getVmArgs());
        templateConf.setWebappFolders(this.getWebappFolders());
        templateConf.setWebappPaths(this.getWebappPaths());
        templateConf.setWebContainerIndex(this.getWebContainerIndex());
        templateConf.setWebContainerVersionIndex(this.getWebContainerVersionIndex());
        templateConf.setEnvironmentVariables(this.getEnvironmentVariables());
        return templateConf;
    }
}
