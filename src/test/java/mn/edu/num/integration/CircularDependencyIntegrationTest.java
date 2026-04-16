package mn.edu.num.integration;

import mn.edu.num.container.ApplicationContext;
import mn.edu.num.exception.CircularDependencyException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CircularDependencyIntegrationTest {

    @Test
    void shouldThrowExceptionWhenCircularDependencyExists() {
        // 2-н талт хамаарал: Chicken -> Egg -> Chicken
        CircularDependencyException ex = assertThrows(
                CircularDependencyException.class,
                () -> new ApplicationContext("mn.edu.num.app.circular")
        );

        System.out.println("✓ Exception message: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("Тойрог хамаарал илэрлээ"));
    }

    @Test
    void shouldThrowExceptionWhenThreeWayCircularDependencyExists() {
        // 3-н талт хамаарал: Alpha -> Beta -> Gamma -> Alpha
        CircularDependencyException ex = assertThrows(
                CircularDependencyException.class,
                () -> new ApplicationContext("mn.edu.num.app.circular3")
        );

        System.out.println("✓ Exception message: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("Тойрог хамаарал илэрлээ"));
    }
}