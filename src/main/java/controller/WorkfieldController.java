package controller;

import entity.Workfield;
import entity.WorkfieldCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.WorkfieldService;

import java.util.List;

@RestController
@RequestMapping("/api/workfields")
public class WorkfieldController {

    @Autowired
    private WorkfieldService workfieldService;

    @GetMapping
    public ResponseEntity<List<Workfield>> getAllWorkfields(){
        List<Workfield> workfields = workfieldService.getAllWorkfields();
        return ResponseEntity.ok(workfields);
    }

    @GetMapping("/freelancer/{freelancerId}")
    public ResponseEntity<List<Workfield>> getWorkfieldsByFreelancerId(@PathVariable String freelancerId){
        List<Workfield> workfields = workfieldService.getWorkfieldsByFreelancerId(freelancerId);
        return ResponseEntity.ok(workfields);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Workfield>> getWorkfieldsByCategory(@PathVariable WorkfieldCategory category){
        List<Workfield> workfields = workfieldService.getAllWorkfieldsByCategory(category);
        return ResponseEntity.ok(workfields);
    }

    @GetMapping("/freelancer/{freelancerId}/category/{category}")
    public ResponseEntity<List<Workfield>> getWorkfieldsByFreelancerIdByCategory(@PathVariable WorkfieldCategory category,
    @PathVariable String freelancerId){
        List<Workfield> workfields = workfieldService.getAllWorkfieldsByCategoryByFreelancerId(freelancerId, category);
        return ResponseEntity.ok(workfields);
    }






}
