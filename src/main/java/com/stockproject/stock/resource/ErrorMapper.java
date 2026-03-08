package com.stockproject.stock.resource;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

/**
 * Global exception handler that converts Java exceptions into proper HTTP responses.
 * Without this, all errors would return a generic 500 Internal Server Error.
 *
 * Examples:
 * - NotFoundException → 404 with JSON message
 * - BadRequestException → 400 with JSON message
 * - Any other exception → 500 with generic message
 */
@Provider
public class ErrorMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        int status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        String message = "An unexpected error occurred";

        if (exception instanceof NotFoundException) {
            status = Response.Status.NOT_FOUND.getStatusCode();
            message = exception.getMessage();
        } else if (exception instanceof BadRequestException) {
            status = Response.Status.BAD_REQUEST.getStatusCode();
            message = exception.getMessage();
        }

        return Response.status(status)
                .entity(Map.of(
                    "error", true,
                    "status", status,
                    "message", message
                ))
                .build();
    }
}
