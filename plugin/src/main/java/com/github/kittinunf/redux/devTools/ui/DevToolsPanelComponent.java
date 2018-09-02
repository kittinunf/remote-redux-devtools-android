package com.github.kittinunf.redux.devTools.ui;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

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
        timeLineActionButton.setIcon(new ImageIcon(getClass().getResource("/images/ic_play.png")));
        timeLineBackwardButton.setIcon(new ImageIcon(getClass().getResource("/images/ic_backward.png")));
        timeLineForwardButton.setIcon(new ImageIcon(getClass().getResource("/images/ic_forward.png")));
    }

}
