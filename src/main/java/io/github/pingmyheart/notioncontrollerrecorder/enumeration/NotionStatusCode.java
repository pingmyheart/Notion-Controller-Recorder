package io.github.pingmyheart.notioncontrollerrecorder.enumeration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum NotionStatusCode {

    INVALID_JSON(HttpStatus.BAD_REQUEST,
            "invalid_json",
            "The request body could not be decoded as JSON."),
    INVALID_REQUEST_URL(HttpStatus.BAD_REQUEST,
            "invalid_request_url",
            "The request URL is not valid."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST,
            "invalid_request",
            "This request is not supported."),
    INVALID_GRANT(HttpStatus.BAD_REQUEST,
            "invalid_grant",
            "The provided authorization grant (e.g., authorization code, resource owner credentials) or refresh token is invalid, expired, revoked, does not match the redirection URI used in the authorization request, or was issued to another client. See OAuth 2.0 documentation for more information."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST,
            "validation_error",
            "The request body does not match the schema for the expected parameters. Check the \"message\" property for more details."),
    MISSING_VERSION(HttpStatus.BAD_REQUEST,
            "missing_version",
            "The request is missing the required Notion-Version header. See Versioning."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,
            "unauthorized",
            "The bearer token is not valid."),
    RESTRICTED_RESOURCE(HttpStatus.FORBIDDEN,
            "restricted_resource",
            "Given the bearer token used, the client doesn't have permission to perform this operation."),
    OBJECT_NOT_FOUND(HttpStatus.NOT_FOUND,
            "object_not_found",
            "Given the bearer token used, the resource does not exist. This error can also indicate that the resource has not been shared with owner of the bearer token."),
    CONFLICT_ERROR(HttpStatus.CONFLICT,
            "conflict_error",
            "The transaction could not be completed, potentially due to a data collision. Make sure the parameters are up to date and try again."),
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS,
            "rate_limited",
            "This request exceeds the number of requests allowed. Slow down and try again. More details on rate limits."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
            "internal_server_error",
            "An unexpected error occurred. Reach out to Notion support."),
    BAD_GATEWAY(HttpStatus.BAD_GATEWAY,
            "bad_gateway",
            "Notion encountered an issue while attempting to complete this request (e.g., failed to establish a connection with an upstream server). Please try again."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE,
            "service_unavailable",
            "Notion is unavailable. This can occur when the time to respond to a request takes longer than 60 seconds, the maximum request timeout. Please try again later."),
    DATABASE_CONNECTION_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE,
            "database_connection_unavailable",
            "Notion's database is unavailable or is not in a state that can be queried. Please try again later."),
    GATEWAY_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT,
            "gateway_timeout",
            "Notion timed out while attempting to complete this request. Please try again later.");

    private final HttpStatus httpStatusCode;
    private final String code;
    private final String description;

    public static NotionStatusCode getResponseCode(String errorCode) {
        Optional<NotionStatusCode> responseCode = Arrays.stream(NotionStatusCode.values())
                .filter(responseCodesEnum -> errorCode.equals(responseCodesEnum.getCode()))
                .findFirst();
        return responseCode.orElse(null);
    }
}
