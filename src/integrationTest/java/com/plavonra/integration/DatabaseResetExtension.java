package com.plavonra.integration;

import java.lang.reflect.Method;
import javax.sql.DataSource;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class DatabaseResetExtension implements BeforeEachCallback {

  private static final Logger log = LoggerFactory.getLogger(DatabaseResetExtension.class);

  @Override
  public void beforeEach(ExtensionContext context) {
    String where =
        context.getRequiredTestClass().getSimpleName()
            + "."
            + context.getTestMethod().map(Method::getName).orElse("?");
    long t0 = System.nanoTime();
    DataSource dataSource =
        SpringExtension.getApplicationContext(context).getBean(DataSource.class);
    DatabaseResetUtils.resetSchema(dataSource);
    log.info("Database reset before [{}] took {} ms", where, (System.nanoTime() - t0) / 1_000_000L);
  }
}
