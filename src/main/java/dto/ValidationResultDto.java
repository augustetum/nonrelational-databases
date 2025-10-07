package dto;

public class ValidationResultDto {
    private boolean isValid;
    private String message;

    private ValidationResultDto() {
        this.isValid = true;
    }

    private ValidationResultDto(String message) {
        this.isValid = false;
        this.message = message;
    }

    public static ValidationResultDto valid() {
        return new ValidationResultDto();
    }

    public static ValidationResultDto invalid(String message) {
        return new ValidationResultDto(message);
    }

    public boolean isInvalid() {
        return !isValid;
    }

    public String getMessage() {
        return message;
    }
}
