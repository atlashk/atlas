package org.atlas.libs.persistence.jpa.helper;

import jakarta.persistence.EntityManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.persistence.DatabaseType;
import org.atlas.libs.framework.persistence.DatabaseTypeResolver;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MariaDBDialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.OracleDialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaDatabaseTypeResolver implements DatabaseTypeResolver, InitializingBean {

  private final EntityManager entityManager;

  private DatabaseType databaseType;

  @Override
  public void afterPropertiesSet() throws Exception {
    // Don't close SessionFactoryImplementor
    SessionFactoryImplementor sessionFactory = entityManager.getEntityManagerFactory()
            .unwrap(SessionFactoryImplementor.class);
    Dialect dialect = sessionFactory.getJdbcServices().getDialect();
    if (dialect instanceof PostgreSQLDialect) {
      databaseType = DatabaseType.POSTGRES;
    } else if (dialect instanceof MariaDBDialect) {
      databaseType = DatabaseType.MARIADB;
    } else if (dialect instanceof MySQLDialect) {
      databaseType = DatabaseType.MYSQL;
    } else if (dialect instanceof OracleDialect) {
      databaseType = DatabaseType.ORACLE;
    }
  }

  @Override
  public DatabaseType resolve() {
    return databaseType;
  }
}
