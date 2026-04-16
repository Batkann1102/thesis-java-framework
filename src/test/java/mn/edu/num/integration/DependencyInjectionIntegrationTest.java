package mn.edu.num.integration;

import mn.edu.num.app.conflict.UserService;
import mn.edu.num.container.ApplicationContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DependencyInjectionIntegrationTest {

    @Test
    void shouldInjectEmailServiceIntoUserService() {
        ApplicationContext ctx = new ApplicationContext("mn.edu.num.app.conflict");
        UserService userService = ctx.getBean(UserService.class);

        assertNotNull(userService);
        assertEquals("Имэйл илгээлээ", userService.register());
    }

    @Test
    void shouldInjectOrderServiceIntoUserService() {
        ApplicationContext ctx = new ApplicationContext("mn.edu.num.app.conflict");
        UserService userService = ctx.getBean(UserService.class);

        assertNotNull(userService);
        assertEquals("Захиалга хийгдлээ!", userService.orderRegister());
    }
}