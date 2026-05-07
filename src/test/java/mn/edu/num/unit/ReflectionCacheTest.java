package mn.edu.num.unit;

import mn.edu.num.annotation.Autowired;
import mn.edu.num.annotation.PostConstruct;
import mn.edu.num.annotation.PreDestroy;
import mn.edu.num.container.ReflectionCache;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionCacheTest {

    static class Foo {
        @Autowired Bar bar;

        @PostConstruct
        public void init() {}

        @PreDestroy
        public void shutdown() {}
    }

    static class Bar {}

    static class WithCtor {
        final Bar bar;
        @Autowired
        public WithCtor(Bar bar) {
            this.bar = bar;
        }
    }

    @Test
    void shouldCacheMetadataPerClass() {
        ReflectionCache cache = new ReflectionCache();
        ReflectionCache.ClassMetadata m1 = cache.metadataFor(Foo.class);
        ReflectionCache.ClassMetadata m2 = cache.metadataFor(Foo.class);
        assertSame(m1, m2, "metadata кэшлэгдсэн байх ёстой");
        assertEquals(1, cache.size());
    }

    @Test
    void shouldDetectAutowiredFields() {
        ReflectionCache.ClassMetadata m = new ReflectionCache().metadataFor(Foo.class);
        assertEquals(1, m.autowiredFields().size());
        assertEquals("bar", m.autowiredFields().get(0).field().getName());
        assertTrue(m.autowiredFields().get(0).required());
    }

    @Test
    void shouldDetectLifecycleMethods() {
        ReflectionCache.ClassMetadata m = new ReflectionCache().metadataFor(Foo.class);
        assertEquals(1, m.postConstructMethods().size());
        assertEquals("init", m.postConstructMethods().get(0).getName());
        assertEquals(1, m.preDestroyMethods().size());
        assertEquals("shutdown", m.preDestroyMethods().get(0).getName());
    }

    @Test
    void shouldDetectAutowiredConstructor() {
        ReflectionCache.ClassMetadata m = new ReflectionCache().metadataFor(WithCtor.class);
        assertNotNull(m.autowiredConstructor());
        assertEquals(1, m.autowiredConstructorParameters().size());
        assertEquals(Bar.class, m.autowiredConstructorParameters().get(0).type());
    }
}
