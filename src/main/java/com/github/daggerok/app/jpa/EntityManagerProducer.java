package com.github.daggerok.app.jpa;

import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Slf4j
@ApplicationScoped
public class EntityManagerProducer {

  @Produces
  @Dependent
  @PersistenceContext
  EntityManager entityManager;

  public void close(@Disposes EntityManager entityManager) {
    log.info("bye: {}", entityManager);
    entityManager.close();
  }
}
