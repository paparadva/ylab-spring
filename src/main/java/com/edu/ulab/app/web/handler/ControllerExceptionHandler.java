package com.edu.ulab.app.web.handler;

import com.edu.ulab.app.exception.EntityDoesNotExistException;
import com.edu.ulab.app.exception.NotFoundException;
import com.edu.ulab.app.web.response.BaseWebResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@Slf4j
@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler({NotFoundException.class, EntityDoesNotExistException.class})
    public ResponseEntity<BaseWebResponse> handleNotFoundExceptionException(@NonNull final Exception exc) {
        log.error(exc.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new BaseWebResponse(createErrorMessage(exc)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseWebResponse> handleGeneralException(@NonNull final Exception exc) {
        log.error(exc.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new BaseWebResponse(createErrorMessage(exc)));
    }

    private String createErrorMessage(Exception exception) {
        final String message = exception.getMessage();
        log.error(ExceptionHandlerUtils.buildErrorMessage(exception));
        return message;
    }
}
