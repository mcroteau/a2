package spike.config;

import n.annotate.Dependency;
import n.jdbc.BasicDataSource;

public class DbConfig {
    /**
     * Dependency tells M to instantiate
     * the defined bean.
     *
     * Method names are bean definitions.
     * Here method dataSource will be stored as
     * 'datasource'. A 'datasource' bean is required if
     * data is enabled.
     *
     * @return BasicDataSource
     */
    @Dependency
    public BasicDataSource dataSource(){
        String url = "jdbc:h2:tcp://localhost:9092/" + BasicDataSource.getH2Path();
        return new BasicDataSource.Builder()
                .dbName("app")
                .driver("org.h2.Driver")
                .url(url)
                .username("sa")
                .password("")
                .build();
    }
}
