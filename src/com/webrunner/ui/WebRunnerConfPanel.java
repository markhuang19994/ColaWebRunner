package com.webrunner.ui;

import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.webrunner.util.JavaSwingUtil;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * View / Presentation - Created using the WYSIWYG editor.
 * Used the JGoodies Form Layout - which is BSD.
 *
 * @author Gui Keller
 */
public class WebRunnerConfPanel {

    private JPanel mainPanel;
    private JTextField pathField;
    private JTextField webappField;
    private JTextField classesField;
    private JTextField runOnPortField;
    private JTextField xmlField;
    private JButton browseButton;
    private JTextField vmArgsField;
    private EnvironmentVariablesComponent environmentVariables;
    private JLabel spacerLabel;
    private JLabel vmArgsLabel;
    private JLabel firstMsgLabel;
    private JLabel xmlLabel;
    private JLabel runOnPortLabel;
    private JLabel classesLabel;
    private JLabel webappLabel;
    private JLabel pathLabel;
    private JLabel secondMsgLabel;
    private JLabel envVarLabel;
    private JCheckBox useDefaultArtifactCheckBox;
    private JComboBox webContainer;
    private JComboBox containerVersion;
    private JTextField hotSwapPort;

    public WebRunnerConfPanel() {
        // Action executed when clicked on "Browse XML"
        browseButton.addActionListener(e -> {
            // Shows a file chooser
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(Boolean.TRUE);
            // Checking whether the user clicked okay
            int result = fileChooser.showOpenDialog(new JFrame());
            if (result == JFileChooser.APPROVE_OPTION) {
                StringBuffer paths = new StringBuffer();
                File[] selectedFiles = fileChooser.getSelectedFiles();
                if (selectedFiles != null && selectedFiles.length > 0) {
                    for (File selectedFile : selectedFiles) {
                        // Selected files in CSV format
                        paths.append(selectedFile.getAbsolutePath() + ",");
                    }
                    // Removing the comma at the end
                    String value = paths.substring(0, (paths.length() - 1));
                    xmlField.setText(value);
                }
            }
        });
        webContainer.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Object item = e.getItem();
                Optional.ofNullable(SupportWebContainer.getTypeByName(item.toString()))
                        .ifPresent(supportWebContainer -> {
                            List<String> versions = supportWebContainer.getVersions();
                            JavaSwingUtil.setComboBox(this.containerVersion, versions.toArray());
                        });
            }
        });
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JTextField getPathField() {
        return pathField;
    }

    public JTextField getWebappField() {
        return webappField;
    }

    public JTextField getClassesField() {
        return classesField;
    }

    public JTextField getRunOnPortField() {
        return runOnPortField;
    }

    public JTextField getXmlField() {
        return xmlField;
    }

    public JTextField getVmArgsField() {
        return vmArgsField;
    }

    public EnvironmentVariablesComponent getEnvironmentVariables() {
        return environmentVariables;
    }

    public JCheckBox getUseDefaultArtifactCheckBox() {
        return useDefaultArtifactCheckBox;
    }

    public JComboBox getWebContainer() {
        return webContainer;
    }

    public JComboBox getContainerVersion() {
        return containerVersion;
    }

    public JTextField getHotSwapPort() {
        return hotSwapPort;
    }

    public enum SupportWebContainer {
        JETTY("6.1.26", "9.2.16.v20160414"), TOMCAT("2.1");

        private List<String> versions;

        SupportWebContainer(String... version) {
            this.versions = Arrays.asList(version);
        }

        public List<String> getVersions() {
            return this.versions;
        }

        public static SupportWebContainer getContainerByIndex(int index) {
            SupportWebContainer[] values = SupportWebContainer.values();
            if (index > values.length - 1) {
                return values[0];
            }
            return values[index];
        }

        public static String getContainerVersionByIndex(SupportWebContainer container, int index) {
            if (index > container.versions.size() - 1) {
                return container.versions.get(0);
            }
            return container.versions.get(index);
        }

        public static String[] getNames() {
            SupportWebContainer[] values = SupportWebContainer.values();
            return Arrays.stream(values)
                    .map(Enum::name)
                    .collect(Collectors.toList())
                    .toArray(new String[]{});
        }

        public static SupportWebContainer getTypeByName(String name) {
            SupportWebContainer[] values = SupportWebContainer.values();
            return Arrays.stream(values)
                    .filter(value -> value.name().toString().equalsIgnoreCase(name))
                    .findFirst()
                    .orElse(null);
        }
    }
}
