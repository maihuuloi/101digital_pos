package com.digital.pos.exception;

import com.digital.pos.domain.exception.AllQueueFullException;
import com.digital.pos.domain.exception.InvalidOrderStateException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Log4j2
public class RestControllerAdvice {

  @ExceptionHandler(InvalidOrderStateException.class)
  public ResponseEntity<ErrorResponse> handleInvalidOrderState(InvalidOrderStateException ex) {
    log.error("Invalid order state: {}", ex.getMessage(), ex);
    return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_ORDER_STATE", ex.getMessage());
  }

  @ExceptionHandler(AllQueueFullException.class)
  public ResponseEntity<ErrorResponse> handleAllQueueFull(AllQueueFullException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorResponse("ALL_QUEUES_FULL", ex.getMessage()));
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<?> handleNotFound(EntityNotFoundException ex) {
    log.error("User not found: {}", ex.getMessage(), ex);
    return buildResponse(HttpStatus.NOT_FOUND, "DATA_NOT_FOUND", ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleGeneral(Exception ex) {
    log.error("An error occurred: {}", ex.getMessage(), ex);
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "An error occurred");
  }

  @ExceptionHandler(InvalidRefreshTokenException.class)
  public ResponseEntity<ErrorResponse> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
    log.error("Invalid refresh token: {}", ex.getMessage(), ex);
    return buildResponse(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", ex.getMessage());

  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex) {
    return buildResponse(
        HttpStatus.BAD_REQUEST,
        "MALFORMED_JSON",
        "Request body is invalid or unreadable"
    );
  }

  private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String code, String message) {
    return ResponseEntity.status(status).body(new ErrorResponse(code, message));
  }
}
