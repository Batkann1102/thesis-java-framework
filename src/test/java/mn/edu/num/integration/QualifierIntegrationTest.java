package mn.edu.num.integration;

import mn.edu.num.app.qualifer.presistence.MongoUserRepository;
import mn.edu.num.app.qualifer.service.ServiceFactory;
import mn.edu.num.container.ApplicationContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QualifierIntegrationTest {

    @Test
    void shouldInjectMongoRepositoryUsingQualifier() {
        ApplicationContext ctx = new ApplicationContext("mn.edu.num.app.qualifer");
        ServiceFactory serviceFactory = ctx.getBean(ServiceFactory.class);

        assertNotNull(serviceFactory);
        assertEquals("MongoDB Repository", serviceFactory.getRepoType());
    }

    @Test
    void shouldReturnMongoBeanWhenRequestedByCustomName() {
        ApplicationContext ctx = new ApplicationContext("mn.edu.num.app.qualifer");
        Object bean = ctx.getBean("MongoRepository");

        assertNotNull(bean);
        assertInstanceOf(MongoUserRepository.class, bean);

        MongoUserRepository repository = (MongoUserRepository) bean;
        assertEquals("MongoDB Repository", repository.getType());
    }

    @Test
    void shouldReturnJDBCBeanWhenRequestedByCustomName() {
        ApplicationContext ctx = new ApplicationContext("mn.edu.num.app.qualifer");
        Object bean = ctx.getBean("JDBCRepository");

        assertNotNull(bean);
        assertInstanceOf(mn.edu.num.app.qualifer.presistence.JDBCMySQLRepository.class, bean);

        mn.edu.num.app.qualifer.presistence.JDBCMySQLRepository repository = 
                (mn.edu.num.app.qualifer.presistence.JDBCMySQLRepository) bean;
        assertEquals("JDBC Repository", repository.getType());
    }
}