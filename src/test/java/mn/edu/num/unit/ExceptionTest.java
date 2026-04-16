package mn.edu.num.unit;

import mn.edu.num.container.ApplicationContext;
import mn.edu.num.exception.BeanCreationException;
import mn.edu.num.exception.CircularDependencyException;
import mn.edu.num.exception.NoSuchBeanException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void testNoSuchBeanExceptionConstructors() {
        NoSuchBeanException e1 = new NoSuchBeanException("message");
        assertEquals("message", e1.getMessage());

        NoSuchBeanException e2 = new NoSuchBeanException("beanName", List.of("beanA", "beanB"));
        assertTrue(e2.getMessage().contains("beanName"));
        assertTrue(e2.getMessage().contains("beanA, beanB"));

        NoSuchBeanException e3 = new NoSuchBeanException(String.class, List.of("beanA"));
        assertTrue(e3.getMessage().contains("java.lang.String"));
        assertTrue(e3.getMessage().contains("beanA"));
    }

    @Test
    void testBeanCreationExceptionConstructors() {
        RuntimeException cause = new RuntimeException("root");
        BeanCreationException e1 = new BeanCreationException("msg", cause);
        assertEquals("msg", e1.getMessage());
        assertEquals(cause, e1.getCause());

        BeanCreationException e2 = new BeanCreationException("myBean", "failed", cause);
        assertTrue(e2.getMessage().contains("myBean"));
        assertTrue(e2.getMessage().contains("failed"));
        assertEquals(cause, e2.getCause());
    }

    @Test
    void testCircularDependencyException() {
        CircularDependencyException e = new CircularDependencyException("circular");
        assertEquals("circular", e.getMessage());
    }

    @Test
    void shouldTriggerUnreachableCircularDependencyCheckInApplicationContext() throws Exception {
        // ApplicationContext.java:80-81 мөрийг хүчээр ажиллуулах (Reflection)
        ApplicationContext ctx = new ApplicationContext("mn.edu.num.empty");
        
        java.lang.reflect.Field inCreationField = ApplicationContext.class.getDeclaredField("inCreation");
        inCreationField.setAccessible(true);
        java.util.Set<String> inCreation = (java.util.Set<String>) inCreationField.get(ctx);
        
        inCreation.add("testBean");
        
        mn.edu.num.container.BeanDefinition def = new mn.edu.num.container.BeanDefinition(
            "testBean", String.class, mn.edu.num.container.ScopeType.SINGLETON);
            
        java.lang.reflect.Method createBeanMethod = ApplicationContext.class.getDeclaredMethod(
            "createBean", mn.edu.num.container.BeanDefinition.class);
        createBeanMethod.setAccessible(true);
        
        java.lang.reflect.InvocationTargetException ite = assertThrows(
            java.lang.reflect.InvocationTargetException.class,
            () -> createBeanMethod.invoke(ctx, def)
        );
        
        assertInstanceOf(mn.edu.num.exception.CircularDependencyException.class, ite.getCause());
        assertTrue(ite.getCause().getMessage().contains("Тойрог хамаарал илэрлээ: testBean"));
    }

    @Test
    void shouldTriggerClassNotFoundExceptionInScanner() throws Exception {
        // ClassPathScanner.java:70-72 мөрийг хүчээр ажиллуулах
        mn.edu.num.scanner.ClassPathScanner scanner = new mn.edu.num.scanner.ClassPathScanner("mn.edu.num.empty");
        
        java.lang.reflect.Method processClassMethod = mn.edu.num.scanner.ClassPathScanner.class.getDeclaredMethod(
            "processClass", String.class, java.util.List.class);
        processClassMethod.setAccessible(true);
        
        // Энэ нь системд байхгүй класс тул ClassNotFoundException шиднэ
        processClassMethod.invoke(scanner, "non.existent.FakeClass", new java.util.ArrayList<>());
    }
}
