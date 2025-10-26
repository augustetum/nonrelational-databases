package controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import dto.FreelancerDetailsDto;
import dto.FreelancerRatingLeaderboardDto;
import service.FreelancerService;

@RestController
@RequestMapping("/api/freelancers")
public class FreelancerController {
    @Autowired
    private FreelancerService freelancerService;

    @GetMapping
    public ResponseEntity<?> getFreelancerDetails(String userId) {
        Optional<FreelancerDetailsDto> maybeFreelancer = freelancerService.getFreelancerDetails(userId);
        return ResponseEntity.ok(maybeFreelancer);
    }

    @GetMapping("/dates")
    public ResponseEntity<?> getAvailableDates(String freelancerId) {
        List<LocalDate> availableDates = freelancerService.getAvailableDates(freelancerId);
        if (availableDates.isEmpty())
            return ResponseEntity.ok("This freelancer has no available dates");
        else
            return ResponseEntity.ok(availableDates);
    }
    
    @GetMapping("/leaderboard")
    public ResponseEntity<List<?>> getLeaderboard(
        @RequestParam(defaultValue = "averageRating") String sortBy,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(defaultValue = "0") int skip
    ) {
        if (sortBy.contains("jobsCompleted")) {

        }

        // default to average rating leaderboard
        List<FreelancerRatingLeaderboardDto> leaderboadDetails = freelancerService.getRatingLeaderboard(limit, skip);
        return ResponseEntity.ok(leaderboadDetails);
    }

}
