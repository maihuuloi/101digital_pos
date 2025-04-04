package com.digital.pos.exception;

public class ResourceNotFoundException extends BaseException {

  public ResourceNotFoundException(String message) {
    super(message, "RESOURCE_NOT_FOUND");
  }
}
