package mn.edu.num.integration;

import mn.edu.num.container.ApplicationContext;
import mn.edu.num.exception.BeanCreationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BeanCreationIntegrationTest {

    @Test
    void shouldThrowBeanCreationExceptionWhenDefaultConstructorIsMissing() {
        BeanCreationException ex = assertThrows(
                BeanCreationException.class,
                () -> new ApplicationContext("mn.edu.num.app.broken")
        );

        System.out.println("✓ Exception message: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("noDefaultConstructor"));
        assertTrue(ex.getMessage().contains("Default constructor олдсонгүй"));
    }

    @Test
    void shouldThrowBeanCreationExceptionWhenConstructorThrowsException() {
        // ApplicationContext.java:106 хэсгийг шалгах
        // throw new BeanCreationException(name, e.getMessage(), e);
        BeanCreationException ex = assertThrows(
                BeanCreationException.class,
                () -> new ApplicationContext("mn.edu.num.app.broken")
        );

        System.out.println("✓ Exception message: " + ex.getMessage());
        // broken package дотор NoDefaultConstructor болон ThrowingConstructor байгаа тул аль нэг дээр нь алдаа гарна
        assertTrue(ex.getMessage().contains("Bean үүсгэхэд алдаа"));
    }

    @Test
    void shouldThrowBeanCreationExceptionWhenConstructorThrowsGeneralException() {
        // ApplicationContext.java:105-106 мөрийг тестлэх:
        // catch (Exception e) { throw new BeanCreationException(name, e.getMessage(), e); }
        // throwing багцад зөвхөн ConstructorThrowingBean байгаа бөгөөд
        // default constructor нь IllegalStateException шиддэг
        BeanCreationException ex = assertThrows(
                BeanCreationException.class,
                () -> new ApplicationContext("mn.edu.num.app.throwing")
        );

        System.out.println("✓ Exception message: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("Bean үүсгэхэд алдаа"));
        assertTrue(ex.getMessage().contains("constructorThrowingBean"));
        assertNotNull(ex.getCause(), "Жинхэнэ алдааны шалтгаан (cause) байх ёстой");
    }
}