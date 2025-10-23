package com.translateai.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ApiStatus {

    // ===== 2xx: Success =====
    SUCCESS(HttpStatus.OK, "200001", "Request processed successfully."),
    CREATED(HttpStatus.CREATED, "201001", "Resource created successfully."),
    ACCEPTED(HttpStatus.ACCEPTED, "202001", "Request accepted and being processed."),

    // ===== 4xx: Client Errors =====
    // 400 Bad Request
    BAD_REQUEST_VALID(HttpStatus.BAD_REQUEST, "400001", "Invalid request data."),
    BAD_REQUEST_BUSINESS(HttpStatus.BAD_REQUEST, "400002", "Business rule validation failed."),
    BAD_REQUEST_FILE_EMPTY(HttpStatus.BAD_REQUEST, "400003", "Uploaded file is empty."),
    BAD_REQUEST_FILE_EXTENSIONS(HttpStatus.BAD_REQUEST, "400004", "Unsupported file extension."),
    BAD_REQUEST_FILE_NOT_READ(HttpStatus.BAD_REQUEST, "400005", "Unable to read uploaded file."),
    BAD_REQUEST_FILE_NOT_EXIST(HttpStatus.BAD_REQUEST, "400006", "File does not exist."),
    BAD_REQUEST_DATA_CONFIGURATION_USED(HttpStatus.BAD_REQUEST, "400007", "Data configuration is already in use."),
    BAD_REQUEST_USERNAME_EXISTS(HttpStatus.BAD_REQUEST, "400008", "Username already exists."),
    BAD_REQUEST_EMAIL_EXISTS(HttpStatus.BAD_REQUEST, "400009", "Email address already exists."),
    EMAIL_EXIST(HttpStatus.BAD_REQUEST, "400010", "This email address is already registered in the system."),
    USERNAME_EXIST(HttpStatus.BAD_REQUEST, "400011", "This nickname already exists. Please choose another one."),
    INVALID_USER_AUTH_CODE(HttpStatus.BAD_REQUEST, "400012", "Invalid verification code."),
    AUTH_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "400013", "Verification code has expired."),
    GROUP_USER_EXISTS(HttpStatus.BAD_REQUEST, "400014", "User is already a member of this group."),
    INVALID_OLD_PASSWORD(HttpStatus.BAD_REQUEST, "400015", "Old password is incorrect."),
    TRANSLATION_TEXT_EMPTY(HttpStatus.BAD_REQUEST, "400016", "Text cannot be empty."),
    TRANSLATION_SPEAKER_REQUIRED(HttpStatus.BAD_REQUEST, "400017", "Speaker must be specified."),
    TRANSLATION_LISTENER_REQUIRED(HttpStatus.BAD_REQUEST, "400018", "Listener must be specified."),
    TRANSLATION_INVALID_SPEAKER(HttpStatus.BAD_REQUEST, "400019", "Invalid speaker."),
    TRANSLATION_INVALID_LISTENER(HttpStatus.BAD_REQUEST, "400020", "Invalid listener."),
    TRANSLATION_INVALID_STYLE(HttpStatus.BAD_REQUEST, "400021", "Invalid style."),
    TRANSLATION_INVALID_LANGUAGE(HttpStatus.BAD_REQUEST, "400022", "Invalid target language code."),
    TRANSLATION_IP_DETECTION_FAILED(HttpStatus.BAD_REQUEST, "400023", "Unable to determine client IP address."),
    TRANSLATION_DAILY_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "429001", "Daily translation limit exceeded."),
    TRANSLATION_QUOTA_EXHAUSTED(HttpStatus.PAYMENT_REQUIRED, "402001", "Quota of your plan is exhausted. Please upgrade your plan."),
    TRANSLATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "500002", "Translation failed."),
    
    // Google Play Purchase
    GOOGLE_PLAY_PURCHASE_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "400024", "The provided purchase token is invalid or expired."),
    GOOGLE_PLAY_PURCHASE_NOT_FOUND(HttpStatus.NOT_FOUND, "404002", "Purchase token does not match the specified product ID."),
    GOOGLE_PLAY_INVALID_PURCHASE_TYPE(HttpStatus.BAD_REQUEST, "400025", "Invalid purchase type. Must be 'IN_APP' or 'SUBSCRIPTION'."),
    GOOGLE_PLAY_API_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "500003", "Google Play API is not configured properly."),
    GOOGLE_PLAY_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "500004", "An error occurred while communicating with Google Play API."),

    // 401 Unauthorized
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "401001", "User not found or unauthorized."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "401002", "Invalid username or password."),
    NO_ACCESS_PERMISSION(HttpStatus.UNAUTHORIZED, "401003", "User does not have access permission."),
    USER_NOT_ACTIVE(HttpStatus.UNAUTHORIZED, "401004", "This user account is inactive."),
    TOKEN_VERIFICATION_FAILED(HttpStatus.UNAUTHORIZED, "401005", "OAuth2 token verification failed."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "401006", "Unauthorized request."),

    // 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN, "403001", "Access is denied."),

    // 404 Not Found
    NOT_FOUND(HttpStatus.NOT_FOUND, "404001", "Requested resource not found."),

    // 409 Conflict
    CONFLICT(HttpStatus.CONFLICT, "409001", "Resource already exists or conflicts with current state."),

    // 413 Payload Too Large
    PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "413001", "Request payload size is too large."),

    // 422 Unprocessable Entity
    UNPROCESSABLE_ENTITY(HttpStatus.UNPROCESSABLE_ENTITY, "422001", "Unable to process the provided data."),

    // ===== 5xx: Server Errors =====
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "500001", "An unexpected server error occurred.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ApiStatus(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}