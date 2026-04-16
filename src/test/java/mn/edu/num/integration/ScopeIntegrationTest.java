package mn.edu.num.integration;

import mn.edu.num.container.ApplicationContext;
import mn.edu.num.app.scope.PrototypeService;
import mn.edu.num.app.scope.SingletonService;
import mn.edu.num.app.scope.InvalidScopeService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScopeIntegrationTest {

    @Test
    void shouldReturnDifferentInstancesForPrototypeScope() {
        ApplicationContext ctx = new ApplicationContext("mn.edu.num.app.scope");

        PrototypeService s1 = ctx.getBean(PrototypeService.class);
        PrototypeService s2 = ctx.getBean(PrototypeService.class);

        assertNotNull(s1);
        assertNotNull(s2);
        assertNotSame(s1, s2);
    }

    @Test
    void shouldReturnSameInstanceForSingletonScope() {
        ApplicationContext ctx = new ApplicationContext("mn.edu.num.app.scope");

        SingletonService s1 = ctx.getBean(SingletonService.class);
        SingletonService s2 = ctx.getBean(SingletonService.class);

        assertNotNull(s1);
        assertNotNull(s2);
        assertSame(s1, s2);
    }

    @Test
    void shouldFallbackToSingletonWhenScopeValueIsInvalid() {
        ApplicationContext ctx = new ApplicationContext("mn.edu.num.app.scope");

        InvalidScopeService s1 = ctx.getBean(InvalidScopeService.class);
        InvalidScopeService s2 = ctx.getBean(InvalidScopeService.class);

        assertNotNull(s1);
        assertNotNull(s2);
        assertSame(s1, s2);
    }
}