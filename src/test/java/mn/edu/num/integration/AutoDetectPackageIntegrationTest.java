package mn.edu.num.integration;

import mn.edu.num.container.ApplicationContext;
import mn.edu.num.container.DependencyNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * detectCallerBasePackage() method-ийн integration тест.
 *
 * Энэ тест класс нь mn.edu.num.integration package-д байрлаж байгаа учир
 * new ApplicationContext() дуудахад detectCallerBasePackage() нь
 * "mn.edu.num.integration" гэсэн package олно.
 * Тэр package-д @Component класс байхгүй тул хоосон container үүснэ.
 */
class AutoDetectPackageIntegrationTest {

    @Test
    @DisplayName("No-arg constructor дуудагч классын package-г автоматаар илрүүлнэ")
    void noArgConstructorShouldDetectCallerPackage() {
        // mn.edu.num.integration package-д @Component байхгүй → хоосон container
        ApplicationContext ctx = new ApplicationContext();
        Map<String, DependencyNode> nodeMap = ctx.getTreeBuilder().getNodeMap();

        assertTrue(nodeMap.isEmpty(),
                "mn.edu.num.integration package-д @Component байхгүй учир bean олдохгүй");
    }

    @Test
    @DisplayName("Exclude-тай constructor дуудагч package-г автоматаар илрүүлнэ")
    void excludeConstructorShouldDetectCallerPackage() {
        // Мөн mn.edu.num.integration-аас scan хийнэ — хоосон байна
        ApplicationContext ctx = new ApplicationContext(List.of("mn.edu.num.app.broken"));
        Map<String, DependencyNode> nodeMap = ctx.getTreeBuilder().getNodeMap();

        assertTrue(nodeMap.isEmpty(),
                "mn.edu.num.integration package-д @Component байхгүй учир bean олдохгүй");
    }

    @Test
    @DisplayName("String constructor дамжуулахад тухайн package-аас scan хийнэ")
    void stringConstructorShouldUseGivenPackage() {
        // Шууд package зааж өгвөл тэндээс scan хийнэ
        ApplicationContext ctx = new ApplicationContext("mn.edu.num.app.conflict");
        Map<String, DependencyNode> nodeMap = ctx.getTreeBuilder().getNodeMap();

        assertEquals(3, nodeMap.size(), "conflict package-д 3 bean байна");
        assertTrue(nodeMap.containsKey("userService"));
        assertTrue(nodeMap.containsKey("emailService"));
        assertTrue(nodeMap.containsKey("orderService"));
    }

    @Test
    @DisplayName("Auto-detect болон string constructor-ийн үр дүн ялгаатай байна")
    void autoDetectAndExplicitPackageShouldDiffer() {
        // Auto-detect: mn.edu.num.integration → хоосон
        ApplicationContext autoCtx = new ApplicationContext();
        // Explicit: mn.edu.num.app.conflict → 3 bean
        ApplicationContext explicitCtx = new ApplicationContext("mn.edu.num.app.conflict");

        assertNotEquals(
                autoCtx.getTreeBuilder().getNodeMap().size(),
                explicitCtx.getTreeBuilder().getNodeMap().size(),
                "Auto-detect (integration package) болон explicit (conflict package) ялгаатай байх ёстой"
        );
    }
}

