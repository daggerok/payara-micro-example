package com.github.daggerok.app;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@ApplicationScoped
@Path("{path: .*?}")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class MyResource {

  @Inject MyRepository myRepository;

  @POST
  public Response createPerson(Map<String, String> request) {
    myRepository.addString(request.getOrDefault("string", ""));
    return Response.accepted().build();
  }

  @GET
  public Collection<String> getAllPeople() {
    return myRepository.getStrings();
  }
}
