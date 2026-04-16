package mn.edu.num.unit;

import mn.edu.num.container.BeanDefinition;
import mn.edu.num.container.BeanRegistry;
import mn.edu.num.container.ScopeType;
import mn.edu.num.exception.NoSuchBeanException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BeanRegistryTest {

    private BeanRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new BeanRegistry();
    }

    @Test
    void shouldRegisterAndRetrieveBeanDefinition() {
        BeanDefinition def = new BeanDefinition("myBean", String.class, ScopeType.SINGLETON);
        registry.register(def);

        BeanDefinition retrieved = registry.getDefinition("myBean");
        assertNotNull(retrieved);
        assertEquals("myBean", retrieved.getBeanName());
        assertEquals(String.class, retrieved.getBeanClass());
    }

    @Test
    void shouldThrowExceptionWhenBeanNotFound() {
        assertThrows(NoSuchBeanException.class, () -> registry.getDefinition("nonExistent"));
    }

    @Test
    void shouldCacheAndRetrieveSingletonInstances() {
        String instance = "Hello World";
        registry.cacheSingleton("myBean", instance);

        assertTrue(registry.hasSingleton("myBean"));
        assertEquals(instance, registry.getSingleton("myBean"));
    }

    @Test
    void shouldFindDefinitionsByType() {
        BeanDefinition def1 = new BeanDefinition("bean1", String.class, ScopeType.SINGLETON);
        BeanDefinition def2 = new BeanDefinition("bean2", Integer.class, ScopeType.SINGLETON);
        registry.register(def1);
        registry.register(def2);

        List<BeanDefinition> stringBeans = registry.findByType(String.class);
        assertEquals(1, stringBeans.size());
        assertEquals(String.class, stringBeans.get(0).getBeanClass());

        List<BeanDefinition> objectBeans = registry.findByType(Object.class);
        // String and Integer are both subclasses of Object
        assertEquals(2, objectBeans.size());
    }

    @Test
    void shouldReturnAllDefinitions() {
        registry.register(new BeanDefinition("b1", String.class, ScopeType.SINGLETON));
        registry.register(new BeanDefinition("b2", Integer.class, ScopeType.SINGLETON));

        Collection<BeanDefinition> all = registry.getAllDefinitions();
        assertEquals(2, all.size());
    }

    @Test
    void shouldOverwriteExistingDefinition() {
        registry.register(new BeanDefinition("myBean", String.class, ScopeType.SINGLETON));
        registry.register(new BeanDefinition("myBean", Integer.class, ScopeType.SINGLETON));

        BeanDefinition retrieved = registry.getDefinition("myBean");
        assertEquals(Integer.class, retrieved.getBeanClass());
    }

    @Test
    void shouldReturnFalseForMissingSingleton() {
        assertFalse(registry.hasSingleton("nonExistent"));
        assertNull(registry.getSingleton("nonExistent"));
    }

    @Test
    void shouldReturnStringRepresentationOfBeanDefinition() {
        BeanDefinition def = new BeanDefinition("myBean", String.class, ScopeType.SINGLETON);
        String str = def.toString();
        
        assertTrue(str.contains("name='myBean'"));
        assertTrue(str.contains("class=String"));
        assertTrue(str.contains("scope=SINGLETON"));
    }
}
