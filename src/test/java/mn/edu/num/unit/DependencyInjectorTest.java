package mn.edu.num.unit;

import mn.edu.num.annotation.Autowired;
import mn.edu.num.annotation.Component;
import mn.edu.num.annotation.Qualifier;
import mn.edu.num.container.BeanDefinition;
import mn.edu.num.container.BeanRegistry;
import mn.edu.num.container.DependencyInjector;
import mn.edu.num.container.ScopeType;
import mn.edu.num.exception.BeanCreationException;
import mn.edu.num.exception.NoSuchBeanException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DependencyInjectorTest {

    private BeanRegistry registry;
    private DependencyInjector injector;

    @BeforeEach
    void setUp() {
        registry = new BeanRegistry();
        injector = new DependencyInjector(registry);
    }

    @Component
    static class ServiceA {}

    @Component
    static class ServiceB {
        @Autowired
        ServiceA serviceA;
    }

    @Component
    static class ServiceC {
        @Autowired
        @Qualifier("specialA")
        ServiceA serviceA;
    }

    @Test
    void shouldInjectDependencyByType() {
        ServiceA a = new ServiceA();
        ServiceB b = new ServiceB();

        registry.register(new BeanDefinition("serviceA", ServiceA.class, ScopeType.SINGLETON));
        registry.cacheSingleton("serviceA", a);

        DependencyInjector.ApplicationContextRef mockContext = name -> a;

        injector.inject(b, new BeanDefinition("serviceB", ServiceB.class, ScopeType.SINGLETON), mockContext);

        assertNotNull(b.serviceA);
        assertEquals(a, b.serviceA);
    }

    @Test
    void shouldInjectDependencyByNameUsingQualifier() {
        ServiceA specialA = new ServiceA();
        ServiceC c = new ServiceC();

        registry.register(new BeanDefinition("specialA", ServiceA.class, ScopeType.SINGLETON));
        registry.cacheSingleton("specialA", specialA);

        DependencyInjector.ApplicationContextRef mockContext = name -> {
            if ("specialA".equals(name)) return specialA;
            return null;
        };

        injector.inject(c, new BeanDefinition("serviceC", ServiceC.class, ScopeType.SINGLETON), mockContext);

        assertNotNull(c.serviceA);
        assertEquals(specialA, c.serviceA);
    }

    @Test
    void shouldThrowExceptionWhenDependencyNotFound() {
        ServiceB b = new ServiceB();
        // ServiceA is not registered

        DependencyInjector.ApplicationContextRef mockContext = name -> null;

        assertThrows(NoSuchBeanException.class, () ->
            injector.inject(b, new BeanDefinition("serviceB", ServiceB.class, ScopeType.SINGLETON), mockContext)
        );
    }

    @Test
    void shouldThrowExceptionWhenMultipleCandidatesFound() {
        ServiceB b = new ServiceB();
        // Register two beans of type ServiceA
        registry.register(new BeanDefinition("serviceA1", ServiceA.class, ScopeType.SINGLETON));
        registry.register(new BeanDefinition("serviceA2", ServiceA.class, ScopeType.SINGLETON));

        DependencyInjector.ApplicationContextRef mockContext = name -> null;

        NoSuchBeanException ex = assertThrows(NoSuchBeanException.class, () ->
            injector.inject(b, new BeanDefinition("serviceB", ServiceB.class, ScopeType.SINGLETON), mockContext)
        );
        assertTrue(ex.getMessage().contains("Олон bean тохирч байна"));
    }

    @Test
    void shouldThrowExceptionWhenDependencyNotFoundWithMessage() {
        ServiceB b = new ServiceB();
        // ServiceA is not registered
        registry.register(new BeanDefinition("otherBean", String.class, ScopeType.SINGLETON));

        DependencyInjector.ApplicationContextRef mockContext = name -> null;

        NoSuchBeanException ex = assertThrows(NoSuchBeanException.class, () ->
            injector.inject(b, new BeanDefinition("serviceB", ServiceB.class, ScopeType.SINGLETON), mockContext)
        );
        assertTrue(ex.getMessage().contains("Dependency олдсонгүй"));
        assertTrue(ex.getMessage().contains("Бүртгэлтэй bean-үүд: [otherBean]"));
    }

    @Test
    void shouldThrowBeanCreationExceptionWhenFieldSetThrowsIllegalAccess() throws Exception {
        // DependencyInjector.java:58-61 мөрийг тестлэх:
        // catch (IllegalAccessException e) {
        //     throw new BeanCreationException("Field inject хийхэд алдаа: " + field.getName(), e);
        // }
        // final field руу inject хийхэд Java 17+ дээр IllegalAccessException гардаг

        // final @Autowired field-тэй туслах класс
        ServiceA a = new ServiceA();
        FinalFieldService target = new FinalFieldService();

        registry.register(new BeanDefinition("serviceA", ServiceA.class, ScopeType.SINGLETON));
        registry.cacheSingleton("serviceA", a);

        DependencyInjector.ApplicationContextRef mockContext = name -> a;

        BeanCreationException ex = assertThrows(BeanCreationException.class, () ->
            injector.inject(target,
                new BeanDefinition("finalFieldService", FinalFieldService.class, ScopeType.SINGLETON),
                mockContext)
        );

        System.out.println("✓ Exception message: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("Field inject хийхэд алдаа"));
        assertTrue(ex.getMessage().contains("serviceA"));
        assertNotNull(ex.getCause());
    }

    // static final field-тэй туслах класс - Java 12+ дээр static final field.set() нь IllegalAccessException шиднэ
    @Component
    static class FinalFieldService {
        @Autowired
        static final ServiceA serviceA = null;
    }
}
