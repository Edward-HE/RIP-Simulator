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

	// 路由器Map (Router.routerName,Router)
	public static Map<String, Router> RouterList;

	// 直连网络
	private static final String Direct = "Direct";

	// 发送RIP报文时间间隔
	private static final long SleepTime = (long) 1000;

	// 定时任务延时时间
	private static final long SleepTime_2 = (long) 2000;

	// 路由器名
	private String routerName;

	// 直连网络列表 (netID标识)
	private List<String> directNetworkList;

	// 是否与直连网络正常相连的MAP (网络号，状态)
	private Map<String, Boolean> directNetworkMap;

	// 路由表
	private List<RoutingTable> routingList;

	// RIP缓存（未处理的RIP报文）
	private Map<String, List<RoutingTable>> RIPCache;

	private Timer timer;

	private RouterPanel showPanel;

	private MainFrame Logpanel;

	// 路由器集合初始化
	static {
		RouterList = new HashMap<String, Router>();
	}

	/**
	 * 添加路由器
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
	 * @param routeName 路由器名
	 */
	public Router(String routeName) {
		routerName = routeName;
		routeListInit();
		timerInit();
		addRouter(this);
	}

	private void routeListInit() {
		// 直连网络列表初始化
		directNetworkList = new CopyOnWriteArrayList<String>();
		// 直连网络连接状态集合初始化
		directNetworkMap = new ConcurrentHashMap<String, Boolean>();
		// 路由表初始化
		routingList = new CopyOnWriteArrayList<RoutingTable>();
		// RIP报文队列初始化
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
				System.out.print("不可达");
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

		// 遍历未处理RIP报文集合
		for (Map.Entry<String, List<RoutingTable>> entry : RIPCache.entrySet()) {
			// Key--路由器名
			String sentRouterName = entry.getKey();
			// Value--对应路由表
			List<RoutingTable> sentRoutingList = entry.getValue();

			for (RoutingTable sitem : sentRoutingList) {
				// 路由表处理
				// 置下一跳路由器为发送路由其
				sitem.setNextRouter(sentRouterName);
				// 跳数加1
				sitem.increHopNum();

				// 存储目的网络相同的routingListItem'Index
				int index = -1;

				for (int i = 0; i < routingList.size(); i++) {
					RoutingTable titem = routingList.get(i);
					// 判断routingList是否存在与该项目的网络相同的项，有则置index为i并跳出循环
					if (titem.equalsDNet(sitem)) {
						index = i;
						break;
					}
				}

				// routingList存在与该项目的网络相同的项
				if (index != -1) {
					RoutingTable titem = routingList.get(index);
					// 判断是否有相同下一跳路由器
					if (titem.equalsNRouter(sitem)) {
						// 有相同下一跳路由器
						// 替换原有Item
						routingList.remove(index);
						routingList.add(sitem);
					} else {
						// 比较跳数
						if (sitem.getHopNum() < titem.getHopNum()) {
							// 跳数小于原有项
							// 替换原有Item
							routingList.remove(index);
							routingList.add(sitem);
						}
					}
				}
				// 不存在--添加该项目
				else {
					routingList.add(sitem);
				}
			}
		}
		// 清空RIPCache
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
		String[] title = { "目的网络", "下一跳路由器", "跳数" };
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
			// 发送RIP报文至网络

			sentSelfRIP();
			try {
				Thread.sleep(SleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// 距离向量算法
			BFRA();

		}
	}

}
