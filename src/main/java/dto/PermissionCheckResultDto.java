package dto;

public class PermissionCheckResultDto {
    private boolean isAllowed;
    private String message;

    private PermissionCheckResultDto() {
        this.isAllowed = true;
    }

    private PermissionCheckResultDto(String message) {
        this.isAllowed = false;
        this.message = message;
    }

    public static PermissionCheckResultDto valid() {
        return new PermissionCheckResultDto();
    }

    public static PermissionCheckResultDto invalid(String message) {
        return new PermissionCheckResultDto(message);
    }

    public boolean isDenied() {
        return !isAllowed;
    }

    public String getMessage() {
        return message;
    }
}