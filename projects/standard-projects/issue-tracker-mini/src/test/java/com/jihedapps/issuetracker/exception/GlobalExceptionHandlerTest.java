package com.jihedapps.issuetracker.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Mock
    private MethodArgumentNotValidException validationException;

    @Mock
    private BindingResult bindingResult;

    @Test
    void notFoundMapsTo404WithTheExceptionMessage() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleNotFound(new ResourceNotFoundException("Ticket introuvable : 42"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("status", 404);
        assertThat(response.getBody()).containsEntry("message", "Ticket introuvable : 42");
    }

    @Test
    void validationErrorMapsTo400WithFirstFieldError() {
        FieldError fieldError = new FieldError("ticket", "title", "ne doit pas etre vide");
        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(validationException);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "title : ne doit pas etre vide");
    }

    @Test
    void validationErrorFallsBackToGenericMessageWhenNoFieldErrors() {
        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(validationException);

        assertThat(response.getBody()).containsEntry("message", "Requete invalide");
    }
}
