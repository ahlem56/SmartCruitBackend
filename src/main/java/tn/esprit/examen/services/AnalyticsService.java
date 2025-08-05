package tn.esprit.examen.services;

import com.google.analytics.data.v1beta.*;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.stereotype.Service;
import tn.esprit.examen.entities.JobViews;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnalyticsService {

    private static final String PROPERTY_ID = "properties/480287545";

    public List<JobViews> fetchJobOfferViews() throws IOException {
        BetaAnalyticsDataClient analyticsData = BetaAnalyticsDataClient.create(
                BetaAnalyticsDataSettings.newBuilder()
                        .setCredentialsProvider(
                                FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(
                                        new FileInputStream("src/main/resources/smartcruit-463807-ff77862ff767.json"))))
                        .build()
        );

        RunReportRequest request = RunReportRequest.newBuilder()
                .setProperty(PROPERTY_ID)
                .addDimensions(Dimension.newBuilder().setName("pagePath"))
                .addMetrics(Metric.newBuilder().setName("screenPageViews"))
                .addDateRanges(DateRange.newBuilder().setStartDate("30daysAgo").setEndDate("today").build())
                .build();

        RunReportResponse response = analyticsData.runReport(request);

        List<JobViews> jobViews = new ArrayList<>();
        for (Row row : response.getRowsList()) {
            String path = row.getDimensionValues(0).getValue(); // "/job-details/5"
            String views = row.getMetricValues(0).getValue();
            if (path.startsWith("/job-details/")) {
                String jobId = path.split("/")[2];
                jobViews.add(new JobViews(Long.parseLong(jobId), Integer.parseInt(views)));
            }
        }

        return jobViews;
    }

    public List<JobViews> fetchRealtimeViews() throws IOException {
        BetaAnalyticsDataClient analyticsData = BetaAnalyticsDataClient.create(
                BetaAnalyticsDataSettings.newBuilder()
                        .setCredentialsProvider(
                                FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(
                                        new FileInputStream("src/main/resources/smartcruit-463807-ff77862ff767.json"))))
                        .build()
        );

        RunRealtimeReportRequest realtimeRequest = RunRealtimeReportRequest.newBuilder()
                .setProperty(PROPERTY_ID)
                .addDimensions(Dimension.newBuilder().setName("unifiedScreenName")) // ✅ USE THIS
                .addMetrics(Metric.newBuilder().setName("screenPageViews"))
                .build();

        RunRealtimeReportResponse response = analyticsData.runRealtimeReport(realtimeRequest);

        List<JobViews> realtimeViews = new ArrayList<>();
        for (Row row : response.getRowsList()) {
            String screenName = row.getDimensionValues(0).getValue(); // Should be like "job-details/9"
            String views = row.getMetricValues(0).getValue();

            if (screenName.contains("job-details/")) {
                try {
                    String jobId = screenName.split("job-details/")[1].split("[/?&#]")[0];
                    realtimeViews.add(new JobViews(Long.parseLong(jobId), Integer.parseInt(views)));
                } catch (Exception e) {
                    System.err.println("❌ Could not parse jobId from screenName: " + screenName);
                }
            }
        }

        System.out.println("⚡ Called fetchRealtimeViews()");
        for (Row row : response.getRowsList()) {
            System.out.println("📦 Realtime Row → " + row);
        }

        return realtimeViews;
    }





}
