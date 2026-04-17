package mn.edu.num;

import mn.edu.num.annotation.EnableIoC;
import mn.edu.num.container.ApplicationContext;
import mn.edu.num.container.DependencyTreeBuilder;
import mn.edu.num.container.DependencyTreeExporter;

/**
 * Төслийн эхлэл цэг.
 * @EnableIoC annotation нь энэ классын package (mn.edu.num)-аас
 * эхлэн бүх дэд package-уудыг scan хийж @Component bean-уудыг олно.
 *
 * Spring Boot-ийн @SpringBootApplication + SpringApplication.run()-тай адил загвар.
 */
@EnableIoC(
        visualize = true,
        excludePackages = {
                "mn.edu.num.app.broken",
                "mn.edu.num.app.circular",
                "mn.edu.num.app.circular3",
                "mn.edu.num.app.throwing"
        }
)
public class Main {
    public static void main(String[] args) {
        // Spring Boot: SpringApplication.run(MyApp.class, args)
        // Манай IoC:   ApplicationContext.run(Main.class)
        ApplicationContext ctx = ApplicationContext.run(Main.class);

        // Dependency tree
        DependencyTreeBuilder builder = ctx.getTreeBuilder();

        System.out.println("\n--- Dependency Tree (Console) ---");
        System.out.println(builder.printTree());

        System.out.println("--- Dependency Tree (JSON) ---");
        System.out.println(DependencyTreeExporter.toJson(builder.getNodeMap()));
    }
}