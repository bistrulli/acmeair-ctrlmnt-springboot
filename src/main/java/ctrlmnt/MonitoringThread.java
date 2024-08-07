package ctrlmnt;

import com.google.api.Metric;
import com.google.api.MonitoredResource;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.monitoring.v3.*;
import com.google.protobuf.util.Timestamps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class MonitoringThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringThread.class.getName());

    public MonitoringThread() {

    }

    @Override
    public void run() {
        // Every 30 seconds send metrics
        super.run();
        double step = 30d; // Seconds
        logger.info("requestCount : " + ControllableService.requestCount.get());
        logger.info("requestCountM1 : " + ControllableService.requestCountM1.get());

        logger.info("serviceTimeSum : " + ControllableService.serviceTimesSum.get());
        logger.info("serviceTimeSumM1 : " + ControllableService.serviceTimesSumM1.get());
        try {
            double rps = (double) (ControllableService.requestCount.get() - ControllableService.requestCountM1.get()) / step;
            double avg_st = (double) (ControllableService.serviceTimesSum.get() - ControllableService.serviceTimesSumM1.get()) / (ControllableService.requestCount.get() - ControllableService.requestCountM1.get());

            ControllableService.requestCountM1.set(ControllableService.requestCount.get());
            ControllableService.serviceTimesSumM1.set(ControllableService.serviceTimesSum.get());

            logger.info("rps : {}", rps);
            logger.info("avg_st : {}", avg_st);
            writeCustomMetric("rps_gauge", rps);
            writeCustomMetric("st_gauge", avg_st);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void writeCustomMetric(String metricName, double metricValue) throws IOException {
        // Instantiates a client
        try (MetricServiceClient metricServiceClient = MetricServiceClient.create()) {

            // Prepares an individual data point
            long nowMillis = System.currentTimeMillis();
            TimeInterval interval = TimeInterval.newBuilder()
                    .setEndTime(Timestamps.fromMillis(nowMillis))
                    .setStartTime(Timestamps.fromMillis(nowMillis)) // Set startTime for clarity, even if the same as endTime
                    .build();
            TypedValue value = TypedValue.newBuilder().setDoubleValue(metricValue).build();
            Point point = Point.newBuilder().setInterval(interval).setValue(value).build();

            List<Point> pointList = new ArrayList<>();
            pointList.add(point);


            ProjectName name = ProjectName.of("my-microservice-test-project");

            // Fetch pod name from environment variable
            String podName = System.getenv("POD_NAME"); // Assumes POD_NAME is set via Kubernetes Downward API

            logger.info("POD_NAME = {}", podName);

            // Prepares the metric descriptor
            Map<String, String> metricLabels = new HashMap<>();
//            String serviceName = "tier" + Project.getTierNumber();
            metricLabels.put("service", podName); // serviceName);
//            metricLabels.put("pod_name", podName);
            Metric metric = Metric.newBuilder()
                    .setType("custom.googleapis.com/" + metricName)
                    .putAllLabels(metricLabels)
                    .build();

            // Prepares the monitored resource descriptor
            Map<String, String> resourceLabels = new HashMap<>();
            resourceLabels.put("project_id", "my-microservice-test-project");
            MonitoredResource resource = MonitoredResource.newBuilder()
                    .setType("global")
                    .putAllLabels(resourceLabels)
                    .build();

            // Prepares the time series request
            TimeSeries timeSeries = TimeSeries.newBuilder()
                    .setMetric(metric)
                    .setResource(resource)
                    .addAllPoints(pointList)
                    .build();

            CreateTimeSeriesRequest request = CreateTimeSeriesRequest.newBuilder()
                    .setName(name.toString())
                    .addAllTimeSeries(Collections.singletonList(timeSeries))
                    .build();

            // Writes time series data
            metricServiceClient.createTimeSeries(request);

            logger.info("Done writing time series data.");
        }
    }
}
