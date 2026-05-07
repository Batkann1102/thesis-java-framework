package mn.edu.num.integration;

import mn.edu.num.container.ApplicationContext;
import mn.edu.num.container.ApplicationContextBuilder;
import mn.edu.num.exception.BeanScopeException;
import mn.edu.num.exception.NoSuchBeanException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LazyAndStrictScopeIntegrationTest {

    @Test
    void lazyInitShouldDeferSingletonCreation() {
        ApplicationContext ctx = ApplicationContextBuilder.builder()
                .basePackage("mn.edu.num.app.scope")
                .lazyInit(true)
                .build();
        assertTrue(ctx.isLazyInit());
        // bean count should still match scanned beans
        assertTrue(ctx.beanCount() >= 1);
        ctx.close();
    }

    @Test
    void strictScopeShouldThrowOnUnknownScope() {
        BeanScopeException ex = assertThrows(BeanScopeException.class, () ->
                ApplicationContextBuilder.builder()
                        .basePackage("mn.edu.num.app.scope")
                        .strictScope(true)
                        .build());
        assertTrue(ex.getMessage().contains("invalidScopeService"));
    }

    @Test
    void didYouMeanShouldSuggestSimilarBeanName() {
        try (ApplicationContext ctx = ApplicationContextBuilder.builder()
                .basePackage("mn.edu.num.app.scope")
                .build()) {
            NoSuchBeanException ex = assertThrows(NoSuchBeanException.class, () ->
                    ctx.getBean("singletonServic")); // ihrer typo
            assertTrue(ex.getMessage().contains("Магадгүй та"),
                    "did-you-mean санал илрэх ёстой: " + ex.getMessage());
        }
    }
}
