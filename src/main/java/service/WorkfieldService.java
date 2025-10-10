package service;

import entity.Workfield;
import entity.WorkfieldCategory;
import org.springframework.stereotype.Service;
import repository.WorkfieldRepository;

import java.util.List;

@Service
public class WorkfieldService {
    private final WorkfieldRepository workfieldRepository;


    public WorkfieldService(WorkfieldRepository workfieldRepository) {
        this.workfieldRepository = workfieldRepository;
    }

//    public List<Workfield> getAllWorkfields(){
//        retun
//
//    }

    public List<Workfield> getWorkfieldsByFreelancerId(String freelancerId){
        List<Workfield> workfields = workfieldRepository.getWorkfieldsByFreelancerId(freelancerId);
        return workfields;
    }

    public List<Workfield> getAllWorkfieldsByCategory(WorkfieldCategory category){
        List<Workfield> workfields = workfieldRepository.getAllWorkfieldsByCategory(category);
        return workfields;
    }

    public List<Workfield> getAllWorkfieldsByCategoryByFreelancerId(String freelancerId, WorkfieldCategory category){
        List<Workfield> workfields = workfieldRepository.getAllWorkfieldsByCategoryByFreelancerId(freelancerId, category);
        return workfields;
    }
}
