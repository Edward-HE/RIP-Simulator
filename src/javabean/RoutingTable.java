package javabean;

public class RoutingTable {

	// ������
	public static final int maxHopNum = 16;

	// Ŀ������(IP��ַ��ʶ)
	private String destiNetwork;

	// ��һ��·����(RouterName��ʶ)
	private String nextRouter;

	// ����
	private int hopNum;

	public RoutingTable() {

	}

	/**
	 * @param dNetwork Ŀ�����������
	 * @param nRouter  ��һ��·��������
	 * @param hNum     ����
	 */
	public RoutingTable(String dNetwork, String nRouter, int hNum) {
		setDestiNetwork(dNetwork);
		setNextRouter(nRouter);
		setHopNum(hNum);
	}

	/**
	 * ���ƹ��캯��
	 */
	public RoutingTable(RoutingTable copyItem) {
		setDestiNetwork(new String(copyItem.destiNetwork));
		setNextRouter(new String(copyItem.nextRouter));
		setHopNum(copyItem.hopNum);
	}

	public String getDestiNetwork() {
		return destiNetwork;
	}

	public void setDestiNetwork(String dNetwork) {
		destiNetwork = dNetwork;
	}

	public String getNextRouter() {
		return nextRouter;
	}

	public void setNextRouter(String nRouter) {
		nextRouter = nRouter;
	}

	public int getHopNum() {
		return hopNum;
	}

	public void setHopNum(int hNum) {
		hopNum = hNum;
	}

	/**
	 * ��������1
	 */
	public void increHopNum() {
		if (hopNum < maxHopNum) {
			hopNum++;
		}
	}

	/**
	 * �ж�·�ɱ��Ƿ�����ͬĿ������
	 */
	public boolean equalsDNet(Object obj) {
		if (obj instanceof RoutingTable) {
			return this.destiNetwork.equals(((RoutingTable) obj).destiNetwork);
		}
		return false;
	}

	/**
	 * �ж��Ƿ������ͬ��һ����·����
	 */
	public boolean equalsNRouter(Object obj) {
		if (obj instanceof RoutingTable) {
			return this.nextRouter.equals(((RoutingTable) obj).getNextRouter());
		}
		return false;
	}

	@Override
	public String toString() {
		return String.format("%15s%12s%5d", destiNetwork, nextRouter, hopNum);
	}

}
