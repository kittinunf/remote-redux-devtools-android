package com.github.kittinunf.redux.devTools.ui;

import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

public class DevToolsPanelComponent {

    public JPanel devToolsPanel;

    public JLabel serverAddressLabel;
    public JLabel connectedClientLabel;

    public JTree monitorStateTree;

    public JButton timeLineActionButton;
    public JSlider timeLineTimeSlider;
    public JButton timeLineBackwardButton;
    public JButton timeLineForwardButton;

    public DevToolsPanelComponent() {
        IconFontSwing.register(FontAwesome.getIconFont());

        setUpMonitorUI();
        setUpTimeLineUI();
    }

    private void setUpMonitorUI() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
        DefaultTreeModel model = new DefaultTreeModel(root);
        monitorStateTree.setModel(model);
        monitorStateTree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Icon getOpenIcon() {
                return null;
            }

            @Override
            public Icon getClosedIcon() {
                return null;
            }

            @Override
            public Icon getLeafIcon() {
                return null;
            }
        });
    }

    private void setUpTimeLineUI() {
        timeLineActionButton.setIcon(IconFontSwing.buildIcon(FontAwesome.PLAY, 18, Color.WHITE));
        timeLineBackwardButton.setIcon(IconFontSwing.buildIcon(FontAwesome.ANGLE_LEFT, 30, Color.WHITE));
        timeLineForwardButton.setIcon(IconFontSwing.buildIcon(FontAwesome.ANGLE_RIGHT, 30, Color.WHITE));
    }

}
