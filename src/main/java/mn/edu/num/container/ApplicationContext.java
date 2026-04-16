package mn.edu.num.container;

import mn.edu.num.exception.BeanCreationException;
import mn.edu.num.exception.CircularDependencyException;
import mn.edu.num.exception.NoSuchBeanException;
import mn.edu.num.scanner.ClassPathScanner;

import java.util.*;

public class ApplicationContext implements DependencyInjector.ApplicationContextRef {

    private final BeanRegistry registry = new BeanRegistry();
    private final DependencyInjector injector = new DependencyInjector(registry);
    // Circular dependency илрүүлэх
    private final Set<String> inCreation = new HashSet<>();

    public ApplicationContext(String basePackage) {
        System.out.println("[Context] Эхэлж байна... package: " + basePackage);
        ClassPathScanner scanner = new ClassPathScanner(basePackage);
        List<BeanDefinition> definitions = scanner.scan();

        // Бүртгэх
        for (BeanDefinition def : definitions) {
            registry.register(def);
        }

        // Singleton bean-уудыг урьдчилан үүсгэх
        for (BeanDefinition def : definitions) {
            if (def.getScope() == ScopeType.SINGLETON) {
                try {
                    getBean(def.getBeanName());
                } catch (Exception e) {
                    System.err.println("[Context] Singleton bean үүсгэхэд алдаа [" + def.getBeanName() + "]: " + e.getMessage());
                    throw e;
                }
            }
        }
        System.out.println("[Context] Container бэлэн боллоо.");
    }

    @Override
    public Object getBean(String name) {
        BeanDefinition def = registry.getDefinition(name);

        // Кэш шалгахаас ӨМНӨ тойрог хамаарал шалгана
        if (inCreation.contains(name)) {
            throw new CircularDependencyException(
                    "Тойрог хамаарал илэрлээ: " + name
                            + " | Үүсгэгдэж байгаа: " + inCreation);
        }
        if (def.getScope() == ScopeType.SINGLETON) {
            if (registry.hasSingleton(name)) {
                return registry.getSingleton(name);
            }
            return createBean(def);
        } else {
            // Prototype — шинэ instance үүсгэнэ
            return createBean(def);
        }
    }

    public <T> T getBean(Class<T> type) {
        List<BeanDefinition> candidates = registry.findByType(type);
        if (candidates.isEmpty()) {
            throw new NoSuchBeanException(type, registry.getAllDefinitions().stream()
                    .map(BeanDefinition::getBeanName).toList());
        }
        if (candidates.size() > 1) {
            throw new NoSuchBeanException("Олон bean тохирч байна: " + type.getName()
                    + ". Боломжит bean-үүд: " + candidates.stream().map(BeanDefinition::getBeanName).toList());
        }
        return type.cast(getBean(candidates.get(0).getBeanName()));
    }

    private Object createBean(BeanDefinition def) {
        String name = def.getBeanName();

        // Circular dependency шалгах
        if (inCreation.contains(name)) {
            throw new CircularDependencyException(
                    "Тойрог хамаарал илэрлээ: " + name);
        }
        inCreation.add(name);

        try {
            Object instance = def.getBeanClass()
                    .getDeclaredConstructor()
                    .newInstance();

            // Dependency inject хийнэ (Singleton кэш-д орохоос ӨМНӨ field inject хийвэл circular dependency-д асуудал үүснэ)
            // Гэхдээ одоогийн хэрэгжүүлэлтээр singleton кэш-д ЭХЭЛЖ хийж байна.
            
            // Singleton бол cache-д хадгална
            if (def.getScope() == ScopeType.SINGLETON) {
                registry.cacheSingleton(name, instance);
            }
            
            injector.inject(instance, def, this);
            return instance;

        } catch (CircularDependencyException e) {
            throw e;
        } catch (NoSuchMethodException e) {
            throw new BeanCreationException(name, "Default constructor олдсонгүй", e);
        } catch (Exception e) {
            throw new BeanCreationException(name, e.getMessage(), e);
        } finally {
            inCreation.remove(name);
        }
    }
}
