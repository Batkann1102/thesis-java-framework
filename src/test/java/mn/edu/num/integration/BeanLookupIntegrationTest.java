package mn.edu.num.integration;

import mn.edu.num.container.ApplicationContext;
import mn.edu.num.exception.NoSuchBeanException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BeanLookupIntegrationTest {

    @Test
    void shouldThrowExceptionWhenBeanNameDoesNotExist() {
        ApplicationContext ctx = new ApplicationContext("mn.edu.num.app.conflict");

        NoSuchBeanException ex = assertThrows(
                NoSuchBeanException.class,
                () -> ctx.getBean("байхгүйСервис")
        );

        System.out.println("✓ Exception: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("байхгүйСервис")
                || ex.getMessage().contains("Bean олдсонгүй"));
    }

    @Test
    void shouldThrowExceptionWhenBeanTypeDoesNotExist() {
        ApplicationContext ctx = new ApplicationContext("mn.edu.num.app.conflict");

        NoSuchBeanException ex = assertThrows(
                NoSuchBeanException.class,
                () -> ctx.getBean(String.class)
        );

        System.out.println("✓ Exception: " + ex.getMessage());
        assertNotNull(ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenMultipleBeansMatchByType() {
        ApplicationContext ctx = new ApplicationContext("mn.edu.num.app.qualifer");

        NoSuchBeanException ex = assertThrows(
                NoSuchBeanException.class,
                () -> ctx.getBean(mn.edu.num.app.qualifer.port.UserRepository.class)
        );

        System.out.println("✓ Exception: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("Олон bean тохирч байна"));
    }
}