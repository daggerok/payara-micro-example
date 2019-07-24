package com.github.daggerok.app.jpa;

import lombok.extern.java.Log;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static java.lang.String.format;

@Log
@ApplicationScoped
public class EntityManagerProducer {

  @Produces
  @Dependent
  @PersistenceContext
  EntityManager entityManager;

  public void close(@Disposes EntityManager entityManager) {
    log.info(format("bye: %s", entityManager));
    entityManager.close();
  }
}
