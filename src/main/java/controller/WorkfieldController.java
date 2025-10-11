package controller;

import dto.EditWorkfieldDto;
import dto.ValidationResultDto;
import entity.Workfield;
import entity.WorkfieldCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import service.CustomFreelancerDetails;
import service.WorkfieldService;
import service.WorkfieldValidationService;

import java.util.List;

@RestController
@RequestMapping("/api/workfields")
public class WorkfieldController {

    @Autowired
    private WorkfieldService workfieldService;

    @Autowired
    private WorkfieldValidationService validationService;

    @GetMapping
    public ResponseEntity<List<Workfield>> getAllWorkfields(){
        List<Workfield> workfields = workfieldService.getAllWorkfields();
        return ResponseEntity.ok(workfields);
    }

    @GetMapping("/freelancer")
    public ResponseEntity<List<Workfield>> getWorkfieldsByCurrentFreelancer(Authentication authentication){
        CustomFreelancerDetails userDetails = (CustomFreelancerDetails) authentication.getPrincipal();
        String freelancerId = userDetails.getUser().getId();

        List<Workfield> workfields = workfieldService.getWorkfieldsByFreelancerId(freelancerId);
        return ResponseEntity.ok(workfields);
    }

    @GetMapping("/{category}")
    public ResponseEntity<List<Workfield>> getWorkfieldsByCategory(@PathVariable WorkfieldCategory category){
        List<Workfield> workfields = workfieldService.getAllWorkfieldsByCategory(category);
        return ResponseEntity.ok(workfields);
    }

    @GetMapping("/freelancer/{category}")
    public ResponseEntity<List<Workfield>> getWorkfieldsByFreelancerIdByCategory(@PathVariable WorkfieldCategory category,
    Authentication authentication){
        CustomFreelancerDetails userDetails = (CustomFreelancerDetails) authentication.getPrincipal();
        String freelancerId = userDetails.getUser().getId();
        List<Workfield> workfields = workfieldService.getAllWorkfieldsByCategoryByFreelancerId(freelancerId, category);
        return ResponseEntity.ok(workfields);
    }

    @PutMapping("/{workfieldId}")
    public ResponseEntity<?> editWorkfield(@PathVariable String workfieldId,
                                           Authentication authentication,
                                           @RequestBody EditWorkfieldDto dto){
        CustomFreelancerDetails userDetails = (CustomFreelancerDetails) authentication.getPrincipal();
        String freelancerId = userDetails.getUser().getId();

        Workfield workfield = new Workfield();
        workfield.setId(workfieldId);
        workfield.setCategory(dto.getCategory());
        workfield.setDescription(dto.getDescription());
        workfield.setHourlyRate(dto.getHourlyRate());

        ValidationResultDto validationResult = validationService.validate(workfield);
        if (validationResult.isInvalid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationResult);
        }

        workfieldService.editWorkfield(freelancerId, workfieldId, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<?> addWorkfield(Authentication authentication, @RequestBody Workfield workfield){
        CustomFreelancerDetails userDetails = (CustomFreelancerDetails) authentication.getPrincipal();
        String freelancerId = userDetails.getUser().getId();

        ValidationResultDto validationResult = validationService.validate(workfield);
        if (validationResult.isInvalid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationResult);
        }

        workfieldService.addWorkfield(freelancerId, workfield);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{workfieldId}")
    public ResponseEntity<?> deleteWorkfield(@PathVariable String workfieldId, Authentication authentication){
        CustomFreelancerDetails userDetails = (CustomFreelancerDetails) authentication.getPrincipal();
        String freelancerId = userDetails.getUser().getId();

        workfieldService.deleteWorkfield(freelancerId, workfieldId);
        return ResponseEntity.ok().build();
    }
}
