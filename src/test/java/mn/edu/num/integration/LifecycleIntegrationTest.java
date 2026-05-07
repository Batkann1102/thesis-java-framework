package mn.edu.num.integration;

import mn.edu.num.app.lifecycle.LifecycleBean;
import mn.edu.num.container.ApplicationContext;
import mn.edu.num.container.ApplicationContextBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LifecycleIntegrationTest {

    @BeforeEach
    void clearEvents() {
        LifecycleBean.events.clear();
    }

    @Test
    void shouldInvokePostConstructAfterInjection() {
        try (ApplicationContext ctx = ApplicationContextBuilder.builder()
                .basePackage("mn.edu.num.app.lifecycle")
                .build()) {
            LifecycleBean bean = ctx.getBean(LifecycleBean.class);
            assertTrue(bean.initialized, "@PostConstruct хийж дуудагдсан байх ёстой");
            assertTrue(LifecycleBean.events.contains("init:LifecycleBean"));
        }
    }

    @Test
    void shouldInvokePreDestroyOnClose() {
        ApplicationContext ctx = ApplicationContextBuilder.builder()
                .basePackage("mn.edu.num.app.lifecycle")
                .build();
        LifecycleBean bean = ctx.getBean(LifecycleBean.class);
        assertFalse(bean.destroyed);
        ctx.close();
        assertTrue(bean.destroyed, "@PreDestroy дуудагдсан байх ёстой");
        assertTrue(LifecycleBean.events.contains("destroy:LifecycleBean"));
    }

    @Test
    void shouldThrowAfterClose() {
        ApplicationContext ctx = ApplicationContextBuilder.builder()
                .basePackage("mn.edu.num.app.lifecycle")
                .build();
        ctx.close();
        assertTrue(ctx.isClosed());
        assertThrows(IllegalStateException.class, () -> ctx.getBean(LifecycleBean.class));
    }

    @Test
    void closeShouldBeIdempotent() {
        ApplicationContext ctx = ApplicationContextBuilder.builder()
                .basePackage("mn.edu.num.app.lifecycle")
                .build();
        ctx.close();
        ctx.close(); // дахин дуудах нь алдаа гаргахгүй
        assertTrue(ctx.isClosed());
    }
}
