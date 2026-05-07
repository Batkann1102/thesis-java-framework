package mn.edu.num.container;

import mn.edu.num.annotation.EnableIoC;
import mn.edu.num.annotation.Scope;
import mn.edu.num.exception.BeanCreationException;
import mn.edu.num.exception.BeanScopeException;
import mn.edu.num.exception.CircularDependencyException;
import mn.edu.num.exception.NoSuchBeanException;
import mn.edu.num.scanner.ClassPathScanner;
import mn.edu.num.util.Levenshtein;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Энэхүү классыг өөрийн бүтээсэн IoC Framework-ийнхээ "зүрх" буюу үндсэн Container гэж ойлгож болно.
 * Энэ нь классуудыг хайж олох (scan), Bean үүсгэх, dependency injection (хамаарлууд) хийх,
 * мөн Scope-ийг удирдах бүх амьдралын мөчлөгийг (Lifecycle) чиглүүлэн ажиллана.
 *
 * <h3>Container lifecycle (амьдралын мөчлөг)</h3>
 * <ol>
 *   <li><b>SCANNING</b> — {@link ClassPathScanner} нь base package-аас бүх @Component
 *       классуудыг ачаалж {@link BeanDefinition} хэлбэрээр буцаана.</li>
 *   <li><b>REGISTRATION</b> — Definition-уудыг {@link BeanRegistry} дотор бүртгэнэ.
 *       Bean нэрийн давхардал, scope буруу утга энэ үед илрэх боломжтой.</li>
 *   <li><b>INSTANTIATION</b> — Singleton bean-уудыг урьдчилан үүсгэнэ
 *       (lazyInit=true үед энэ алхам алгасагдана).</li>
 *   <li><b>INJECTION</b> — {@link DependencyInjector} нь @Autowired field-уудад
 *       хамаарлуудыг бөглөнө. Reflection caching ашиглагдана.</li>
 *   <li><b>POST_CONSTRUCT</b> — @PostConstruct method-уудыг singleton bean
 *       тус бүрд бүртгэгдсэн дарааллаар дуудна.</li>
 *   <li><b>READY</b> — Container ашиглахад бэлэн.</li>
 *   <li><b>CLOSING (close())</b> — @PreDestroy method-уудыг бүртгэлийн ЭСРЭГ
 *       дарааллаар дуудаж, singleton кэшийг цэвэрлэнэ.</li>
 * </ol>
 *
 * <h3>Design decisions</h3>
 * <ul>
 *   <li><b>Singleton-ийг ӨМНӨ инжекцлэх</b>: circular dependency-г илрүүлэхийн
 *       тулд singleton-г кэш-д эхлээд хадгалаад дараа нь field inject хийнэ.
 *       Энэ нь partially-constructed bean-уудыг detect хийх боломжийг олгоно.</li>
 *   <li><b>LinkedHashMap-аар бүртгэл</b>: deterministic дарааллыг ApplicationContext-ийн
 *       гадна талд (test, benchmark, close()) нь шаардагддаг.</li>
 *   <li><b>ReflectionCache</b>: тус container-д singleton тул нэг класст хийсэн
 *       reflection ажилбар бусад dependency-уудтай хуваалцагдана.</li>
 * </ul>
 */
public class ApplicationContext implements DependencyInjector.ApplicationContextRef, AutoCloseable {

    private final BeanRegistry registry = new BeanRegistry();
    private final ReflectionCache reflectionCache = new ReflectionCache();
    private final DependencyInjector injector = new DependencyInjector(registry, reflectionCache);
    private final DependencyTreeBuilder treeBuilder;
    // Circular dependency илрүүлэх
    private final Set<String> inCreation = new LinkedHashSet<>();
    private final boolean lazyInit;
    private final boolean strictScope;
    private boolean closed = false;

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
        boolean lazyInit = false;
        boolean strictScope = false;

        if (primarySource.isAnnotationPresent(EnableIoC.class)) {
            EnableIoC config = primarySource.getAnnotation(EnableIoC.class);

            // Нэмэлт scan package зааж өгсөн бол тэдгээрийг ашиглана
            if (config.scanPackages().length > 0) {
                basePackage = config.scanPackages()[0]; // Эхний package-г root болгоно
            }

            // Exclude package-ууд
            excludePackages.addAll(Arrays.asList(config.excludePackages()));

            visualize = config.visualize();
            lazyInit = config.lazyInit();
            strictScope = config.strictScope();
        }


        System.out.println("[IoC] Primary source: " + primarySource.getName());

