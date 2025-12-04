package controller;

import dto.CreateWorkfieldRequest;
import dto.EditWorkfieldDto;
import dto.ValidationResultDto;
import entity.Workfield;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import service.CustomFreelancerDetails;
import service.EventLogService;
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

    @Autowired
    private EventLogService eventLogService;

    @GetMapping
    public ResponseEntity<List<Workfield>> getAllWorkfields() {
        List<Workfield> workfields = workfieldService.getAllWorkfields();
        return ResponseEntity.ok(workfields);
    }


    @GetMapping("/freelancer")
    public ResponseEntity<List<Workfield>> getWorkfieldsByCurrentFreelancer(Authentication authentication) {
        CustomFreelancerDetails userDetails = (CustomFreelancerDetails) authentication.getPrincipal();
        String freelancerId = userDetails.getUser().getId();

        List<Workfield> workfields = workfieldService.getWorkfieldsByFreelancerId(freelancerId);
        return ResponseEntity.ok(workfields);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<List<Workfield>> getWorkfieldsByCategory(@PathVariable String categoryId) {
        List<Workfield> workfields = workfieldService.getAllWorkfieldsByCategory(categoryId);
        return ResponseEntity.ok(workfields);
    }

    @GetMapping("/freelancer/{categoryId}")
    public ResponseEntity<List<Workfield>> getWorkfieldsByFreelancerIdByCategory(@PathVariable String categoryId, Authentication authentication) {
        // get user details
        CustomFreelancerDetails userDetails = (CustomFreelancerDetails) authentication.getPrincipal();
        String freelancerId = userDetails.getUser().getId();
        
        List<Workfield> workfields = workfieldService.getAllWorkfieldsByCategoryByFreelancerId(freelancerId, categoryId);
        return ResponseEntity.ok(workfields);
    }

    @PostMapping
    public ResponseEntity<?> addWorkfield(Authentication authentication, @RequestBody CreateWorkfieldRequest request) {
        // get user details
        CustomFreelancerDetails userDetails = (CustomFreelancerDetails) authentication.getPrincipal();
        String freelancerId = userDetails.getUser().getId();

        // create workfield entity
        Workfield workfield = new Workfield();
        workfield.setCategoryId(request.getCategoryId());
        workfield.setDescription(request.getDescription());
        workfield.setHourlyRate(request.getHourlyRate());

        //Sutvarkyta
         ValidationResultDto validationResult = validationService.validate(workfield);
         if (validationResult.isInvalid()) {
             eventLogService.logEvent("WORKFIELD", workfield.getId(), "WORKFIELD_CREATE", "FAILURE", freelancerId,
                     "WORKFIELD INVALID");
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationResult);
         }

        // add workfield
        workfieldService.addWorkfield(freelancerId, workfield);
        eventLogService.logEvent("WORKFIELD", workfield.getId(), "WORKFIELD_CREATE", "SUCCESS", freelancerId,
        "CATEGORY: " + workfield.getCategoryId() + ", DESCRIPTION: " + workfield.getDescription()
                + ", HOURLY_RATE: " + workfield.getHourlyRate());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

     @PutMapping("/{workfieldId}")
    public ResponseEntity<?> editWorkfield(@PathVariable String workfieldId, Authentication authentication, @RequestBody EditWorkfieldDto dto) {
        // get user details
        CustomFreelancerDetails userDetails = (CustomFreelancerDetails) authentication.getPrincipal();
        String freelancerId = userDetails.getUser().getId();

        // create workfield entity
        Workfield workfield = new Workfield();
        workfield.setId(workfieldId);
        workfield.setCategoryId(dto.getCategoryId());
        workfield.setDescription(dto.getDescription());
        workfield.setHourlyRate(dto.getHourlyRate());

         // Sutvarkyta
         ValidationResultDto validationResult = validationService.validate(workfield);
         if (validationResult.isInvalid()) {
             eventLogService.logEvent("WORKFIELD", workfield.getId(), "WORKFIELD_EDIT", "FAILURE", freelancerId,
                     "WORKFIELD INVALID");
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationResult);
         }

        // edit workfield
        workfieldService.editWorkfield(freelancerId, workfieldId, dto);
        eventLogService.logEvent("WORKFIELD", workfield.getId(), "WORKFIELD_EDIT", "SUCCESS", freelancerId,
                "CATEGORY: " + workfield.getCategoryId() + ", DESCRIPTION: " + workfield.getDescription()
                        + ", HOURLY_RATE: " + workfield.getHourlyRate());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{workfieldId}")
    public ResponseEntity<?> deleteWorkfield(@PathVariable String workfieldId, Authentication authentication) {
        // get user details
        CustomFreelancerDetails userDetails = (CustomFreelancerDetails) authentication.getPrincipal();
        String freelancerId = userDetails.getUser().getId();

        // delete workfield
        workfieldService.deleteWorkfield(freelancerId, workfieldId);
        eventLogService.logEvent("WORKFIELD", workfieldId, "WORKFIELD_DELETE", "SUCCESS", freelancerId,
                null);

        return ResponseEntity.ok().build();
    }
}
