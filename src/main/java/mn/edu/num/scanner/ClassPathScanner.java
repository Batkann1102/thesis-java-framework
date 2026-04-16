package mn.edu.num.scanner;

import mn.edu.num.annotation.Component;
import mn.edu.num.container.BeanDefinition;
import mn.edu.num.container.ScopeType;
import mn.edu.num.annotation.Scope;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ClassPathScanner {

    private final String basePackage;

    public ClassPathScanner(String basePackage) {
        this.basePackage = basePackage;
    }

    public List<BeanDefinition> scan() {
        List<BeanDefinition> definitions = new ArrayList<>();
        String path = basePackage.replace('.', '/');
        URL resource = Thread.currentThread()
                .getContextClassLoader()
                .getResource(path);

        if (resource == null) return definitions;

        File dir = new File(resource.getFile());
        scanDirectory(dir, basePackage, definitions);
        return definitions;
    }

    private void scanDirectory(File dir, String packageName,
                               List<BeanDefinition> definitions) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName(), definitions);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "."
                        + file.getName().replace(".class", "");
                processClass(className, definitions);
            }
        }
    }

    private void processClass(String className, List<BeanDefinition> definitions) {
        try {
            Class<?> clazz = Class.forName(className);
            if (clazz.isInterface() || !clazz.isAnnotationPresent(Component.class)) return;

            Component component = clazz.getAnnotation(Component.class);
            String beanName = component.value().isEmpty()
                    ? decapitalize(clazz.getSimpleName())
                    : component.value();

            ScopeType scope = ScopeType.SINGLETON;
            if (clazz.isAnnotationPresent(Scope.class)) {
                String scopeVal = clazz.getAnnotation(Scope.class).value();
                scope = "prototype".equalsIgnoreCase(scopeVal)
                        ? ScopeType.PROTOTYPE : ScopeType.SINGLETON;
            }

            definitions.add(new BeanDefinition(beanName, clazz, scope));
            System.out.println("[Scanner] Илэрлээ: " + beanName + " → " + clazz.getName());

        } catch (ClassNotFoundException e) {
            System.err.println("[Scanner] Класс ачаалахад алдаа: " + className);
        }
    }

    private String decapitalize(String name) {
        if (name == null || name.isEmpty()) return name;
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}