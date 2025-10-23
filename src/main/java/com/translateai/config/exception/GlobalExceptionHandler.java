package com.translateai.config.exception;

import com.translateai.common.ApiResponse;
import com.translateai.common.ApiStatus;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle custom RestApiException
     * @param restApiException the custom exception to handle
     * @return ApiResponse<Object> the response containing the API status and error details
     */
    @ExceptionHandler(RestApiException.class)
    public ApiResponse<Object> handleCustomException(RestApiException restApiException) {
        ApiStatus apiStatus = restApiException.getApiStatus();
        System.err.println(restApiException.getMessage());
        logger.error("RestApiException: ", restApiException);
        return new ApiResponse<>(apiStatus, null);
    }

    /**
     * Handle all other exceptions
     * @param ex the exception to handle
     * @return ApiResponse<Object> the response containing the API status and error details
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Object> handleAllExceptions(Exception ex) {
        ApiStatus apiStatus = ApiStatus.INTERNAL_SERVER_ERROR;
        logger.error("INTERNAL_SERVER_ERROR: ", ex);
        return new ApiResponse<>(apiStatus, null);
    }

    /**
     * Handle EntityNotFoundException
     * @param ex the exception to handle
     * @return ApiResponse<Object> the response containing the API status and error details
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ApiResponse<Object> handleEntityNotFoundException(EntityNotFoundException ex) {
        ApiStatus apiStatus = ApiStatus.BAD_REQUEST_VALID;
        logger.error("Error: ", ex);
        return new ApiResponse<>(apiStatus, null);
    }

    /**
     * Handle DataIntegrityViolationException
     * @param ex the exception to handle
     * @return ApiResponse<Object> the response containing the API status and error details
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiResponse<Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        ApiStatus apiStatus = ApiStatus.BAD_REQUEST_VALID;
        logger.error("Error: ", ex);
        return new ApiResponse<>(apiStatus, null);
    }

    /**
     * Handle ConstraintViolationException
     * @param ex the exception to handle
     * @return ApiResponse<Object> the response containing the API status and error details
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Object> handleConstraintViolationException(ConstraintViolationException ex) {
        ApiStatus apiStatus = ApiStatus.BAD_REQUEST_VALID;
        logger.error("Error: ", ex);
        return new ApiResponse<>(apiStatus, null);
    }

    /**
     * Handle OptimisticLockException
     * @param ex the exception to handle
     * @return ApiResponse<Object> the response containing the API status and error details
     */
    @ExceptionHandler(OptimisticLockException.class)
    public ApiResponse<Object> handleOptimisticLockException(OptimisticLockException ex) {
        ApiStatus apiStatus = ApiStatus.BAD_REQUEST_VALID;
        logger.error("Error: ", ex);
        return new ApiResponse<>(apiStatus, null);
    }

    /**
     * Handle TransactionSystemException
     * @param ex the exception to handle
     * @return ApiResponse<Object> the response containing the API status and error details
     */
    @ExceptionHandler(TransactionSystemException.class)
    public ApiResponse<Object> handleTransactionSystemException(TransactionSystemException ex) {
        ApiStatus apiStatus = ApiStatus.BAD_REQUEST_VALID;
        logger.error("Error: ", ex);
        return new ApiResponse<>(apiStatus, null);
    }

}