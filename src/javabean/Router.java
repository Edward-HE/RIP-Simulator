package javabean;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.DefaultListModel;
import javax.swing.table.DefaultTableModel;

import ui.RouterPanel;
import ui.MainFrame;

public class Router extends Thread {

	// ·����Map (Router.routerName,Router)
	public static Map<String, Router> RouterList;

	// ֱ������
	private static final String Direct = "Direct";

	// ����RIP����ʱ����
	private static final long SleepTime = (long) 1000;

	// ��ʱ������ʱʱ��
	private static final long SleepTime_2 = (long) 2000;

	// ·������
	private String routerName;

	// ֱ�������б� (netID��ʶ)
	private List<String> directNetworkList;

	// �Ƿ���ֱ����������������MAP (����ţ�״̬)
	private Map<String, Boolean> directNetworkMap;

	// ·�ɱ�
	private List<RoutingTable> routingList;

	// RIP���棨δ�����RIP���ģ�
	private Map<String, List<RoutingTable>> RIPCache;

	private Timer timer;

	private RouterPanel showPanel;

	private MainFrame Logpanel;

	// ·�������ϳ�ʼ��
	static {
		RouterList = new HashMap<String, Router>();
	}

	/**
	 * ���·����
	 * 
	 * @param router
	 */
	public static void addRouter(Router router) {
		RouterList.put(router.getRouterName(), router);
	}

	public Router() {

	}

	/**
	 * 
	 * 
	 * @param routeName ·������
	 */
	public Router(String routeName) {
		routerName = routeName;
		routeListInit();
		timerInit();
		addRouter(this);
	}

	private void routeListInit() {
		// ֱ�������б��ʼ��
		directNetworkList = new CopyOnWriteArrayList<String>();
		// ֱ����������״̬���ϳ�ʼ��
		directNetworkMap = new ConcurrentHashMap<String, Boolean>();
		// ·�ɱ��ʼ��
		routingList = new CopyOnWriteArrayList<RoutingTable>();
		// RIP���Ķ��г�ʼ��
		RIPCache = new ConcurrentHashMap<String, List<RoutingTable>>();
	}

	private void timerInit() {
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				Router.this.checkDestiNetwork();
			}
		}, 0, SleepTime_2);
	}

	public String getRouterName() {
		return routerName;
	}

	public synchronized void setOnetDirectNetwork(String Ip) {
		directNetworkList.add(Ip);
		directNetworkMap.put(Ip, true);
		routingList.add(new RoutingTable(Ip, Direct, 1));
		Network n = Network.NetworkList.get(Ip);
		n.setOneDirectRouter(routerName);
	}

	public void setShowPanel(RouterPanel rp) {
		showPanel = rp;
	}

	public synchronized void checkDestiNetwork() {
		for (String net : directNetworkList) {
			if (!directNetworkMap.get(net)) {
				System.out.print("���ɴ�");
			}
		}
	}

	public synchronized void putRIPCache(Map<String, List<RoutingTable>> nRIPCache) {
		for (Map.Entry<String, List<RoutingTable>> entry : nRIPCache.entrySet()) {
			String sentRouterName = entry.getKey();
			List<RoutingTable> sentRoutingList = entry.getValue();
			List<RoutingTable> copyRoutingList = new CopyOnWriteArrayList<RoutingTable>();
			for (RoutingTable sitem : sentRoutingList) {
				copyRoutingList.add(new RoutingTable(sitem));
			}
			RIPCache.put(sentRouterName, copyRoutingList);
		}
	}

	private void sentSelfRIP() {
		for (String dNetworkID : directNetworkList) {
			if (directNetworkMap.get(dNetworkID)) {
				Network dNetwork = Network.NetworkList.get(dNetworkID);

				List<RoutingTable> copyRoutingList = new CopyOnWriteArrayList<RoutingTable>();
				for (RoutingTable sitem : routingList) {
					copyRoutingList.add(new RoutingTable(sitem));
				}
				dNetwork.putRIPCache(routerName, copyRoutingList);
			} else {
			}
		}
	}

	private synchronized void BFRA() {

		// ����δ����RIP���ļ���
		for (Map.Entry<String, List<RoutingTable>> entry : RIPCache.entrySet()) {
			// Key--·������
			String sentRouterName = entry.getKey();
			// Value--��Ӧ·�ɱ�
			List<RoutingTable> sentRoutingList = entry.getValue();

			for (RoutingTable sitem : sentRoutingList) {
				// ·�ɱ���
				// ����һ��·����Ϊ����·����
				sitem.setNextRouter(sentRouterName);
				// ������1
				sitem.increHopNum();

				// �洢Ŀ��������ͬ��routingListItem'Index
				int index = -1;

				for (int i = 0; i < routingList.size(); i++) {
					RoutingTable titem = routingList.get(i);
					// �ж�routingList�Ƿ���������Ŀ��������ͬ���������indexΪi������ѭ��
					if (titem.equalsDNet(sitem)) {
						index = i;
						break;
					}
				}

				// routingList���������Ŀ��������ͬ����
				if (index != -1) {
					RoutingTable titem = routingList.get(index);
					// �ж��Ƿ�����ͬ��һ��·����
					if (titem.equalsNRouter(sitem)) {
						// ����ͬ��һ��·����
						// �滻ԭ��Item
						routingList.remove(index);
						routingList.add(sitem);
					} else {
						// �Ƚ�����
						if (sitem.getHopNum() < titem.getHopNum()) {
							// ����С��ԭ����
							// �滻ԭ��Item
							routingList.remove(index);
							routingList.add(sitem);
						}
					}
				}
				// ������--��Ӹ���Ŀ
				else {
					routingList.add(sitem);
				}
			}
		}
		// ���RIPCache
		RIPCache.clear();
	}

	private void showInfo() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		showPanel.timeLabel.setText(df.format(new Date()));
		DefaultListModel<String> lmodel = new DefaultListModel<String>();
		for (String nname : directNetworkList) {
			lmodel.addElement(nname);
		}
		showPanel.destiNetworkList.setModel(lmodel);
		String[] title = { "Ŀ������", "��һ��·����", "����" };
		DefaultTableModel model = new DefaultTableModel(title, 0);
		for (RoutingTable i : routingList) {
			if (i != null) {
				Object[] info = new Object[3];
				info[0] = i.getDestiNetwork();
				info[1] = i.getNextRouter();
				info[2] = i.getHopNum();
				model.addRow(info);
			}
		}
		showPanel.routingTable.setModel(model);
	}

	@Override
	public void run() {
		while (true) {

			showInfo();
			// ����RIP����������

			sentSelfRIP();
			try {
				Thread.sleep(SleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// ���������㷨
			BFRA();

		}
	}

}
