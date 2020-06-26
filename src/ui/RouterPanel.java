package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

import javabean.Router;

public class RouterPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Router router;

	public JList<String> destiNetworkList;

	public JLabel timeLabel;

	public JTable routingTable;

	public RouterPanel(Router r) {
		router = r;
		router.setShowPanel(this);
		this.setLayout(new BorderLayout());
		Init();
	}

	private void Init() {
		this.setBorder(new TitledBorder(router.getRouterName()));
		JPanel dNetworkListPanel = new JPanel();
		dNetworkListPanel.setPreferredSize(new Dimension(120, 0));
		destiNetworkList = new JList<String>();
		dNetworkListPanel.setBorder(new TitledBorder("直连网络"));
		dNetworkListPanel.add(destiNetworkList);
		this.add(dNetworkListPanel, BorderLayout.WEST);
		JPanel routingListPanel = new JPanel();
		routingListPanel.setPreferredSize(new Dimension(0, 300));
		routingTable = new JTable();
		JScrollPane scroll = new JScrollPane(routingTable);
		routingListPanel.setBorder(new TitledBorder("实时路由表"));
		routingListPanel.setLayout(new BorderLayout());
		JPanel tablepane = new JPanel();
		tablepane.add(scroll);
		timeLabel = new JLabel();
		routingListPanel.add(timeLabel, BorderLayout.NORTH);
		routingListPanel.add(tablepane, BorderLayout.CENTER);
		this.add(routingListPanel, BorderLayout.CENTER);
	}

}
