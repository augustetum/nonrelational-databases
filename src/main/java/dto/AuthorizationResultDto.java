package dto;

import enumerator.AuthorizationStatus;

public class AuthorizationResultDto {
    private AuthorizationStatus status = AuthorizationStatus.SUCCESS;
    private String message;

    public void setMessage(String message) {
        this.status = AuthorizationStatus.FAILURE;
        this.message = message;
    }

    public AuthorizationStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
