package com.betvictor.processing.exception;

import com.betvictor.processing.data.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidParagraphCountException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidParagraphCount(InvalidParagraphCountException exception, HttpServletRequest request) {
        return errorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParagraphCount(HttpServletRequest request) {
        return errorResponse(HttpStatus.BAD_REQUEST, "Required request parameter 'p' is missing", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidParagraphCountType(HttpServletRequest request) {
        return errorResponse(HttpStatus.BAD_REQUEST, "p must be a valid integer", request);
    }

    @ExceptionHandler(HipsumClientException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidHipsumResponse(HipsumClientException exception, HttpServletRequest request) {
        return errorResponse(HttpStatus.BAD_GATEWAY, exception.getMessage(), request);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ApiErrorResponse> handleHipsumRequestFailure(HttpServletRequest request) {
        return errorResponse(HttpStatus.BAD_GATEWAY, "Failed to retrieve paragraphs from Hipsum", request);
    }

    @ExceptionHandler({org.springframework.kafka.KafkaException.class, org.apache.kafka.common.KafkaException.class})
    public ResponseEntity<ApiErrorResponse> handleKafkaFailure(HttpServletRequest request) {
        return errorResponse(HttpStatus.SERVICE_UNAVAILABLE, "Failed to publish the processing result", request);
    }

    private ResponseEntity<ApiErrorResponse> errorResponse(HttpStatus status, String message, HttpServletRequest request) {
        ApiErrorResponse body = new ApiErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
