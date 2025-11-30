package service;

import dto.EditWorkfieldDto;
import dto.ValidationResultDto;
import entity.Workfield;
import entity.WorkfieldCategory;
import org.springframework.stereotype.Service;
import repository.WorkfieldRepository;

import java.util.List;

@Service
public class WorkfieldService {
    private final WorkfieldRepository workfieldRepository;
    private final WorkfieldValidationService validationService;

    public WorkfieldService(WorkfieldRepository workfieldRepository, WorkfieldValidationService validationService) {
        this.workfieldRepository = workfieldRepository;
        this.validationService = validationService;
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
        ValidationResultDto validationResult = validationService.validate(workfield);
        if (validationResult.isInvalid()) {
            throw new IllegalArgumentException(validationResult.getMessage());
        }
        workfieldRepository.addWorkfield(freelancerId, workfield);
    }

    public void editWorkfield(String freelancerId, String workfieldId, EditWorkfieldDto dto){
        workfieldRepository.editWorkfield(freelancerId, workfieldId, dto);
    }

    public void deleteWorkfield(String freelancerId, String workfieldId){
        workfieldRepository.deleteWorkfield(freelancerId, workfieldId);
    }
}
