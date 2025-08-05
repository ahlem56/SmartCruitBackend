package tn.esprit.examen.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobViews {
    private Long jobId;
    private int views;
    private String title;

    public JobViews(Long jobId, int views) {
        this.jobId = jobId;
        this.views = views;
    }

    public Long getJobId() {
        return jobId;
    }

    public int getViews() {
        return views;
    }

    public String getTitle() {
        return title;
    }


}
