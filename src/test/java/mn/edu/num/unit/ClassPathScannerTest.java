package mn.edu.num.unit;

import mn.edu.num.container.BeanDefinition;
import mn.edu.num.container.ScopeType;
import mn.edu.num.scanner.ClassPathScanner;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClassPathScannerTest {

    @Test
    void shouldScanClassesWithComponentAnnotation() {
        ClassPathScanner scanner = new ClassPathScanner("mn.edu.num.app");
        List<BeanDefinition> definitions = scanner.scan();

        assertFalse(definitions.isEmpty());
        // UserService, EmailService, OrderService should be there
        boolean hasUserService = definitions.stream()
                .anyMatch(d -> d.getBeanClass().getSimpleName().equals("UserService"));
        assertTrue(hasUserService);
    }

    @Test
    void shouldRespectCustomBeanName() {
        ClassPathScanner scanner = new ClassPathScanner("mn.edu.num.app.qualifer.presistence");
        List<BeanDefinition> definitions = scanner.scan();

        // MongoUserRepository has @Component("MongoRepository")
        BeanDefinition mongoDef = definitions.stream()
                .filter(d -> d.getBeanClass().getSimpleName().equals("MongoUserRepository"))
                .findFirst()
                .orElseThrow();

        assertEquals("MongoRepository", mongoDef.getBeanName());
    }

    @Test
    void shouldDefaultToDecapitalizedName() {
        ClassPathScanner scanner = new ClassPathScanner("mn.edu.num.app");
        List<BeanDefinition> definitions = scanner.scan();

        BeanDefinition emailDef = definitions.stream()
                .filter(d -> d.getBeanClass().getSimpleName().equals("EmailService"))
                .findFirst()
                .orElseThrow();

        assertEquals("emailService", emailDef.getBeanName());
    }

    @Test
    void shouldHandlePrototypeScope() {
        ClassPathScanner scanner = new ClassPathScanner("mn.edu.num.app.scope");
        List<BeanDefinition> definitions = scanner.scan();

        BeanDefinition prototypeDef = definitions.stream()
                .filter(d -> d.getBeanClass().getSimpleName().equals("PrototypeService"))
                .findFirst()
                .orElseThrow();

        assertEquals(ScopeType.PROTOTYPE, prototypeDef.getScope());
    }

    @Test
    void shouldReturnEmptyListForNonExistentPackage() {
        ClassPathScanner scanner = new ClassPathScanner("non.existent.package");
        List<BeanDefinition> definitions = scanner.scan();
        assertTrue(definitions.isEmpty());
    }

    @Test
    void shouldIgnoreInterfaces() {
        ClassPathScanner scanner = new ClassPathScanner("mn.edu.num.app.qualifer.port");
        List<BeanDefinition> definitions = scanner.scan();
        // UserRepository is an interface, it should not be registered as a bean
        assertTrue(definitions.isEmpty(), "Interfaces should not be scanned as beans");
    }

    @Test
    void shouldHandleEmptyPackage() {
        ClassPathScanner scanner = new ClassPathScanner("mn.edu.num.empty");
        List<BeanDefinition> definitions = scanner.scan();
        assertTrue(definitions.isEmpty());
    }

    @Test
    void shouldIgnoreNonClassFiles() throws Exception {
        // .class биш файлыг алгасах branch-ыг тестлэх (scanDirectory 42-р мөр)
        java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("scannerTest");
        java.nio.file.Files.createFile(tempDir.resolve("readme.txt"));
        java.nio.file.Files.createFile(tempDir.resolve("config.properties"));

        ClassPathScanner scanner = new ClassPathScanner("mn.edu.num.app");
        Method method = ClassPathScanner.class.getDeclaredMethod(
                "scanDirectory", File.class, String.class, List.class);
        method.setAccessible(true);

        List<BeanDefinition> defs = new ArrayList<>();
        method.invoke(scanner, tempDir.toFile(), "test.pkg", defs);

        assertTrue(defs.isEmpty(), ".class биш файлууд алгасагдах ёстой");

        // Түр файлуудыг цэвэрлэх
        java.nio.file.Files.walk(tempDir)
                .sorted(java.util.Comparator.reverseOrder())
                .forEach(p -> p.toFile().delete());
    }

    @Test
    void shouldHandleNullFilesList() throws Exception {
        // files == null тохиолдлыг тестлэх (scanDirectory 37-р мөр)
        ClassPathScanner scanner = new ClassPathScanner("mn.edu.num.app");
        Method method = ClassPathScanner.class.getDeclaredMethod(
                "scanDirectory", File.class, String.class, List.class);
        method.setAccessible(true);

        File nonExistentDir = new File("/non/existent/path/that/does/not/exist");
        List<BeanDefinition> defs = new ArrayList<>();
        method.invoke(scanner, nonExistentDir, "test.pkg", defs);

        assertTrue(defs.isEmpty(), "Байхгүй хавтас дээр хоосон жагсаалт буцаах ёстой");
    }

    @Test
    void shouldReturnNullWhenDecapitalizeNull() throws Exception {
        // decapitalize(null) branch-ыг тестлэх (75-р мөр)
        ClassPathScanner scanner = new ClassPathScanner("mn.edu.num.app");
        Method method = ClassPathScanner.class.getDeclaredMethod("decapitalize", String.class);
        method.setAccessible(true);

        Object result = method.invoke(scanner, (String) null);
        assertNull(result, "null оролт дээр null буцаах ёстой");
    }

    @Test
    void shouldReturnEmptyWhenDecapitalizeEmpty() throws Exception {
        // decapitalize("") branch-ыг тестлэх (75-р мөр)
        ClassPathScanner scanner = new ClassPathScanner("mn.edu.num.app");
        Method method = ClassPathScanner.class.getDeclaredMethod("decapitalize", String.class);
        method.setAccessible(true);

        Object result = method.invoke(scanner, "");
        assertEquals("", result, "Хоосон текст дээр хоосон текст буцаах ёстой");
    }

    @Test
    void shouldIgnoreNonComponentClasses() throws Exception {
        // @Component аннотацигүй ердийн классыг алгасах branch-ыг тестлэх (processClass 53-р мөр)
        ClassPathScanner scanner = new ClassPathScanner("mn.edu.num.app");
        Method method = ClassPathScanner.class.getDeclaredMethod(
                "processClass", String.class, List.class);
        method.setAccessible(true);

        List<BeanDefinition> defs = new ArrayList<>();
        // BeanDefinition нь @Component аннотацигүй ердийн класс
        method.invoke(scanner, "mn.edu.num.container.BeanDefinition", defs);

        assertTrue(defs.isEmpty(), "@Component аннотацигүй класс бүртгэгдэх ёсгүй");
    }
}
