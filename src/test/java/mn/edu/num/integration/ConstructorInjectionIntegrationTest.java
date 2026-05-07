package mn.edu.num.integration;

import mn.edu.num.app.ctorinject.Repo;
import mn.edu.num.app.ctorinject.Service;
import mn.edu.num.container.ApplicationContext;
import mn.edu.num.container.ApplicationContextBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConstructorInjectionIntegrationTest {

    @Test
    void shouldInjectViaAutowiredConstructor() {
        try (ApplicationContext ctx = ApplicationContextBuilder.builder()
                .basePackage("mn.edu.num.app.ctorinject")
                .build()) {
            Service service = ctx.getBean(Service.class);
            Repo repo = ctx.getBean(Repo.class);
            assertNotNull(service.repo);
            assertSame(repo, service.repo);
            assertEquals("service uses repo", service.describe());
        }
    }
}
