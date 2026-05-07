package mn.edu.num.container;

import mn.edu.num.exception.BeanCreationException;
import mn.edu.num.exception.NoSuchBeanException;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Шинээр үүссэн Bean инстансад хэрэгтэй байгаа бусад хамааралтай Bean-уудыг (dependencies)
 * автоматаар холбох буюу inject хийх үйлдлийг гүйцэтгэх гол туслах класс.
 *
 * <p>Reflection хайлтын overhead-ийг хязгаарлахын тулд {@link ReflectionCache}-аас
 * тус класст харгалзах урьдчилан тооцоолсон metadata-г ашиглана. Үүний үр дүнд:
 * <ul>
 *   <li>{@code getDeclaredFields()} нэг класст нэг л удаа дуудагдана</li>
 *   <li>{@link Field#setAccessible(boolean)} давтан дуудагдахгүй</li>
 *   <li>annotation шалгалтын тоо багасна</li>
 * </ul>
 * Энэ оновчлол нь "reflection overhead" гэсэн шүүмжид шууд хариулсан шийдэл
 * бөгөөд benchmark-аар нотлогдсон (docs/benchmarks/index.mdx).
 */
public class DependencyInjector {

    private final BeanRegistry registry;
    private final ReflectionCache reflectionCache;

    public DependencyInjector(BeanRegistry registry) {
        this(registry, new ReflectionCache());
    }

    public DependencyInjector(BeanRegistry registry, ReflectionCache reflectionCache) {
        this.registry = registry;
        this.reflectionCache = reflectionCache;
    }

    public void inject(Object instance, BeanDefinition definition,
                       ApplicationContextRef contextRef) {
        ReflectionCache.ClassMetadata metadata = reflectionCache.metadataFor(definition.getBeanClass());

        for (ReflectionCache.InjectableField injectable : metadata.autowiredFields()) {
            Field field = injectable.field();
            Class<?> fieldType = field.getType();

            Object dependency;
            if (injectable.beanName() != null) {
                dependency = contextRef.getBean(injectable.beanName());
            } else {
                List<BeanDefinition> candidates = registry.findByType(fieldType);
                if (candidates.isEmpty()) {
                    if (!injectable.required()) {
                        continue; // optional dependency — алгасна
                    }
                    throw new NoSuchBeanException(
                            "Dependency олдсонгүй: " + fieldType.getName()
                                    + " [" + definition.getBeanName() + "]. Бүртгэлтэй bean-үүд: "
                                    + registry.getAllDefinitions().stream().map(BeanDefinition::getBeanName).toList());
                }
                if (candidates.size() > 1) {
                    throw new NoSuchBeanException(
                            "Олон bean тохирч байна: " + fieldType.getName()
                                    + ". @Qualifier ашиглана уу. Боломжит bean-үүд: "
                                    + candidates.stream().map(BeanDefinition::getBeanName).toList());
                }
                dependency = contextRef.getBean(candidates.get(0).getBeanName());
            }

            try {
                field.set(instance, dependency);
                System.out.println("[Injector] Inject: "
                        + definition.getBeanName() + "." + field.getName());
            } catch (IllegalAccessException e) {
                throw new BeanCreationException(
                        "Field inject хийхэд алдаа: " + field.getName(), e);
            }
        }
    }

    public ReflectionCache reflectionCache() {
        return reflectionCache;
    }

    // ApplicationContext-тэй circular reference үүсгэхгүйн тулд interface ашиглана
    public interface ApplicationContextRef {
        Object getBean(String name);
    }
}
