package controller;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import dto.FreelancerDetailsDto;
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

    @GetMapping("/leaderboard")
    public ResponseEntity<List<FreelancerDetailsDto>> getLeaderboard(
        @RequestParam(defaultValue = "averageRating") String sortBy,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(defaultValue = "0") int skip
    ) {
        List<FreelancerDetailsDto> leaderboard = freelancerService.getLeaderboard(sortBy, limit, skip);
        return ResponseEntity.ok(leaderboard);
    }

}
