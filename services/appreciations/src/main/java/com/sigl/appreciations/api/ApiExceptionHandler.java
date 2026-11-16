package com.sigl.appreciations.api;

import com.sigl.appreciations.domain.ForbiddenActionException;
import com.sigl.appreciations.domain.IllegalTransitionException;
import com.sigl.appreciations.domain.InvalidAppraisalException;
import com.sigl.appreciations.domain.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiExceptionHandler {

  @ExceptionHandler(InvalidAppraisalException.class)
  ProblemDetail invalid(InvalidAppraisalException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ProblemDetail invalidBody(MethodArgumentNotValidException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request body");
  }

  @ExceptionHandler(ForbiddenActionException.class)
  ProblemDetail forbidden(ForbiddenActionException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
  }

  @ExceptionHandler(NotFoundException.class)
  ProblemDetail notFound(NotFoundException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler(IllegalTransitionException.class)
  ProblemDetail conflict(IllegalTransitionException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
  }
}
