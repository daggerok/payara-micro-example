package com.github.daggerok.app.liquibase;

import liquibase.integration.cdi.CDILiquibaseConfig;
import liquibase.integration.cdi.annotations.LiquibaseType;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.sql.DataSource;

@Log
@ApplicationScoped
public class LiquibaseConfig {

  @Resource
  DataSource myDataSource;

  @Produces
  @LiquibaseType
  public CDILiquibaseConfig cdiLiquibaseConfig() {
    CDILiquibaseConfig config = new CDILiquibaseConfig();
    config.setChangeLog("liquibase/changelog.xml");
    return config;
  }

  @Produces
  @SneakyThrows
  @LiquibaseType
  public DataSource dataSource() {
    return myDataSource;
  }

  @Produces
  @LiquibaseType
  public ResourceAccessor resourceAccessor() {
    return new ClassLoaderResourceAccessor(getClass().getClassLoader());
  }
}
