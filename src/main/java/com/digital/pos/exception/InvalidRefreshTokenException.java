package com.digital.pos.exception;

public class InvalidRefreshTokenException extends BaseException {

  public InvalidRefreshTokenException(String message, String errorCode) {
    super(message, errorCode);
  }
}
