package com.github.daggerok.app;

import lombok.extern.log4j.Log4j2;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import java.util.logging.Logger;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Log4j2
@Provider
public class ErrorHandler implements ExceptionMapper<Throwable> {

  @Override
  public Response toResponse(final Throwable exception) {
    final String message = exception.getLocalizedMessage();
    log.error(() -> format("Unexpected error: %s", message));
    return Response.status(BAD_REQUEST).entity(singletonMap("error", message)).build();
  }
}
