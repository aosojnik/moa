/*
 *    OutlierPanel.java
 *    Copyright (C) 2010 RWTH Aachen University, Germany
 *    @author Jansen (moa@cs.rwth-aachen.de)
 *
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *
 */

package moa.gui.visualization;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.RenderingHints;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import moa.cluster.SphereCluster;
import moa.learners.clusterers.outliers.MyBaseOutlierDetector;
import moa.learners.clusterers.outliers.MyBaseOutlierDetector.Outlier;

public class OutlierPanel extends JPanel {
	private boolean bAntiAlias = false;

	private MyBaseOutlierDetector myOutlierDetector;
	private Outlier myOutlier;
	private SphereCluster cluster;

	JDialog frameInfo = null;

	private double[] center;
	private final static int DRAW_SIZE = 4;
	protected double decay_rate;

	protected int x_dim = 0;
	protected int y_dim = 1;
	protected Color col;
	protected Color default_color = Color.BLACK;

	protected StreamOutlierPanel streamPanel;

	protected int panel_size;
	protected int window_size;
	protected boolean highligted = false;
	private double r;

	/** Creates new form ObjectPanel */

	public OutlierPanel(MyBaseOutlierDetector myOutlierDetector, Outlier outlier, SphereCluster cluster, Color color,
			StreamOutlierPanel sp) {
		this.myOutlierDetector = myOutlierDetector;
		this.myOutlier = outlier;
		this.cluster = cluster;
		center = cluster.getCenter();
		r = cluster.getRadius();
		streamPanel = sp;

		default_color = col = color;

		setVisible(true);
		setOpaque(false);
		setSize(new Dimension(1, 1));
		setLocation(0, 0);

		initComponents();
	}

	public void setDirection(double[] direction) {
	}

	public void updateLocation() {
		x_dim = streamPanel.getActiveXDim();
		y_dim = streamPanel.getActiveYDim();

		if ((cluster != null) && (center == null)) {
			getParent().remove(this);
		} else {
			// size of the parent
			window_size = Math.min(streamPanel.getWidth(), streamPanel.getHeight());

			panel_size = DRAW_SIZE + 1;

			setSize(new Dimension(panel_size, panel_size));

			int x = (int) Math.round(center[x_dim] * window_size);
			int y = (int) Math.round(center[y_dim] * window_size);
			setLocation(x - (panel_size / 2), y - (panel_size / 2));
		}
	}

	public void updateTooltip() {
		/*
		 * setToolTipText(cluster.getInfo());
		 * ToolTipManager.sharedInstance().registerComponent(this);
		 * ToolTipManager.sharedInstance().setInitialDelay(0);
		 */
	}

	@Override
	public boolean contains(int x, int y) {
		// only react on the hull of the cluster
		double dist = Math.sqrt(Math.pow(x - panel_size / 2, 2) + Math.pow(y - panel_size / 2, 2));
		if (panel_size / 2 - 5 < dist && dist < panel_size / 2 + 5)
			return true;
		else
			return false;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated
	// Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				formMouseClicked(evt);
			}
		});

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 296, Short.MAX_VALUE));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 266, Short.MAX_VALUE));
	}// </editor-fold>//GEN-END:initComponents

	private void formMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_formMouseClicked
		streamPanel.setHighlightedOutlierPanel(this);
		showInfo();
		streamPanel.setHighlightedOutlierPanel(null);
	}// GEN-LAST:event_formMouseClicked

	public String getInfo() {
		return myOutlierDetector.getObjectInfo(myOutlier.obj);
	}

	private void showInfo() {
		String title = "Outlier information";

		JLabel comp = new JLabel();
		comp.setText(getInfo());

		JOptionPane pane = new JOptionPane(comp);
		pane.setOptionType(JOptionPane.DEFAULT_OPTION);
		pane.setMessageType(JOptionPane.PLAIN_MESSAGE);

		PointerInfo pointerInfo = MouseInfo.getPointerInfo();
		Point mousePoint = pointerInfo.getLocation();

		JDialog dialog = pane.createDialog(this, title);
		dialog.setLocation(mousePoint);
		dialog.setVisible(true);
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (bAntiAlias) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}

		updateLocation();
		if (highligted) {
			g.setColor(Color.ORANGE);
		} else {
			g.setColor(default_color);
		}

		int drawSize = DRAW_SIZE;
		int drawStart = 0;

		g.fillOval(drawStart, drawStart, drawSize, drawSize);
		g.drawOval(drawStart, drawStart, drawSize, drawSize);
	}

	public void highlight(boolean enabled) {
		highligted = enabled;
		repaint();
	}

	public boolean isValidCluster() {
		return (center != null);
	}

	public int getClusterID() {
		return (int) cluster.getId();
	}

	public int getClusterLabel() {
		return (int) cluster.getGroundTruth();
	}

	public String getSVGString(int width) {
		StringBuffer out = new StringBuffer();

		int x = (int) (center[x_dim] * window_size);
		int y = (int) (center[y_dim] * window_size);
		int radius = panel_size / 2;

		out.append("<circle ");
		out.append("cx='" + x + "' cy='" + y + "' r='" + radius + "'");
		out.append(" stroke='green' stroke-width='1' fill='white' fill-opacity='0' />");
		out.append("\n");
		return out.toString();
	}

	public void drawOnCanvas(Graphics2D imageGraphics) {
		int x = (int) (center[x_dim] * window_size - (panel_size / 2));
		int y = (int) (center[y_dim] * window_size - (panel_size / 2));
		int radius = panel_size;
		imageGraphics.drawOval(x, y, radius, radius);
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	// End of variables declaration//GEN-END:variables
}
