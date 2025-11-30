package service;

import dto.ValidationResultDto;
import entity.Workfield;
import entity.WorkfieldCategory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

@Service
public class WorkfieldValidationService {
    private final WorkfieldCategoryService categoryService;

    public WorkfieldValidationService(WorkfieldCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public ValidationResultDto validate(Workfield workfield) {
        if (workfield == null) {
            return ValidationResultDto.invalid("Workfield can't be null.");
        }

        List<Function<Workfield, ValidationResultDto>> validators = List.of(
                w -> validateCategory(w.getCategory()),
                w -> validateDescription(w.getDescription()),
                w -> validateHourlyRate(w.getHourlyRate())
        );

        return validators.stream()
                .map(v -> v.apply(workfield))
                .filter(ValidationResultDto::isInvalid)
                .findFirst()
                .orElse(ValidationResultDto.valid());
    }

    private ValidationResultDto validateCategory(WorkfieldCategory category) {
        if (category == null) {
            return ValidationResultDto.invalid("Workfield category can't be null.");
        }
        if (category.getId() == null || category.getId().isBlank()) {
            return ValidationResultDto.invalid("Workfield category ID can't be empty.");
        }
        if (!categoryService.categoryExists(category.getId())) {
            return ValidationResultDto.invalid("Category with ID '" + category.getId() + "' does not exist.");
        }
        return ValidationResultDto.valid();
    }

    private ValidationResultDto validateDescription(String description) {
        if (description == null || description.isBlank()) {
            return ValidationResultDto.invalid("Workfield description can't be empty.");
        }
        return ValidationResultDto.valid();
    }

    private ValidationResultDto validateHourlyRate(int hourlyRate) {
        if (hourlyRate < 0) {
            return ValidationResultDto.invalid("Hourly rate cannot be negative.");
        }
        return ValidationResultDto.valid();
    }
}
