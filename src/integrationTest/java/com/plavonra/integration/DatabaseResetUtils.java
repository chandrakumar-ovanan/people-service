package com.plavonra.integration;

import javax.sql.DataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

public final class DatabaseResetUtils {

  private static final String SCHEMA_SCRIPT = "schema.sql";

  private DatabaseResetUtils() {}

  public static void resetSchema(DataSource dataSource) {
    ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
    populator.addScript(new ClassPathResource(SCHEMA_SCRIPT));
    populator.setSqlScriptEncoding("UTF-8");
    DatabasePopulatorUtils.execute(populator, dataSource);
  }
}
