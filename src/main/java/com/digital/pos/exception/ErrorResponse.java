package com.digital.pos.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ErrorResponse {

  private String code;          // e.g. "USER_NOT_FOUND"
  private String message;
}
