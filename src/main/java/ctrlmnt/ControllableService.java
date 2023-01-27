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
		//TODO devo aggiungere la logica per eseguire il compito sulla cpu e non solo attraverso gli sleep
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

			while (true) {
				long st = System.nanoTime();
				TimeUnit.MILLISECONDS.sleep(Math.min(d.longValue(), 20l));
				long wt = (System.nanoTime() - st);
				d -= wt / 1.0e6;
				
				if(d<=0) {
					break;
				}

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

//	public void doWork(long stime) {
//		this.ingress();
//		try {
//			ExponentialDistribution dist = new ExponentialDistribution(stime);
//			Double isTime = dist.sample();
//
//			double usersKm1 = this.getUser().doubleValue();
//			double hwkm1 = this.getHw().doubleValue();
//			
//			//Double isTime=Long.valueOf(stime).doubleValue();
//			Double d = null;
//			if (usersKm1 > hwkm1) {
//				d = (double) (isTime.doubleValue() * (usersKm1 / hwkm1));
//			} else {
//				d = isTime.doubleValue();
//			}
//
//			TimeUnit.MILLISECONDS.sleep(d.longValue());
//
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		} finally {
//			this.egress();
//		}
//	}
}
