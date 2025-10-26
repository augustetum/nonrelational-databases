package dto;

import lombok.Data;

@Data
public class FreelancerJobsCompletedLeaderboardDto {
    private String id;
    private String firstName;
    private String lastName;
    private int jobsCompleted;
}
