package service;

import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import dto.ValidationResultDto;
import entity.Review;

@Service
public class ReviewValidationService {
    public ValidationResultDto validate(Review review, boolean idRequired) {
        if (review == null)
        {
            return ValidationResultDto.invalid("Review can't be null.");
        }

        List<Function<Review, ValidationResultDto>> validators = List.of(
            r -> validateId(r.getId(), idRequired),
            r -> validateRating(r.getRating()),
            r -> validateDetails(r.getDetails())
        );

        return validators.stream()
            .map(v -> v.apply(review))
            .filter(ValidationResultDto::isInvalid)
            .findFirst()
            .orElse(ValidationResultDto.valid());
    }

    private ValidationResultDto validateId(String id, boolean idRequired) {
        if (idRequired && id == null)
        {
            return ValidationResultDto.invalid("Review id can't be null.");
        }

        return ValidationResultDto.valid();
    }

    private ValidationResultDto validateRating(double rating) {
        if (rating < 0 || rating > 5) {
            return ValidationResultDto.invalid("Review rating must be between 0 and 5.");
        }

        if (rating % 0.5 != 0) {
            return ValidationResultDto.invalid("Review rating must be an increment of 0.5.");
        }

        return ValidationResultDto.valid();
    }

    private ValidationResultDto validateDetails(String details) {
        if (details == null || details.isBlank()) {
            return ValidationResultDto.invalid("Review can't be empty.");
        }

        return ValidationResultDto.valid();
    }
}
