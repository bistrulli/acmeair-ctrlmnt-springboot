package ctrlmnt;

import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.distribution.ExponentialDistribution;

public abstract class ControllableService {

	public abstract Float getHw();
	public abstract void setHw(Float hw);
	public abstract String getName();
	public abstract void ingress();
	public abstract void egress();
	public abstract Integer getUser();

	public void doWork(long stime) {
		this.ingress();
		try {
			ExponentialDistribution dist = new ExponentialDistribution(stime);
			Double isTime = dist.sample();

			Double hwkm1 = null;
			Double usersKm1 = null;
			Double d = null;

			usersKm1 = this.getUser().doubleValue();
			hwkm1 = this.getHw().doubleValue();
			if (usersKm1 >= hwkm1) {
				d = (double) (isTime.doubleValue() * (usersKm1 / hwkm1));
			} else {
				d = isTime.doubleValue();
			}

			while (d > 0) {
				long st = System.nanoTime();
				TimeUnit.MILLISECONDS.sleep(Math.min(d.longValue(), 10l));
				long wt = (System.nanoTime() - st);
				d -= wt / 1.0e6;

				if (usersKm1 >= hwkm1) {
					isTime = d * (hwkm1 / usersKm1);
				} else {
					isTime = d;
				}

				usersKm1 = this.getUser().doubleValue();
				hwkm1 = this.getHw().doubleValue();
				if (usersKm1 >= hwkm1) {
					d = (double) (isTime.doubleValue() * (usersKm1 / hwkm1));
				} else {
					d = isTime.doubleValue();
				}

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			this.egress();
		}

	}
}
