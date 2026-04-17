package mn.edu.num.integration;

import mn.edu.num.container.*;
import mn.edu.num.exception.CircularDependencyException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ФБШ-16: Dependency tree үүсгэх, node тоо, холбоос, тойрог хамаарал илрүүлэлтийн тест.
 */
class DependencyTreeIntegrationTest {

    @Test
    void shouldBuildTreeWithCorrectNodeCount() {
        // conflict package: UserService, EmailService, OrderService
        ApplicationContext ctx = new ApplicationContext("mn.edu.num.app.conflict");
        DependencyTreeBuilder builder = ctx.getTreeBuilder();
        Map<String, DependencyNode> nodeMap = builder.getNodeMap();

        assertEquals(3, nodeMap.size(), "3 bean бүртгэгдсэн байх ёстой");
        assertTrue(nodeMap.containsKey("userService"));
        assertTrue(nodeMap.containsKey("emailService"));
        assertTrue(nodeMap.containsKey("orderService"));
    }

    @Test
    void shouldHaveCorrectDependencies() {
        ApplicationContext ctx = new ApplicationContext("mn.edu.num.app.conflict");
        DependencyTreeBuilder builder = ctx.getTreeBuilder();
        Map<String, DependencyNode> nodeMap = builder.getNodeMap();

        DependencyNode userNode = nodeMap.get("userService");
        assertNotNull(userNode);
        assertEquals(2, userNode.getDependencies().size(),
                "UserService нь 2 dependency-тэй: EmailService, OrderService");

        // EmailService, OrderService нь dependency-гүй
        DependencyNode emailNode = nodeMap.get("emailService");
        assertTrue(emailNode.getDependencies().isEmpty());

        DependencyNode orderNode = nodeMap.get("orderService");
        assertTrue(orderNode.getDependencies().isEmpty());
    }

    @Test
    void shouldDetectCircularDependencyWithDFS() {
        // circular package-д Chicken ↔ Egg тойрог хамаарал байгаа
        // Container эхлүүлэхэд CircularDependencyException үүсэх учир
        // tree builder-г шууд registry дээр тестлэнэ
        BeanRegistry registry = new BeanRegistry();
        registry.register(new BeanDefinition("chicken",
                mn.edu.num.app.circular.Chicken.class, ScopeType.SINGLETON));
        registry.register(new BeanDefinition("egg",
                mn.edu.num.app.circular.Egg.class, ScopeType.SINGLETON));

        DependencyTreeBuilder builder = new DependencyTreeBuilder(registry);
        builder.buildTree();

        assertThrows(CircularDependencyException.class, builder::detectCircularDependencies,
                "DFS тойрог хамаарлыг илрүүлэх ёстой");
    }

    @Test
    void shouldPrintTreeFormat() {
        ApplicationContext ctx = new ApplicationContext("mn.edu.num.app.conflict");
        DependencyTreeBuilder builder = ctx.getTreeBuilder();

        String tree = builder.printTree();
        assertNotNull(tree);
        assertTrue(tree.contains("ApplicationContext"), "Tree нь ApplicationContext header-тэй байх ёстой");
        assertTrue(tree.contains("userService"), "Tree-д userService байх ёстой");
        assertTrue(tree.contains("SINGLETON"), "Scope харагдах ёстой");

        System.out.println(tree);
    }

    @Test
    void shouldExportToJson() {
        ApplicationContext ctx = new ApplicationContext("mn.edu.num.app.conflict");
        DependencyTreeBuilder builder = ctx.getTreeBuilder();

        String json = DependencyTreeExporter.toJson(builder.getNodeMap());
        assertNotNull(json);
        assertTrue(json.contains("\"beans\""));
        assertTrue(json.contains("\"userService\""));
        assertTrue(json.contains("\"dependencies\""));

        System.out.println(json);
    }

    @Test
    void shouldExportInteractiveHtmlWithSearch() {
        ApplicationContext ctx = new ApplicationContext("mn.edu.num.app.conflict");
        DependencyTreeBuilder builder = ctx.getTreeBuilder();

        String html = DependencyTreeHtmlExporter.generateHtml(builder.getNodeMap());
        assertNotNull(html);
        assertTrue(html.contains("Dependency Tree Dashboard"));
        assertTrue(html.contains("id=\"search-input\""));
        assertTrue(html.contains("searchNodes()"));
        assertTrue(html.contains("clearSearch()"));
        assertTrue(html.contains("id=\"search-count\""));
        assertTrue(html.contains("class=\"tree-panel\""));
        assertTrue(html.contains("class=\"detail-panel\""));
        assertTrue(html.contains("data-search=\"userservice mn.edu.num.app.conflict.userservice\""));
    }
}

