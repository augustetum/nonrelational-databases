package service;

import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import dto.ValidationResultDto;
import entity.Review;
import entity.ReviewId;

@Service
public class ReviewValidationService {
    public ValidationResultDto validate(Review review, boolean idRequired) {
        if (review == null)
        {
            return ValidationResultDto.invalid("Review can't be null.");
        }

        List<Function<Review, ValidationResultDto>> validators = List.of(
            r -> validateId(r.getId()),
            r -> validateRating(r.getRating()),
            r -> validateDetails(r.getDetails()),
            r -> validateAuthorId(r.getAuthorId())
        );

        return validators.stream()
            .map(v -> v.apply(review))
            .filter(ValidationResultDto::isInvalid)
            .findFirst()
            .orElse(ValidationResultDto.valid());
    }

    private ValidationResultDto validateId(ReviewId id) {
        if (id == null)
        {
            return ValidationResultDto.invalid("Review id can't be null.");
        }

        if (id.revieweeId() == null)
        {
            return ValidationResultDto.invalid("Part of review id is missing: reviewee id can't be null.");
        }

        if (id.reviewId() == null)
        {
            return ValidationResultDto.invalid("Part of review id is missing: review id can't be null.");
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

    private ValidationResultDto validateAuthorId(String authorId) {
        if (authorId == null) {
            return ValidationResultDto.invalid("Author id can't be null.");
        }

        return ValidationResultDto.valid();
    }
}
