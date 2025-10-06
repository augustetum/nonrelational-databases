package dto;

import enumerator.PermissionStatus;

public class PermissionCheckResultDto {
    private PermissionStatus status = PermissionStatus.ALLOWED;
    private String message;

    public void setMessage(String message) {
        this.status = PermissionStatus.DENIED;
        this.message = message;
    }

    public PermissionStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
