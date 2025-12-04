package service;

import dto.ValidationResultDto;
import entity.Workfield;
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
                w -> validateCategoryId(w.getCategoryId()),
                w -> validateDescription(w.getDescription()),
                w -> validateHourlyRate(w.getHourlyRate())
        );

        return validators.stream()
                .map(v -> v.apply(workfield))
                .filter(ValidationResultDto::isInvalid)
                .findFirst()
                .orElse(ValidationResultDto.valid());
    }

    private ValidationResultDto validateCategoryId(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return ValidationResultDto.invalid("Workfield category ID can't be empty.");
        }
        if (!categoryService.categoryExists(categoryId)) {
            return ValidationResultDto.invalid("Category with ID '" + categoryId + "' does not exist.");
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
