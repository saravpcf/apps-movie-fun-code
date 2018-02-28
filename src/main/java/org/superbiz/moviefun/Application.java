package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Map;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials dbCred(@Value("${VCAP_SERVICES:{\"p-mysql\": [{\"credentials\": {\"jdbcUrl\": \"jdbc:mysql://127.0.0.1:3306/albums?user=root\"}, \"name\": \"albums-mysql\"}, {\"credentials\": {\"jdbcUrl\": \"jdbc:mysql://127.0.0.1:3306/movies?user=root\"}, \"name\": \"movies-mysql\"}]}}") String vcapSvc) {
        return new DatabaseServiceCredentials(vcapSvc);
    }

    @Bean("albumsDataSource")
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("albums-mysql", "p-mysql"));
        HikariDataSource hkDs = new HikariDataSource();
        hkDs.setDataSource(dataSource);
        return hkDs;
    }

    @Bean("moviesDataSource")
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("movies-mysql", "p-mysql"));
        HikariDataSource hkDs = new HikariDataSource();
        hkDs.setDataSource(dataSource);
        return hkDs;
    }

    @Bean
    public HibernateJpaVendorAdapter entityMgr() {
        HibernateJpaVendorAdapter hbJPA = new HibernateJpaVendorAdapter(){

            @Override
            public Map<String, Object> getJpaPropertyMap() {
                Map<String, Object> jpaPropertyMap = super.getJpaPropertyMap();
//                jpaPropertyMap.put("hibernate.hbm2ddl.auto", "create-drop");
                return jpaPropertyMap;
            }
        };
        hbJPA.setDatabase(Database.MYSQL);
        hbJPA.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        hbJPA.setGenerateDdl(true);
        hbJPA.setShowSql(true);
        return hbJPA;
    }

    @Bean("moviesDS")
    public LocalContainerEntityManagerFactoryBean moviesDS(@Qualifier("moviesDataSource") DataSource ds, HibernateJpaVendorAdapter hbJPA) {
        LocalContainerEntityManagerFactoryBean lcEM = new LocalContainerEntityManagerFactoryBean();
        lcEM.setDataSource(ds);
        lcEM.setJpaVendorAdapter(hbJPA);
        lcEM.setPackagesToScan("org.superbiz.moviefun.movies");
        lcEM.setPersistenceUnitName("movies");
        return lcEM;
    }

    @Bean("albumsDS")
    public LocalContainerEntityManagerFactoryBean albumsDS(@Qualifier("albumsDataSource") DataSource ds, HibernateJpaVendorAdapter hbJPA) {
        LocalContainerEntityManagerFactoryBean lcEM = new LocalContainerEntityManagerFactoryBean();
        lcEM.setDataSource(ds);
        lcEM.setJpaVendorAdapter(hbJPA);
        lcEM.setPackagesToScan("org.superbiz.moviefun.albums");
        lcEM.setPersistenceUnitName("albums");
        return lcEM;
    }

    @Bean("moviesPTM")
    public PlatformTransactionManager moviesPTM(@Qualifier("moviesDS") LocalContainerEntityManagerFactoryBean lcEM){
        JpaTransactionManager jpaMgr = new JpaTransactionManager(lcEM.getObject());
        return jpaMgr;
    }

    @Bean("albumsPTM")
    public PlatformTransactionManager albumsPTM(@Qualifier("albumsDS") LocalContainerEntityManagerFactoryBean lcEM){
        JpaTransactionManager jpaMgr = new JpaTransactionManager(lcEM.getObject());
        return jpaMgr;
    }

}
