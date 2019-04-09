package com.webrunner.util;

import com.intellij.openapi.graph.impl.option.ColorListCellRendererImpl;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.ListCellRendererWrapper;

import javax.swing.*;

/**
 * @author MarkHuang
 * @version <ul>
 * <li>2018/9/26, MarkHuang,new
 * </ul>
 * @since 2018/9/26
 */
public class JavaSwingUtil {
    private static final DefaultListCellRenderer TEXT_CENTER;

    static {
        TEXT_CENTER = new DefaultListCellRenderer();
        TEXT_CENTER.setHorizontalAlignment(DefaultListCellRenderer.CENTER);
    }

    public static void setComboBox(JComboBox comboBox, Object[] item) {
        setComboBox(comboBox, item, 0);
    }

    @SuppressWarnings("unchecked")
    public static void setComboBox(JComboBox comboBox, Object[] item, int index) {
        DefaultComboBoxModel model = new DefaultComboBoxModel(item);
        comboBox.setModel(model);
        comboBox.setSelectedIndex(index);
    }

    public static void setComboBoxTextCenter(JComboBox comboBox) {
        comboBox.setRenderer(TEXT_CENTER);
    }
}
