package ctrlmnt;

import redis.clients.jedis.Jedis;

public class CtrlMNT implements Runnable {

	private ControllableService svc = null;
	private Jedis jedis = null;

	public CtrlMNT(ControllableService serviceRest) {
		this.svc = serviceRest;
		this.jedis = new Jedis();
	}

	public void run() {
		String hw = this.jedis.get(this.svc.getName() + "_" + "hw");
		if(hw != null) {
			this.svc.setHw(Float.valueOf(hw));
		} else {
			//System.out.println("null");
		}
	}
}
