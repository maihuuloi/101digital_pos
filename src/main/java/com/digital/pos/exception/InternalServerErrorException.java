package com.digital.pos.exception;

public class InternalServerErrorException extends BaseException {

  public InternalServerErrorException(String message) {
    super(message, "INTERNAL_SERVER_ERROR");
  }

  public InternalServerErrorException(String message, String errorCode) {
    super(message, errorCode);
  }


}
