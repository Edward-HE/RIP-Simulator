package javabean;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Network extends Thread {

	public static Map<String, Network> NetworkList;

	private static final long SleepTime = (long) 1000;

	private String netID;

	private List<String> destiRouterList;

	private Map<String, Boolean> destiRouterMap;

	private Map<String, List<RoutingTable>> RIPCache;

	static {
		NetworkList = new HashMap<String, Network>();
	}

	/**
	 * 
	 * @param net
	 */
	public static void addNetwork(Network net) {
		NetworkList.put(net.netID.toString(), net);
	}

	public Network() {

	}

	/**
	 * 
	 * @param nID 网络号
	 */
	public Network(String nID) {
		netID = nID;
		infoListInit();
		addNetwork(this);
	}

	/**
	 * 信息列表初始化
	 */
	private void infoListInit() {
		// 直连路由器队列初始化
		destiRouterList = new CopyOnWriteArrayList<String>();
		// 直连路由器状态集合初始化
		destiRouterMap = new ConcurrentHashMap<String, Boolean>();
		// RIP报文缓存队列初始化
		RIPCache = new ConcurrentHashMap<String, List<RoutingTable>>();
	}

	/**
	 * 
	 * @param routerName 路由器名
	 */
	public synchronized void setOneDirectRouter(String routerName) {
		destiRouterList.add(routerName);
		destiRouterMap.put(routerName, true);
	}

	/**
	 * 
	 * @param sentRouter 发送路由器名
	 * @param rList      路由表
	 */
	public synchronized void putRIPCache(String sentRouter, List<RoutingTable> rList) {
		// 若Cache存在该路由器发来的RIP报文，移除原报文
		if (RIPCache.containsKey(sentRouter)) {
			RIPCache.remove(sentRouter);
		}
		// 报文添加
		RIPCache.put(sentRouter, rList);
	}

	/**
	 * 
	 * @param dRouterName 路由器名
	 * @return Map< String,List< RoutingListItem > > 专属RIP报文集合
	 */
	private synchronized Map<String, List<RoutingTable>> getDedicRIPCache(String dRouterName) {
		// 临时变量result
		Map<String, List<RoutingTable>> result = new ConcurrentHashMap<String, List<RoutingTable>>();
		result.putAll(RIPCache);
		// 移除由路由器dRouterName发送的报文
		result.remove(dRouterName);
		return result;
	}

	/**
	 */
	private void sentSelfRIP() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (String dRouterName : destiRouterList) {
			if (destiRouterMap.get(dRouterName)) {
				Router router = Router.RouterList.get(dRouterName);
				router.putRIPCache(this.getDedicRIPCache(router.getRouterName()));
			} else {
			}
		}
	}

	@Override
	public void run() {
		while (true) {
			// 发送RIP报文缓存至路由器
			sentSelfRIP();
			try {
				Thread.sleep(SleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
