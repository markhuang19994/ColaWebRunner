package com.webrunner.ui;

import com.intellij.compiler.options.CompileStepBeforeRun;
import com.intellij.execution.BeforeRunTask;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.packaging.artifacts.*;
import com.intellij.packaging.elements.CompositePackagingElement;
import com.intellij.packaging.elements.PackagingElement;
import com.intellij.packaging.elements.PackagingElementFactory;
import com.intellij.packaging.impl.artifacts.ArtifactImpl;
import com.intellij.packaging.impl.run.BuildArtifactsBeforeRunTask;
import com.intellij.packaging.impl.run.BuildArtifactsBeforeRunTaskProvider;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.PsiShortNamesCache;
import com.webrunner.conf.WebRunnerConfigurationType;
import com.webrunner.generator.impl.DefaultPluginEnvGenerator;
import com.webrunner.model.WebRunnerConfiguration;
import com.webrunner.util.FileUtil;
import com.webrunner.util.JavaSwingUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WebRunnerEditor extends SettingsEditor<WebRunnerConfiguration> {

    private static final String RUNNER_ARTIFACT_NAME = "web-runner-artifact:exploded-war";
    private WebRunnerConfPanel configurationPanel;

    public WebRunnerEditor(WebRunnerConfiguration webRunnerConfiguration) {
        this.configurationPanel = new WebRunnerConfPanel();
        super.resetFrom(webRunnerConfiguration);
    }

    /**
     * This is invoked when the form is first loaded.
     * The values may be stored in disk, if not, set some defaults
     *
     * @param webRunnerConfiguration jettyRunnerConfiguration
     */
    @Override
    protected void resetEditorFrom(@NotNull WebRunnerConfiguration webRunnerConfiguration) {
        Project project = webRunnerConfiguration.getProject();
        // context Path
        String contextPath = webRunnerConfiguration.getWebappPaths();
        if (contextPath != null && !"".equals(contextPath.trim())) {
            this.configurationPanel.getPathField().setText(contextPath);
        } else {
            contextPath = getColaWebProjectContextPath(project);
            contextPath = "".equals(contextPath) ? project.getName() : contextPath;
            this.configurationPanel.getPathField().setText("/" + contextPath);
        }
        // WebApp Folder (one level down to web.xml"
        String webappFolders = webRunnerConfiguration.getWebappFolders();
        String webModelName = getColaWebAppModelName(project);
        if (webappFolders != null && !"".equals(webappFolders.trim())) {
            this.configurationPanel.getWebappField().setText(webappFolders);
        } else {
//            String webAppsFolder = project.getBaseDir().getPresentableUrl() + "\\" + webModelName + "\\target\\" + contextPath + "\\";
//            this.configurationPanel.getWebappField().setText(webAppsFolder);
        }
        // Classes directory
        String classesDirectories = webRunnerConfiguration.getClassesDirectories();
        if (classesDirectories != null && !"".equals(classesDirectories.trim())) {
            this.configurationPanel.getClassesField().setText(classesDirectories);
        } else {
//            String outputDirectory = project.getBaseDir().getPresentableUrl() + "\\" + webModelName + "\\target\\" + webModelName + "\\WEB-INF\\classes";
//            this.configurationPanel.getClassesField().setText(outputDirectory);
        }
        // Runs on port
        String runningOnPort = webRunnerConfiguration.getRunningOnPort();
        if (runningOnPort != null && !"".equals(runningOnPort)) {
            this.configurationPanel.getRunOnPortField().setText(runningOnPort);
        } else {
            String port = String.valueOf(getColaHttpPortByContextPath(contextPath));
            this.configurationPanel.getRunOnPortField().setText(port);
        }
        // Hot swap port
        String hotSwapPort = webRunnerConfiguration.getHotSwapPort();
        if (hotSwapPort != null && !"".equals(hotSwapPort)) {
            this.configurationPanel.getHotSwapPort().setText(hotSwapPort);
        } else {
            this.configurationPanel.getHotSwapPort().setText("4000");
        }
        // Jetty XML (Optional)
        this.configurationPanel.getXmlField().setText(webRunnerConfiguration.getJettyXml());
        // Env Vars (Optional)
        Map<String, String> environmentVariables = webRunnerConfiguration.getEnvironmentVariables();
        if (environmentVariables != null && !environmentVariables.isEmpty()) {
            this.configurationPanel.getEnvironmentVariables().setEnvs(environmentVariables);
        }
        // Vm Args (Optional)
        this.configurationPanel.getVmArgsField().setText(webRunnerConfiguration.getVmArgs());
        this.configurationPanel.getUseDefaultArtifactCheckBox().setSelected(webRunnerConfiguration.isUseDefaultArtifact());

        int webContainerIndex = webRunnerConfiguration.getWebContainerIndex();
        JavaSwingUtil.setComboBox(this.configurationPanel.getWebContainer(), WebRunnerConfPanel.SupportWebContainer.getNames(), webContainerIndex);

        int webContainerVersionIndex = webRunnerConfiguration.getWebContainerVersionIndex();
        WebRunnerConfPanel.SupportWebContainer nowChooseContainer = WebRunnerConfPanel.SupportWebContainer.getContainerByIndex(webContainerIndex);
        JavaSwingUtil.setComboBox(this.configurationPanel.getContainerVersion(), nowChooseContainer.getVersions().toArray(), webContainerVersionIndex);
        if (isRunnerArtifactNotInBeforeRunTask(webRunnerConfiguration)) {
            initBeforeRunTask(webRunnerConfiguration);
        }
//        updateArtifactOutDir(webRunnerConfiguration);
    }

    private static int containerIndexCache = 0;
    private static int containerVersionCache = 0;

    /**
     * This is invoked when the user fills the form and pushes apply/ok
     *
     * @param runnerConf runnerConfiguration
     */
    @Override
    protected void applyEditorTo(@NotNull WebRunnerConfiguration runnerConf) {
        Project project = runnerConf.getProject();
        int containerIndex = this.configurationPanel.getWebContainer().getSelectedIndex();
        int containerVersion = this.configurationPanel.getContainerVersion().getSelectedIndex();
        runnerConf.setWebappPaths(this.configurationPanel.getPathField().getText());
        runnerConf.setWebappFolders(this.configurationPanel.getWebappField().getText());
        runnerConf.setClassesDirectories(this.configurationPanel.getClassesField().getText());
        runnerConf.setRunningOnPort(this.configurationPanel.getRunOnPortField().getText());
        runnerConf.setJettyXml(this.configurationPanel.getXmlField().getText());
        runnerConf.setVmArgs(this.configurationPanel.getVmArgsField().getText());
        runnerConf.setPassParentEnvironmentVariables(this.configurationPanel.getEnvironmentVariables().isPassParentEnvs());
        runnerConf.setUseDefaultArtifact(this.configurationPanel.getUseDefaultArtifactCheckBox().isSelected());
        runnerConf.setWebContainerIndex(containerIndex);
        runnerConf.setWebContainerVersionIndex(containerVersion);
        runnerConf.setHotSwapPort(this.configurationPanel.getHotSwapPort().getText());
        // Deals with adding / removing env vars before saving to the conf file
        Map<String, String> envVars = this.configurationPanel.getEnvironmentVariables().getEnvs();
        addOrRemoveEnvVar(runnerConf.getEnvironmentVariables(), envVars);
        initBeforeRunTask(runnerConf);
        try {
            runnerConf.writeExternal(new Element(WebRunnerConfiguration.PREFIX + UUID.randomUUID().toString()));
        } catch (WriteExternalException e) {
            throw new RuntimeException(e);
        }
        updateArtifactOutDir(runnerConf);
    }

    private void updateArtifactOutDir(WebRunnerConfiguration runnerConf) {
        int containerIndex = this.configurationPanel.getWebContainer().getSelectedIndex();
        int containerVersion = this.configurationPanel.getContainerVersion().getSelectedIndex();
        Project project = runnerConf.getProject();
        Artifact artifact = getRunnerArtifact(project);
        if (artifact != null && (containerIndexCache != containerIndex || containerVersionCache != containerVersion)) {
            DefaultPluginEnvGenerator envGenerator = new DefaultPluginEnvGenerator(runnerConf.getWebContainerIndex(), runnerConf.getWebContainerVersionIndex());
            String pluginBasePath = envGenerator.getPluginBasePath(project);
            ((ArtifactImpl) artifact).setOutputPath(pluginBasePath + File.separator + "target" + runnerConf.getWebappPaths());
            containerIndexCache = containerIndex;
            containerVersionCache = containerVersion;
        }
    }

    public void initBeforeRunTask(WebRunnerConfiguration runnerConf) {
        Project project = runnerConf.getProject();
        String contextPath = runnerConf.getWebappPaths();
        if (contextPath == null) {
            return;
        }
        //add run jetty necessary artifact to this model
        if (runnerConf.isUseDefaultArtifact()) {
            Artifact artifact = getRunnerArtifact(project);
            if (artifact == null) {
                artifact = createArtifact(runnerConf);
                addArtifact(runnerConf, artifact);
            }

            addBeforeRunTask(runnerConf, new CompileStepBeforeRun.MakeBeforeRunTask());

            if (isRunnerArtifactNotInBeforeRunTask(runnerConf)) {
                BuildArtifactsBeforeRunTaskProvider.setBuildArtifactBeforeRun(project, runnerConf, artifact);
            }

            Optional<RunnerAndConfigurationSettings> first = RunManagerEx
                    .getInstance(project)
                    .getAllSettings()
                    .stream()
                    .filter(ins -> WebRunnerConfigurationType.class.getSimpleName().equals(ins.getConfiguration().getType().getClass().getSimpleName()))
                    .findFirst();
            first.ifPresent(runnerAndConfigurationSettings ->
                    runnerAndConfigurationSettings
                            .getConfiguration()
                            .setBeforeRunTasks(runnerConf.getBeforeRunTasks())
            );
        }
    }

    /**
     * Check before run task is already exits, if not add task in configuration
     *
     * @param configuration RunConfiguration
     * @param runTask       BeforeRunTask
     */
    @SuppressWarnings("unchecked")
    private void addBeforeRunTask(RunConfiguration configuration, BeforeRunTask runTask) {
        Project project = configuration.getProject();
        Key id = runTask.getProviderId();
        RunManagerEx runManagerEx = RunManagerEx.getInstanceEx(project);
        if (runManagerEx.getBeforeRunTasks(configuration, id).isEmpty()) {
            ArrayList newBeforeTasks = new ArrayList(runManagerEx.getBeforeRunTasks(configuration));
            newBeforeTasks.add(runTask);
            runTask.setEnabled(true);
            runManagerEx.setBeforeRunTasks(configuration, newBeforeTasks);
        }
    }

    /**
     * Check module has jetty artifact
     *
     * @param project Project
     * @return Artifact/null
     */
    public static Artifact getRunnerArtifact(Project project) {
        ArtifactManager artManager = ArtifactManager.getInstance(project);
        Optional<Artifact> first = Arrays.stream(artManager.getArtifacts())
                .filter(art -> RUNNER_ARTIFACT_NAME.equals(art.getName()))
                .findFirst();
        return first.orElse(null);
    }

    @SuppressWarnings("unchecked")
    private boolean isRunnerArtifactNotInBeforeRunTask(WebRunnerConfiguration runnerConf) {
        List<ArtifactPointer> list = new ArrayList<>();
        runnerConf
                .getBeforeRunTasks()
                .stream()
                .filter(task -> task instanceof BuildArtifactsBeforeRunTask)
                .map(task -> ((BuildArtifactsBeforeRunTask) task).getArtifactPointers())
                .forEach(x -> list.addAll(((List<ArtifactPointer>) x)));
        List<BeforeRunTask<?>> beforeRunTasks = runnerConf.getBeforeRunTasks();
        return list.stream().noneMatch(pointer -> pointer.getArtifactName().equals(RUNNER_ARTIFACT_NAME));
    }

    /**
     * Create artifact for this plugin
     * structure:
     * |-META-INF
     * |-WEB-INF
     * |-classes
     * |-lib
     * |-dummy web facet <- now intellij sdk can't get web facet, so use dummy facet dir alternative
     *
     * @param webRunnerConfiguration WebRunnerConfiguration
     * @return Artifact
     */
    private Artifact createArtifact(WebRunnerConfiguration webRunnerConfiguration) {
        Project project = webRunnerConfiguration.getProject();
        ArtifactType artType = null;
        for (ArtifactType art : ArtifactType.getAllTypes()) {
            if (art.getId().equals("exploded-war")) {
                artType = art;
            }
        }

        PackagingElementFactory factory = PackagingElementFactory.getInstance();
        CompositePackagingElement<?> root = factory.createArtifactRootElement();
        CompositePackagingElement<?> metaInf = factory.createDirectory("META-INF");
        CompositePackagingElement<?> wInf = factory.createDirectory("WEB-INF");
        CompositePackagingElement<?> classes = factory.createDirectory("classes");
        CompositePackagingElement<?> lib = factory.createDirectory("lib");
        PackagingElement<?> webFacetFiles = null;
        PsiShortNamesCache psiNameCache = PsiShortNamesCache.getInstance(project);
        PsiFile[] pfs = psiNameCache.getFilesByName("web.xml");
        if (pfs.length > 0) {
            File webInf = FileUtil.getParentDir(new File(pfs[0].getVirtualFile().getPresentableUrl()), "WEB-INF");
            if (webInf != null) {
                File webappDir = webInf.getParentFile();
                if (webappDir.exists()) {
                    webFacetFiles = factory.createDirectoryCopyWithParentDirectories(webappDir.getAbsolutePath(), "");
                }
            }
        }

        PsiFile[] metaFiles = psiNameCache.getFilesByName("MANIFEST.MF");
        if (metaFiles.length > 0) {
            metaInf.addFirstChild(factory.createFileCopy(metaFiles[0].getVirtualFile().getPresentableUrl(), "MANIFEST.MF"));
            root.addFirstChild(metaInf);
        }

        List<Module> modules = Arrays.asList(ModuleManager.getInstance(project).getModules());
        for (Module m : modules) {
            classes.addFirstChild(factory.createModuleOutput(m));
        }

        List<String> modelNames = modules.stream().map(Module::getName).collect(Collectors.toList());
        Library[] libraries = LibraryTablesRegistrar.getInstance().getLibraryTable(project).getLibraries();
        if (libraries.length > 0) {
            Arrays.stream(libraries)
                    .filter(library -> !modelNames.contains(library.getName()))
                    .forEach(
                            library -> factory
                                    .createLibraryElements(library)
                                    .forEach(lib::addFirstChild)
                    );
        }

        wInf.addFirstChild(classes);
        wInf.addFirstChild(lib);
        root.addFirstChild(wInf);
        if (webFacetFiles != null) {
            root.addFirstChild(webFacetFiles);
        }

        assert artType != null;
        DefaultPluginEnvGenerator envGenerator = new DefaultPluginEnvGenerator(webRunnerConfiguration.getWebContainerIndex(), webRunnerConfiguration.getWebContainerVersionIndex());
        String pluginBasePath = envGenerator.getPluginBasePath(project);
        return new ArtifactImpl(RUNNER_ARTIFACT_NAME, artType, true, root, pluginBasePath + File.separator + "target" + webRunnerConfiguration.getWebappPaths(), null);
    }

    /**
     * Add artifact in project structure Artifact
     *
     * @param webRunnerConfiguration WebRunnerConfiguration
     * @param artifact               Artifact
     */
    private void addArtifact(WebRunnerConfiguration webRunnerConfiguration, Artifact artifact) {
        ArtifactManager artifactManager = ArtifactManager.getInstance(webRunnerConfiguration.getProject());
        WriteAction.compute(() -> {
            ModifiableArtifactModel model = artifactManager.createModifiableModel();
            ModifiableArtifact modArtifact = model.addArtifact(artifact.getName(), artifact.getArtifactType());
            modArtifact.setRootElement(artifact.getRootElement());
            modArtifact.setOutputPath(artifact.getOutputPath());
            model.commit();
            return modArtifact;
        });
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return this.configurationPanel.getMainPanel();
    }


    /**
     * Adds / removes variables to the System Environment Variables
     *
     * @param currentVars Map<String,String>
     * @param newVars     Map<String,String>
     */
    private void addOrRemoveEnvVar(Map<String, String> currentVars, Map<String, String> newVars) {
        // Removes the current env vars
        if (currentVars != null && !currentVars.isEmpty()) {
            Set<String> keys = currentVars.keySet();
            for (String key : keys) {
                System.clearProperty(key);
            }
        }
        // Adds the new env vars
        if (newVars != null && !newVars.isEmpty()) {
            Set<String> keys = newVars.keySet();
            for (String key : keys) {
                String value = newVars.get(key);
                System.setProperty(key, value);
            }
        }
    }

    /**
     * Return the context path project may use
     *
     * @param project Project
     * @return String value
     */
    private String getColaWebProjectContextPath(Project project) {
        PsiShortNamesCache psnc = PsiShortNamesCache.getInstance(project);
        PsiFile[] pfs = psnc.getFilesByName("pom.xml");
        Pattern extPattern = Pattern.compile("extfunc\\d+");
        String contextPath = "";
        String alternateContextPath = "";
        for (PsiFile pf : pfs) {
            String text = pf.getText();
            Matcher m = extPattern.matcher(text);
            Matcher m1 = extPattern.matcher(text);
            if (m.find()) {
                contextPath = m.group();
                break;
            } else if (m1.find()) {
                alternateContextPath = m1.group(1);
            }
        }
        contextPath = contextPath.equals("") ? alternateContextPath : contextPath;
        return contextPath;
    }

    /**
     * If context path is extfuncxx where xx is digit, return port = 8080 + xx
     *
     * @param contextPath String
     * @return String
     */
    private int getColaHttpPortByContextPath(String contextPath) {
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(contextPath);
        if (m.find()) {
            return Integer.parseInt(m.group()) + 8000;
        }
        return 8080;
    }

    /**
     * Return Cola project web app model name
     *
     * @param project project
     * @return String value
     */
    private String getColaWebAppModelName(Project project) {
        PsiShortNamesCache psnc = PsiShortNamesCache.getInstance(project);
        PsiFile[] pfs = psnc.getFilesByName("web.xml");
        PsiDirectory webModel = null;
        for (PsiFile pf : pfs) {
            PsiDirectory src = getParentDir(pf, "src");
            if (src != null) {
                webModel = src.getParent();
                break;
            }
        }
        return webModel == null ? "" : webModel.getName();
    }

    /**
     * Get parent dir with figure name
     *
     * @param pf      PsiFile
     * @param dirName dirName
     * @return PsiDirectory
     */
    private PsiDirectory getParentDir(PsiFile pf, String dirName) {
        PsiDirectory temp = pf.getParent();
        while (temp != null) {
            if (temp.getName().equals(dirName)) {
                return temp;
            }
            temp = temp.getParent();
        }
        return null;
    }

    public static boolean isModuleNeedPackageToJar(Module module) {
        return !(module.getName().contains("web")
                || module.getName().contains("config")
                || module.getName().contains("common"));
    }

}
