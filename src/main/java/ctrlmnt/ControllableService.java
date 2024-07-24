package ctrlmnt;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.lang.management.ThreadMXBean;

import java.util.concurrent.*;
import org.apache.commons.math3.distribution.ExponentialDistribution;

public abstract class ControllableService {

    public abstract Float getHw();

    public abstract void setHw(Float hw);

    public abstract String getName();

    public abstract void ingress();

    public abstract void egress();

    public abstract Integer getUser();

    public abstract String getIscgroup();

    private ExponentialDistribution dist = null;
    private ThreadMXBean mgm = null;

    // stime is in milliseconds (ms)
    public void doWork(long stime) {
        if (this.dist == null) this.dist = new ExponentialDistribution(stime);
        if (this.mgm == null) this.mgm = ManagementFactory.getThreadMXBean();

        long delay = Long.valueOf(Math.round(this.dist.sample() * 1e06)); // ms to ns
        long start = this.mgm.getCurrentThreadCpuTime(); // nanoseconds (ns)
        while ((this.mgm.getCurrentThreadCpuTime() - start) < delay) {
        }
    }

    public void doWorkSleep(long stime) {

        if (this.getIscgroup() != "y") {
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

                    if (d <= 0) {
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
    }
}