        ApplicationContext ctx = new ApplicationContext(basePackage, excludePackages, lazyInit, strictScope);

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
        this(detectCallerBasePackage(), List.of(), false, false);
    }

    /**
     * Тодорхой package-уудыг алгасах боломжтой constructor.
     * @param excludePackages алгасах package-ууд
     */
    public ApplicationContext(List<String> excludePackages) {
        this(detectCallerBasePackage(), excludePackages, false, false);
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
        this(basePackage, List.of(), false, false);
    }

    public ApplicationContext(String basePackage, List<String> excludePackages) {
        this(basePackage, excludePackages, false, false);
    }

    public ApplicationContext(String basePackage, List<String> excludePackages,
                              boolean lazyInit, boolean strictScope) {
        System.out.println("[Context] Эхэлж байна... package: " + basePackage);
        if (!excludePackages.isEmpty()) {
            System.out.println("[Context] Алгасах package-ууд: " + excludePackages);
        }
        this.lazyInit = lazyInit;
        this.strictScope = strictScope;

        ClassPathScanner scanner = new ClassPathScanner(basePackage, excludePackages);
        List<BeanDefinition> definitions = scanner.scan();

        // Бүртгэх (scope утгыг шалгах боломжтой)
        for (BeanDefinition def : definitions) {
            validateScope(def);
            registry.register(def);
        }

        // Singleton bean-уудыг урьдчилан үүсгэх (lazyInit=true үед алгасна)
        if (!lazyInit) {
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
        } else {
            System.out.println("[Context] lazyInit=true → singleton bean-уудыг шаардлагатай үед үүсгэнэ.");
        }
        // Dependency tree үүсгэж, хэвлэх
        treeBuilder = new DependencyTreeBuilder(registry);
        treeBuilder.buildTree();
        System.out.println(treeBuilder.printTree());
        System.out.println("[Context] Container бэлэн боллоо.");
    }

    /**
     * Bean class дээрх @Scope утгыг шалгана. strictScope=true тохиолдолд
     * тодорхойгүй утга бол {@link BeanScopeException} шиднэ.
     */
    private void validateScope(BeanDefinition def) {
        Scope scopeAnnotation = def.getBeanClass().getAnnotation(Scope.class);
        if (scopeAnnotation == null) return;
        String value = scopeAnnotation.value();
        if (!"singleton".equalsIgnoreCase(value) && !"prototype".equalsIgnoreCase(value)) {
            if (strictScope) {
                throw new BeanScopeException(def.getBeanName(), value);
            } else {
                System.err.println("[Context] WARNING: bean '" + def.getBeanName()
                        + "' дээрх @Scope('" + value + "') нь танигдахгүй; SINGLETON-ыг ашиглана.");
            }
        }
    }

    /**
     * Dependency tree builder-г буцаана.
     */
    public DependencyTreeBuilder getTreeBuilder() {
        return treeBuilder;
    }

    public ReflectionCache getReflectionCache() {
        return reflectionCache;
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isLazyInit() {
        return lazyInit;
    }

    public boolean isStrictScope() {
        return strictScope;
    }

    /** Бүртгэлтэй bean-уудын тоо. */
    public int beanCount() {
        return registry.getAllDefinitions().size();
    }

    @Override
    public Object getBean(String name) {
        ensureOpen();
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
        ensureOpen();
        List<BeanDefinition> candidates = registry.findByType(type);
        if (candidates.isEmpty()) {
            // Did-you-mean: бүртгэлтэй bean-уудын класс нэрсээс ойролцоогоор
            // санал болгох боломж байна. simpleName-ийг харьцуулна.
            List<String> simpleNames = registry.getAllDefinitions().stream()
                    .map(d -> d.getBeanClass().getSimpleName()).toList();
            List<String> suggestions = Levenshtein.suggest(type.getSimpleName(), simpleNames, 3);
            if (!suggestions.isEmpty()) {
                throw new NoSuchBeanException("Bean олдсонгүй: '" + type.getName()
                        + "'. Магадгүй та " + suggestions + " гэснийг хэлсэн үү? "
                        + "Бүртгэлтэй bean-үүд: " + registry.getAllDefinitions().stream()
                        .map(BeanDefinition::getBeanName).toList());
            }
            throw new NoSuchBeanException(type, registry.getAllDefinitions().stream()
                    .map(BeanDefinition::getBeanName).toList());
        }
        if (candidates.size() > 1) {
            throw new NoSuchBeanException("Олон bean тохирч байна: " + type.getName()
                    + ". Боломжит bean-үүд: " + candidates.stream().map(BeanDefinition::getBeanName).toList());
        }
        return type.cast(getBean(candidates.get(0).getBeanName()));
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("ApplicationContext хаагдсан. close() дуудагдсаны дараа bean авах боломжгүй.");
        }
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
            ReflectionCache.ClassMetadata metadata = reflectionCache.metadataFor(def.getBeanClass());
            Object instance = instantiate(def, metadata);

            // Singleton бол cache-д хадгална. Field inject-ээс ӨМНӨ хадгалснаар
            // циклийн харилцан хамаарал зөв илэрнэ (А→B→А үед А-г "in creation"
            // гэж тэмдэглэсэн хэвээр байна).
            if (def.getScope() == ScopeType.SINGLETON) {
                registry.cacheSingleton(name, instance);
            }

            injector.inject(instance, def, this);

            // @PostConstruct lifecycle hook-уудыг ажиллуулна
            invokeLifecycle(metadata.postConstructMethods(), instance, "@PostConstruct", name);
            return instance;

        } catch (CircularDependencyException e) {
            throw e;
        } catch (NoSuchBeanException e) {
            throw e;
        } catch (BeanCreationException e) {
            throw e;
        } catch (NoSuchMethodException e) {
            throw new BeanCreationException(name, "Default constructor олдсонгүй", e);
        } catch (Exception e) {
            throw new BeanCreationException(name, e.getMessage(), e);
        } finally {
            inCreation.remove(name);
        }
    }

    /**
     * Bean инстансыг (а) @Autowired constructor, эсвэл (б) default constructor-аар үүсгэнэ.
     * Аль алин нь олдохгүй бол {@link BeanCreationException} шиднэ.
     */
    private Object instantiate(BeanDefinition def, ReflectionCache.ClassMetadata metadata)
            throws Exception {
        if (metadata.autowiredConstructor() != null) {
            Constructor<?> ctor = metadata.autowiredConstructor();
            List<ReflectionCache.InjectableParameter> params = metadata.autowiredConstructorParameters();
            Object[] args = new Object[params.size()];
            for (int i = 0; i < params.size(); i++) {
                ReflectionCache.InjectableParameter p = params.get(i);
                if (p.beanName() != null) {
                    args[i] = getBean(p.beanName());
                } else {
                    args[i] = getBean(p.type());
                }
            }
            return ctor.newInstance(args);
        }
        if (metadata.defaultConstructor() != null) {
            return metadata.defaultConstructor().newInstance();
        }
        throw new BeanCreationException(def.getBeanName(),
                "Default constructor олдсонгүй. @Autowired constructor нэмэх эсвэл "
                        + "параметргүй constructor нэмнэ үү.",
                new NoSuchMethodException(def.getBeanClass().getName() + "()"));
    }

    private void invokeLifecycle(List<Method> methods, Object instance, String label, String beanName) {
        for (Method method : methods) {
            try {
                method.invoke(instance);
                System.out.println("[Lifecycle] " + label + " дуудлаа: "
                        + beanName + "." + method.getName());
            } catch (InvocationTargetException ite) {
                throw new BeanCreationException(beanName,
                        label + " method '" + method.getName() + "' алдаа гаргалаа",
                        ite.getTargetException());
            } catch (IllegalAccessException iae) {
                throw new BeanCreationException(beanName,
                        label + " method '" + method.getName() + "'-д нэвтрэх боломжгүй",
                        iae);
            }
        }
    }

    /**
     * Container-г аюулгүйгээр хааж дуусгаж @PreDestroy lifecycle method-уудыг
     * бүртгэлийн ЭСРЭГ дарааллаар дуудна. Хаагдсаны дараа getBean() дуудах нь
     * {@link IllegalStateException} шиднэ.
     *
     * <p>{@link AutoCloseable}-ийг implement хийсэн тул try-with-resources-тэй
     * хамт ашиглах боломжтой:
     * <pre>
     * try (ApplicationContext ctx = ApplicationContext.run(MyApp.class)) {
     *     // ... container ашиглах
     * }
     * </pre>
     */
    @Override
    public void close() {
        if (closed) return;
        System.out.println("[Context] close() — @PreDestroy дуудаж байна...");
        List<Map.Entry<String, Object>> entries =
                registry.getSingletonEntriesInRegistrationOrder();
        // Бүртгэлийн ЭСРЭГ дарааллаар: dependency-нхээ өмнө dependent нь хаагдана.
        ListIterator<Map.Entry<String, Object>> it = entries.listIterator(entries.size());
        while (it.hasPrevious()) {
            Map.Entry<String, Object> entry = it.previous();
            try {
                BeanDefinition def = registry.getDefinition(entry.getKey());
                ReflectionCache.ClassMetadata metadata =
                        reflectionCache.metadataFor(def.getBeanClass());
                invokeLifecycle(metadata.preDestroyMethods(), entry.getValue(),
                        "@PreDestroy", entry.getKey());
            } catch (Exception e) {
                System.err.println("[Context] @PreDestroy алдаа [" + entry.getKey()
                        + "]: " + e.getMessage());
            }
        }
        registry.clearSingletons();
        closed = true;
        System.out.println("[Context] Container хаагдлаа.");
    }
}
