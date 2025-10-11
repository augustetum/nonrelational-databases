package service;

import dto.EditWorkfieldDto;
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

    public List<Workfield> getAllWorkfields(){
        return workfieldRepository.getAllWorkfields();
    }

    public List<Workfield> getWorkfieldsByFreelancerId(String freelancerId){
        return workfieldRepository.getWorkfieldsByFreelancerId(freelancerId);
    }

    public List<Workfield> getAllWorkfieldsByCategory(WorkfieldCategory category){
        return workfieldRepository.getAllWorkfieldsByCategory(category);
    }

    public List<Workfield> getAllWorkfieldsByCategoryByFreelancerId(String freelancerId, WorkfieldCategory category){
        return workfieldRepository.getAllWorkfieldsByCategoryByFreelancerId(freelancerId, category);
    }

    public void addWorkfield(String freelancerId, Workfield workfield){
        workfieldRepository.addWorkfield(freelancerId, workfield);
    }

    public void editWorkfield(String freelancerId, String workfieldId, EditWorkfieldDto dto){
        workfieldRepository.editWorkfield(freelancerId, workfieldId, dto);
    }

    public void deleteWorkfield(String freelancerId, String workfieldId){
        workfieldRepository.deleteWorkfield(freelancerId, workfieldId);
    }
}
