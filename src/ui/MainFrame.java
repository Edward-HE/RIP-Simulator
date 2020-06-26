package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javabean.Network;
import javabean.Router;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JComboBox;

public class MainFrame extends JFrame implements ActionListener {

	/**
	 * 好像是为了反序列化，IDE生成的
	 */
	private static final long serialVersionUID = 1L;

	private JPanel contentPane;

	private JPanel error;

	private JComboBox<String> routeslist;

	private JPanel routerInfoPanel;

	private JPanel route;

	private List<Router> routerList;
	private List<RouterPanel> routerPanelList;

	private List<Network> networkList;

	public JTextArea LogShow;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		setTitle("RIP协议模拟器");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1280, 802);
		setLocationRelativeTo(null);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		// 文件菜单栏
		JMenu mnNewMenu = new JMenu("\u6587\u4EF6");
		menuBar.add(mnNewMenu);
		// 打开网络拓扑文件
		JMenuItem mntmOpen = new JMenuItem("\u6253\u5F00\u7F51\u7EDC\u62D3\u6251");
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!routerList.isEmpty() && !networkList.isEmpty()) {
					JOptionPane.showMessageDialog(MainFrame.this, "请先关闭该窗口，重新打开时可以选择新的文件！", "提示",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				try {
					loadNetToplogy();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				showRouterAndNetInfo();
				for (Router r : routerList) {
					r.start();
				}
				for (Network n : networkList) {
					n.start();
				}
			}
		});
		mnNewMenu.add(mntmOpen);

		JMenuItem mntmSaveTable = new JMenuItem("\u4FDD\u5B58\u8DEF\u7531\u8868");
		mnNewMenu.add(mntmSaveTable);

		JMenuItem mntmSaveLog = new JMenuItem("\u4FDD\u5B58\u8F6F\u4EF6\u65E5\u5FD7");
		mnNewMenu.add(mntmSaveLog);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		panelInit();
		listInit();
	}

	private void panelInit() {
		contentPane.setLayout(null);
		JPanel routerInfoPane = new JPanel();
		routerInfoPane.setLayout(new BorderLayout());
		routerInfoPane.setBorder(new TitledBorder("路由器"));
		route = new JPanel();
		route.setBounds(572, 13, 676, 686);
		route.setBorder(new TitledBorder("路由信息"));
		route.setLayout(new BorderLayout());
		route.add(routerInfoPane, BorderLayout.NORTH);
		contentPane.add(route);

		// 路由器信息显示块
		routerInfoPanel = new JPanel();
		routerInfoPanel.setLayout(new BoxLayout(routerInfoPanel, BoxLayout.Y_AXIS));
		JScrollPane scrollPane = new JScrollPane(routerInfoPanel);
		route.add(scrollPane, BorderLayout.CENTER);

		error = new JPanel();
		error.setBounds(5, 13, 553, 155);
		error.setBorder(new TitledBorder("故障模拟"));
		error.setPreferredSize(new Dimension(200, 0));
		contentPane.add(error);
		error.setLayout(null);

		JLabel errorLable = new JLabel("\u6545\u969C\u8DEF\u7531\u5668\u540D\u79F0");
		errorLable.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 17));
		errorLable.setBounds(12, 21, 142, 50);
		error.add(errorLable);

		JButton errorButton = new JButton("\u53D1\u751F\u6545\u969C");
		errorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

			}
		});
		errorButton.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 17));
		errorButton.setBounds(391, 30, 118, 36);
		error.add(errorButton);

		routeslist = new JComboBox<String>();
		routeslist.setBounds(146, 30, 231, 36);
		for (Map.Entry<String, Router> entry : Router.RouterList.entrySet()) {
			routeslist.addItem(entry.getKey());
		}
		// routeslist.setSelectedIndex(0);
		error.add(routeslist);

		JPanel Logpanel = new JPanel();
		Logpanel.setBounds(5, 285, 553, 406);
		contentPane.add(Logpanel);
		Logpanel.setLayout(null);

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(14, 38, 525, 355);
		Logpanel.add(scrollPane_1);

		LogShow = new JTextArea();
		scrollPane_1.setViewportView(LogShow);
		LogShow.setLineWrap(true);

		JLabel lblNewLabel = new JLabel("\u8F6F\u4EF6\u5DE5\u4F5C\u65E5\u5FD7");
		lblNewLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 17));
		lblNewLabel.setBounds(14, 13, 176, 18);
		Logpanel.add(lblNewLabel);
	}

	private void listInit() {
		routerList = new ArrayList<Router>();
		routerPanelList = new ArrayList<RouterPanel>();
		networkList = new ArrayList<Network>();
	}

	/**
	 * 加载网络拓扑
	 * 
	 * @throws IOException
	 */
	public void loadNetToplogy() throws IOException {
		// 文件选择框
		JFileChooser jf = new JFileChooser();
		String filename = new String();
		if (jf.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
			filename = jf.getSelectedFile().getAbsolutePath();
		}
		FileReader fr = new FileReader(filename);
		BufferedReader br = new BufferedReader(fr);
		List<String> allLines = new ArrayList<String>();
		String Line;
		while ((Line = br.readLine()) != null) {
			allLines.add(Line);
		}
		br.close();
		fr.close();

		int index_1 = 0, index_2 = 0;
		// 路由器
		index_1 = allLines.indexOf("===");
		List<String> RouterName = allLines.subList(0, index_1);
		for (String rn : RouterName) {
			routerList.add(new Router(rn));
		}
		// 网络
		index_2 = allLines.lastIndexOf("===");
		List<String> NetworkIp = allLines.subList(index_1 + 1, index_2);
		for (String rn : NetworkIp) {
			networkList.add(new Network(rn));
		}
		// 路由-网络
		List<String> topoInfo = allLines.subList(index_2 + 1, allLines.size());
		for (int i = 0; i < topoInfo.size(); i++) {
			String[] InfoStrs = topoInfo.get(i).split("/");
			for (String ipStr : InfoStrs) {
				routerList.get(i).setOnetDirectNetwork(ipStr);
			}
		}
	}

	private void showRouterAndNetInfo() {
		for (Router r : routerList) {
			RouterPanel rp = new RouterPanel(r);
			rp.setVisible(true);
			routerPanelList.add(rp);
			routerInfoPanel.add(rp);
		}

		contentPane.updateUI();
		route.updateUI();
		routerInfoPanel.updateUI();
	}

	public void actionPerformed(ActionEvent e) {

	}
}
