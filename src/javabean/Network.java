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
	 * @param nID �����
	 */
	public Network(String nID) {
		netID = nID;
		infoListInit();
		addNetwork(this);
	}

	/**
	 * ��Ϣ�б��ʼ��
	 */
	private void infoListInit() {
		// ֱ��·�������г�ʼ��
		destiRouterList = new CopyOnWriteArrayList<String>();
		// ֱ��·����״̬���ϳ�ʼ��
		destiRouterMap = new ConcurrentHashMap<String, Boolean>();
		// RIP���Ļ�����г�ʼ��
		RIPCache = new ConcurrentHashMap<String, List<RoutingTable>>();
	}

	/**
	 * 
	 * @param routerName ·������
	 */
	public synchronized void setOneDirectRouter(String routerName) {
		destiRouterList.add(routerName);
		destiRouterMap.put(routerName, true);
	}

	/**
	 * 
	 * @param sentRouter ����·������
	 * @param rList      ·�ɱ�
	 */
	public synchronized void putRIPCache(String sentRouter, List<RoutingTable> rList) {
		// ��Cache���ڸ�·����������RIP���ģ��Ƴ�ԭ����
		if (RIPCache.containsKey(sentRouter)) {
			RIPCache.remove(sentRouter);
		}
		// �������
		RIPCache.put(sentRouter, rList);
	}

	/**
	 * 
	 * @param dRouterName ·������
	 * @return Map< String,List< RoutingListItem > > ר��RIP���ļ���
	 */
	private synchronized Map<String, List<RoutingTable>> getDedicRIPCache(String dRouterName) {
		// ��ʱ����result
		Map<String, List<RoutingTable>> result = new ConcurrentHashMap<String, List<RoutingTable>>();
		result.putAll(RIPCache);
		// �Ƴ���·����dRouterName���͵ı���
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
			// ����RIP���Ļ�����·����
			sentSelfRIP();
			try {
				Thread.sleep(SleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
