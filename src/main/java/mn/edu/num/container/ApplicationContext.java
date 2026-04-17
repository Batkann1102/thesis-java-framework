package mn.edu.num.container;

import mn.edu.num.annotation.EnableIoC;
import mn.edu.num.exception.BeanCreationException;
import mn.edu.num.exception.CircularDependencyException;
import mn.edu.num.exception.NoSuchBeanException;
import mn.edu.num.scanner.ClassPathScanner;

import java.util.*;

/**
 * Энэхүү классыг өөрийн бүтээсэн IoC Framework-ийнхээ "зүрх" буюу үндсэн Container гэж ойлгож болно.
 * Энэ нь классуудыг хайж олох (scan), Bean үүсгэх, dependency injection (хамаарлууд) хийх,
 * мөн Scope-ийг удирдах бүх амьдралын мөчлөгийг (Lifecycle) чиглүүлэн ажиллана.
 */
public class ApplicationContext implements DependencyInjector.ApplicationContextRef {

    private final BeanRegistry registry = new BeanRegistry();
    private final DependencyInjector injector = new DependencyInjector(registry);
    private final DependencyTreeBuilder treeBuilder;
    // Circular dependency илрүүлэх
    private final Set<String> inCreation = new HashSet<>();

    // ──────────────────────────────────────────────
    //  Spring Boot-ийн загвартай адил static run() method
    // ──────────────────────────────────────────────

    /**
     * Spring Boot-ийн SpringApplication.run()-тай адил.
     * @EnableIoC annotation-тай классыг дамжуулж container-г эхлүүлнэ.
     *
     * <pre>
     * &#64;EnableIoC
     * public class MyApp {
     *     public static void main(String[] args) {
     *         ApplicationContext ctx = ApplicationContext.run(MyApp.class);
     *     }
     * }
     * </pre>
     *
     * @param primarySource @EnableIoC annotation-тай main класс
     * @return бэлэн ApplicationContext
     */
    public static ApplicationContext run(Class<?> primarySource) {
        // 1. @EnableIoC annotation-аас тохиргоог унших
        String basePackage = primarySource.getPackage().getName();
        List<String> excludePackages = new ArrayList<>();
        boolean visualize = false;

        if (primarySource.isAnnotationPresent(EnableIoC.class)) {
            EnableIoC config = primarySource.getAnnotation(EnableIoC.class);

            // Нэмэлт scan package зааж өгсөн бол тэдгээрийг ашиглана
            if (config.scanPackages().length > 0) {
                basePackage = config.scanPackages()[0]; // Эхний package-г root болгоно
            }

            // Exclude package-ууд
            excludePackages.addAll(Arrays.asList(config.excludePackages()));

            // Visualize тохиргоо
            visualize = config.visualize();
        }


        System.out.println("[IoC] Primary source: " + primarySource.getName());

        ApplicationContext ctx = new ApplicationContext(basePackage, excludePackages);

        // @EnableIoC(visualize = true) бол HTML файл үүсгэж browser нээнэ
        if (visualize) {
            DependencyTreeHtmlExporter.generateAndOpen(ctx.getTreeBuilder().getNodeMap());
        }

        return ctx;
    }

    /**
     * Дуудагч классын package-аас автоматаар root package олж scan хийнэ.
     * Ямар ч төсөлд new ApplicationContext() гэж дуудахад л хангалттай.
     */
    public ApplicationContext() {
        this(detectCallerBasePackage(), List.of());
    }

    /**
     * Тодорхой package-уудыг алгасах боломжтой constructor.
     * @param excludePackages алгасах package-ууд
     */
    public ApplicationContext(List<String> excludePackages) {
        this(detectCallerBasePackage(), excludePackages);
    }

    /**
     * Дуудагч классын stack trace-аас root package автоматаар олно.
     * Жишээ: mn.edu.num.Main → "mn.edu.num" гэсэн root package буцаана.
     */
    private static String detectCallerBasePackage() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        String callerClass = null;
        // ApplicationContext-ийн constructor-аас гадна дуудагчийг олно
        for (int i = 1; i < stack.length; i++) {
            if (!stack[i].getClassName().equals(ApplicationContext.class.getName())) {
                callerClass = stack[i].getClassName();
                break;
            }
        }
        if (callerClass == null) callerClass = stack[stack.length - 1].getClassName();
        int lastDot = callerClass.lastIndexOf('.');
        if (lastDot > 0) {
            return callerClass.substring(0, lastDot);
        }
        return callerClass;
    }

    public ApplicationContext(String basePackage) {
        this(basePackage, List.of());
    }

    public ApplicationContext(String basePackage, List<String> excludePackages) {
        System.out.println("[Context] Эхэлж байна... package: " + basePackage);
        if (!excludePackages.isEmpty()) {
            System.out.println("[Context] Алгасах package-ууд: " + excludePackages);
        }
        ClassPathScanner scanner = new ClassPathScanner(basePackage, excludePackages);
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
        // Dependency tree үүсгэж, хэвлэх
        treeBuilder = new DependencyTreeBuilder(registry);
        treeBuilder.buildTree();
        System.out.println(treeBuilder.printTree());
        System.out.println("[Context] Container бэлэн боллоо.");
    }

    /**
     * Dependency tree builder-г буцаана.
     */
    public DependencyTreeBuilder getTreeBuilder() {
        return treeBuilder;
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
