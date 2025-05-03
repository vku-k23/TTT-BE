package com.ttt.cinevibe.exception;

import com.ttt.cinevibe.dto.response.error.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.SQLGrammarException;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Handle validation errors for @Valid annotations
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message("Validation error")
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .debugMessage("Invalid request parameters")
                .build();
        
        // Add field-specific validation errors
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            apiError.addValidationError(error.getField(), error.getDefaultMessage());
        }
        
        // Add global errors (cross-field validations)
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            apiError.addValidationError(error.getObjectName(), error.getDefaultMessage());
        }
        
        return buildResponseEntity(apiError);
    }
    
    // Handle invalid JSON in request body
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, 
            HttpHeaders headers, 
            HttpStatusCode status, 
            WebRequest request) {
        
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message("Malformed JSON request")
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .debugMessage(ex.getMessage())
                .build();
        
        return buildResponseEntity(apiError);
    }
    
    // Handle custom ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    protected ResponseEntity<Object> handleResourceNotFound(
            ResourceNotFoundException ex, 
            WebRequest request) {
        
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.NOT_FOUND)
                .statusCode(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();
        
        return buildResponseEntity(apiError);
    }
    
    // Handle custom DuplicateResourceException
    @ExceptionHandler(DuplicateResourceException.class)
    protected ResponseEntity<Object> handleDuplicateResource(
            DuplicateResourceException ex, 
            WebRequest request) {
        
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .statusCode(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();
        
        return buildResponseEntity(apiError);
    }
    
    // Handle custom UnauthorizedException
    @ExceptionHandler(UnauthorizedException.class)
    protected ResponseEntity<Object> handleUnauthorized(
            UnauthorizedException ex, 
            WebRequest request) {
        
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.FORBIDDEN)
                .statusCode(HttpStatus.FORBIDDEN.value())
                .message(ex.getMessage())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();
        
        return buildResponseEntity(apiError);
    }
    
    // Handle ConstraintViolationException for @Validated annotated params
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {
        
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message("Validation error")
                .path(request.getRequestURI())
                .build();
        
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String fieldName = ((PathImpl) violation.getPropertyPath()).getLeafNode().getName();
            apiError.addValidationError(fieldName, violation.getMessage());
        }
        
        return buildResponseEntity(apiError);
    }
    
    // Handle missing request parameters
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(String.format("Missing required parameter: %s", ex.getParameterName()))
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();
        
        return buildResponseEntity(apiError);
    }
    
    // Handle type mismatch exceptions (wrong parameter type)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(String.format("Parameter '%s' with value '%s' could not be converted to type '%s'", 
                        ex.getName(), ex.getValue(), Objects.requireNonNull(ex.getRequiredType()).getSimpleName()))
                .path(request.getRequestURI())
                .build();
        
        return buildResponseEntity(apiError);
    }
    
    // Handle 404 not found
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.NOT_FOUND)
                .statusCode(HttpStatus.NOT_FOUND.value())
                .message(String.format("No handler found for %s %s", ex.getHttpMethod(), ex.getRequestURL()))
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();
        
        return buildResponseEntity(apiError);
    }
    
    // Handle SQL Grammar Exception specifically (like "Unknown column" errors)
    @ExceptionHandler(SQLGrammarException.class)
    protected ResponseEntity<Object> handleSQLGrammarException(
            SQLGrammarException ex,
            WebRequest request) {
        
        log.error("SQL Grammar error", ex);
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        
        // Create a user-friendly message based on the SQL error
        String message = "Database schema error occurred";
        String debugMessage = ex.getMessage();
        
        // Try to extract the column name if it's an "Unknown column" error
        if (ex.getMessage() != null && ex.getMessage().contains("Unknown column")) {
            Pattern pattern = Pattern.compile("Unknown column '([^']+)'");
            Matcher matcher = pattern.matcher(ex.getMessage());
            if (matcher.find()) {
                String columnName = matcher.group(1);
                message = "Unknown database column: " + columnName;
                debugMessage = "There might be a mismatch between your entity model and database schema. " +
                        "Please check if you need to create a migration for the column: " + columnName;
            }
        }
        
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(message)
                .path(path)
                .debugMessage(debugMessage)
                .build();
        
        return buildResponseEntity(apiError);
    }
    
    // Handle database specific exceptions
    @ExceptionHandler({SQLException.class, DataAccessException.class})
    protected ResponseEntity<Object> handleDatabaseError(Exception ex, WebRequest request) {
        log.error("Database error occurred", ex);
        
        String message = "A database error occurred";
        
        // More specific messages based on exception type
        if (ex instanceof DataIntegrityViolationException) {
            message = "Data integrity violation";
        } else if (ex instanceof InvalidDataAccessApiUsageException) {
            message = "Invalid data access operation";
        }
        
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(message)
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .debugMessage(ex.getMessage())
                .build();
        
        return buildResponseEntity(apiError);
    }
    
    // Handle custom DatabaseException
    @ExceptionHandler(DatabaseException.class)
    protected ResponseEntity<Object> handleDatabaseException(
            DatabaseException ex,
            WebRequest request) {
        
        log.error("Database exception", ex);
        
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(ex.getMessage())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();
        
        return buildResponseEntity(apiError);
    }
    
    // Handle Spring Security exceptions
    @ExceptionHandler({AccessDeniedException.class})
    protected ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.FORBIDDEN)
                .statusCode(HttpStatus.FORBIDDEN.value())
                .message("Access denied: You don't have permission to access this resource")
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();
        
        return buildResponseEntity(apiError);
    }
    
    @ExceptionHandler({AuthenticationException.class})
    protected ResponseEntity<Object> handleAuthentication(AuthenticationException ex, WebRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        String message = "Authentication failed";
        
        if (ex instanceof BadCredentialsException) {
            message = "Invalid credentials";
        }
        
        ApiError apiError = ApiError.builder()
                .status(status)
                .statusCode(status.value())
                .message(message)
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();
        
        return buildResponseEntity(apiError);
    }
    
    // Fallback handler for any other exceptions
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        // Log the full exception for debugging but return a generic message to clients
        log.error("Unhandled exception", ex);
        
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        
        // Check if the exception has a custom status annotation
        if (ex.getClass().isAnnotationPresent(ResponseStatus.class)) {
            status = ex.getClass().getAnnotation(ResponseStatus.class).value();
        }
        
        ApiError apiError = ApiError.builder()
                .status(status)
                .statusCode(status.value())
                .message("An unexpected error occurred")
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .debugMessage(ex.getMessage())
                .build();
        
        return buildResponseEntity(apiError);
    }
    
    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}