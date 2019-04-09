package com.webrunner.startup;

import com.intellij.ProjectTopics;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunManagerListener;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.elements.CompositePackagingElement;
import com.intellij.packaging.elements.PackagingElement;
import com.intellij.packaging.elements.PackagingElementFactory;
import com.intellij.packaging.impl.elements.ProductionModuleOutputPackagingElement;
import com.webrunner.ui.WebRunnerEditor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author MarkHuang
 * @version <ul>
 * <li>2018/10/8, MarkHuang,new
 * </ul>
 * @since 2018/10/8
 */
public class MyPostStartupActivity implements StartupActivity {
    private List<String> artifactLibNames;

    public void runActivity(@NotNull Project project) {
        try{
            ProjectRootManager projectRoot = ProjectRootManager.getInstance(project);
            if (projectRoot.getProjectSdk() == null) {
                Sdk sdk = Arrays.stream(ProjectJdkTable.getInstance().getAllJdks())
                        .min((o1, o2) -> String.valueOf(o1.getVersionString()).contains("1.8") ? 0 : 1)
                        .orElse(null);
                projectRoot.setProjectSdk(sdk);
            }
            updateArtifactLibOnModuleAddNewLib(project);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void updateArtifactLibOnModuleAddNewLib(Project project) {
        project.getMessageBus().connect(project).subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
            @Override
            public void rootsChanged(ModuleRootEvent e) {
                new Thread(() -> {
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    if (e.getSource() instanceof Project) {
                        Project project = (Project) e.getSource();
                        Artifact artifact = WebRunnerEditor.getRunnerArtifact(project);
                        PackagingElementFactory factory = PackagingElementFactory.getInstance();
                        boolean isNeedResetArtifactLib = true;
                        Library[] libraries = LibraryTablesRegistrar.getInstance().getLibraryTable(project).getLibraries();
                        if (artifactLibNames != null) {
                            List<String> newLibNames = Arrays.stream(libraries).map(Library::getName).collect(Collectors.toList());
                            isNeedResetArtifactLib = !newLibNames.equals(artifactLibNames);
                        }
                        if (isNeedResetArtifactLib && artifact != null) {
                            CompositePackagingElement<?> webInf = artifact.getRootElement().findCompositeChild("WEB-INF");
                            if (webInf != null) {
                                CompositePackagingElement<?> lib = webInf.findCompositeChild("lib");
                                if (lib != null) {
                                    webInf.removeChild(lib);
                                    CompositePackagingElement<?> newLib = factory.createDirectory("lib");
                                    Arrays.stream(libraries).forEach(library -> factory
                                            .createLibraryElements(library)
                                            .forEach(newLib::addFirstChild)
                                    );
                                    artifactLibNames = Arrays.stream(libraries).map(Library::getName).collect(Collectors.toList());
                                    webInf.addFirstChild(newLib);
                                }
                            }
                        }
                    }
                }).start();
            }

            @Override
            public void beforeRootsChange(ModuleRootEvent event) {
                System.out.println("WeaveEditorToolingAPI.beforeRootsChange");
            }
        });
    }

    public void addRunManagerListener(Project project) {
        RunManagerEx.getInstanceEx(project).addRunManagerListener(new RunManagerListener() {

            @Override
            public void runConfigurationSelected() {
                Project[] projects = ProjectManager.getInstance().getOpenProjects();
                Project nowProject = Arrays.stream(projects)
                        .filter(newProject -> newProject.getName().equals(project.getName()))
                        .findFirst()
                        .orElse(project);

                RunnerAndConfigurationSettings conf = RunManagerEx.getInstanceEx(nowProject).getSelectedConfiguration();
            }
        });
    }
}
