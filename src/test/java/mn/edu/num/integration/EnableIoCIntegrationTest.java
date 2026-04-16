package mn.edu.num.integration;

import mn.edu.num.annotation.EnableIoC;
import mn.edu.num.container.ApplicationContext;
import mn.edu.num.container.DependencyNode;
import mn.edu.num.container.DependencyTreeBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @EnableIoC annotation болон ApplicationContext.run() integration тест.
 *
 * Энэ тест нь:
 * - @EnableIoC annotation-тай класс дамжуулахад container зөв эхлэх
 * - scanPackages тохиргоогоор тодорхой package scan хийх
 * - excludePackages тохиргоогоор package алгасах
 * - @EnableIoC annotation-гүй класс дамжуулахад default байдлаар ажиллах
 * зэрэг бодит нөхцөлүүдийг шалгана.
 */
class EnableIoCIntegrationTest {

    // ─── Тестийн тохиргооны классууд ───

    /** Зөвхөн conflict package scan хийх тохиргоо */
    @EnableIoC(scanPackages = "mn.edu.num.app.conflict")
    static class ScanConflictOnly {}

    /** Exclude тохиргоотой — broken, circular, throwing алгасна */
    @EnableIoC(
            scanPackages = "mn.edu.num.app",
            excludePackages = {
                    "mn.edu.num.app.broken",
                    "mn.edu.num.app.circular",
                    "mn.edu.num.app.circular3",
                    "mn.edu.num.app.throwing"
            }
    )
    static class ExcludeBrokenPackages {}

    /** scanPackages + excludePackages хоёулаа зааж өгсөн */
    @EnableIoC(
            scanPackages = "mn.edu.num.app",
            excludePackages = {
                    "mn.edu.num.app.broken",
                    "mn.edu.num.app.circular",
                    "mn.edu.num.app.circular3",
                    "mn.edu.num.app.throwing"
            }
    )
    static class ScanWithExclude {}

    /** @EnableIoC annotation-гүй класс */
    static class NoAnnotationClass {}

    // ─── Тестүүд ───

    @Test
    @DisplayName("scanPackages зааж өгвөл зөвхөн тухайн package-аас scan хийнэ")
    void shouldScanOnlySpecifiedPackage() {
        ApplicationContext ctx = ApplicationContext.run(ScanConflictOnly.class);
        DependencyTreeBuilder builder = ctx.getTreeBuilder();
        Map<String, DependencyNode> nodeMap = builder.getNodeMap();

        // conflict package-д 3 bean байна: userService, emailService, orderService
        assertEquals(3, nodeMap.size(), "Зөвхөн conflict package-ийн 3 bean олдох ёстой");
        assertTrue(nodeMap.containsKey("userService"));
        assertTrue(nodeMap.containsKey("emailService"));
        assertTrue(nodeMap.containsKey("orderService"));
    }

    @Test
    @DisplayName("excludePackages зааж өгвөл тухайн package-уудыг алгасна")
    void shouldExcludeSpecifiedPackages() {
        ApplicationContext ctx = ApplicationContext.run(ExcludeBrokenPackages.class);
        DependencyTreeBuilder builder = ctx.getTreeBuilder();
        Map<String, DependencyNode> nodeMap = builder.getNodeMap();

        // broken, circular, throwing package-ийн bean-ууд байхгүй байх ёстой
        for (DependencyNode node : nodeMap.values()) {
            String typeName = node.getType().getName();
            assertFalse(typeName.contains(".broken."),
                    "broken package-ийн bean байж болохгүй: " + typeName);
            assertFalse(typeName.contains(".circular."),
                    "circular package-ийн bean байж болохгүй: " + typeName);
            assertFalse(typeName.contains(".throwing."),
                    "throwing package-ийн bean байж болохгүй: " + typeName);
        }

        // Зөв bean-ууд олдсон байх ёстой
        assertTrue(nodeMap.containsKey("userService"), "userService олдох ёстой");
    }

    @Test
    @DisplayName("scanPackages + excludePackages хамтдаа зөв ажиллана")
    void shouldScanWithExcludeTogether() {
        ApplicationContext ctx = ApplicationContext.run(ScanWithExclude.class);
        DependencyTreeBuilder builder = ctx.getTreeBuilder();
        Map<String, DependencyNode> nodeMap = builder.getNodeMap();

        // app дотроос broken, circular, throwing-ийг хассан бусад бүх bean олдоно
        assertTrue(nodeMap.size() >= 3, "Хамгийн багадаа 3 bean олдох ёстой");
        assertTrue(nodeMap.containsKey("userService"));

        // Exclude хийсэн package-ийн bean байхгүй
        for (DependencyNode node : nodeMap.values()) {
            String typeName = node.getType().getName();
            assertFalse(typeName.contains(".broken."));
            assertFalse(typeName.contains(".circular."));
        }
    }

    @Test
    @DisplayName("@EnableIoC annotation-гүй класс дамжуулахад default scan хийнэ")
    void shouldWorkWithoutAnnotation() {
        // @EnableIoC байхгүй ч run() ажиллах ёстой — классын package-аас scan хийнэ
        // NoAnnotationClass нь mn.edu.num.integration package-д байгаа
        // тиймээс integration package-д @Component байхгүй учир bean олдохгүй
        ApplicationContext ctx = ApplicationContext.run(NoAnnotationClass.class);
        DependencyTreeBuilder builder = ctx.getTreeBuilder();

        // integration package-д @Component класс байхгүй тул хоосон байна
        assertTrue(builder.getNodeMap().isEmpty(),
                "@EnableIoC байхгүй, integration package-д bean байхгүй учир хоосон");
    }

    @Test
    @DisplayName("run() дуудахад dependency tree зөв үүсч, bean-ууд inject хийгдсэн байна")
    void shouldBuildTreeAndInjectDependencies() {
        ApplicationContext ctx = ApplicationContext.run(ScanConflictOnly.class);

        // Bean-уудыг авч шалгана
        Object userService = ctx.getBean("userService");
        assertNotNull(userService, "userService bean үүссэн байх ёстой");

        // Tree-д dependency холбоос зөв тогтоогдсон
        DependencyNode userNode = ctx.getTreeBuilder().getNodeMap().get("userService");
        assertEquals(2, userNode.getDependencies().size(),
                "userService нь emailService, orderService гэсэн 2 dependency-тэй");
    }

    @Test
    @DisplayName("@EnableIoC annotation runtime-д хадгалагдаж, reflection-оор уншигдана")
    void shouldRetainAnnotationAtRuntime() {
        assertTrue(ScanConflictOnly.class.isAnnotationPresent(
                        mn.edu.num.annotation.EnableIoC.class),
                "@EnableIoC annotation runtime-д хадгалагдсан байх ёстой");

        EnableIoC config = ScanConflictOnly.class.getAnnotation(
                mn.edu.num.annotation.EnableIoC.class);
        assertNotNull(config);
        assertEquals(1, config.scanPackages().length);
        assertEquals("mn.edu.num.app.conflict", config.scanPackages()[0]);
        assertEquals(0, config.excludePackages().length);
    }
}

