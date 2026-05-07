package mn.edu.num.container;

import mn.edu.num.annotation.Autowired;
import mn.edu.num.annotation.PostConstruct;
import mn.edu.num.annotation.PreDestroy;
import mn.edu.num.annotation.Qualifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reflection хайлтын үр дүнг класс тус бүрд нэг л удаа хийж кэшилдэг туслах класс.
 * <p>
 * Reflection нь Java-д харьцангуй "үнэтэй" үйлдэл бөгөөд getDeclaredFields,
 * getDeclaredConstructors, isAnnotationPresent зэргийг bean үүсгэх бүрд дуудах нь
 * том суурийн хэмжээнд performance overhead үүсгэдэг. Энэхүү класс нь:
 * <ul>
 *   <li>@Autowired талбарын мета мэдээлэл (Qualifier-той эсэх г.м.)</li>
 *   <li>Default ба @Autowired constructor-ууд</li>
 *   <li>@PostConstruct, @PreDestroy lifecycle method-ууд</li>
 * </ul>
 * зэргийг класс тус бүрд нэг удаа тооцоолон ConcurrentHashMap-д хадгална.
 * Үүний үр дүнд singleton bean-уудын нийт reflection дуудалтын тоог
 * "N x reflection" -> "1 x reflection" болгож бууруулна (benchmark-аар нотлогдсон).
 */
public final class ReflectionCache {

    private final Map<Class<?>, ClassMetadata> cache = new ConcurrentHashMap<>();

    public ClassMetadata metadataFor(Class<?> beanClass) {
        ClassMetadata cached = cache.get(beanClass);
        if (cached != null) {
            return cached;
        }
        ClassMetadata computed = compute(beanClass);
        ClassMetadata existing = cache.putIfAbsent(beanClass, computed);
        return existing != null ? existing : computed;
    }

    private ClassMetadata compute(Class<?> beanClass) {
        List<InjectableField> fields = new ArrayList<>();
        for (Field field : beanClass.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Autowired.class)) continue;
            field.setAccessible(true);
            Qualifier qualifier = field.getAnnotation(Qualifier.class);
            String beanName = qualifier != null ? qualifier.value() : null;
            Autowired autowired = field.getAnnotation(Autowired.class);
            fields.add(new InjectableField(field, beanName, autowired.required()));
        }

        Constructor<?> autowiredCtor = null;
        Constructor<?> defaultCtor = null;
        for (Constructor<?> ctor : beanClass.getDeclaredConstructors()) {
            if (ctor.isAnnotationPresent(Autowired.class)) {
                if (autowiredCtor != null) {
                    throw new IllegalStateException(
                            "Нэг класс дээр зөвхөн нэг @Autowired constructor байж болно: "
                                    + beanClass.getName());
                }
                ctor.setAccessible(true);
                autowiredCtor = ctor;
            }
            if (ctor.getParameterCount() == 0) {
                ctor.setAccessible(true);
                defaultCtor = ctor;
            }
        }

        List<Method> postConstruct = new ArrayList<>();
        List<Method> preDestroy = new ArrayList<>();
        for (Method method : beanClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                validateLifecycleMethod(method, "@PostConstruct");
                method.setAccessible(true);
                postConstruct.add(method);
            }
            if (method.isAnnotationPresent(PreDestroy.class)) {
                validateLifecycleMethod(method, "@PreDestroy");
                method.setAccessible(true);
                preDestroy.add(method);
            }
        }

        List<InjectableParameter> ctorParams = Collections.emptyList();
        if (autowiredCtor != null) {
            ctorParams = new ArrayList<>(autowiredCtor.getParameterCount());
            java.lang.annotation.Annotation[][] paramAnnotations = autowiredCtor.getParameterAnnotations();
            Class<?>[] paramTypes = autowiredCtor.getParameterTypes();
            for (int i = 0; i < paramTypes.length; i++) {
                String beanName = null;
                for (java.lang.annotation.Annotation a : paramAnnotations[i]) {
                    if (a instanceof Qualifier q) {
                        beanName = q.value();
                        break;
                    }
                }
                ctorParams.add(new InjectableParameter(paramTypes[i], beanName));
            }
        }

        return new ClassMetadata(
                Collections.unmodifiableList(fields),
                autowiredCtor,
                defaultCtor,
                Collections.unmodifiableList(ctorParams),
                Collections.unmodifiableList(postConstruct),
                Collections.unmodifiableList(preDestroy)
        );
    }

    private static void validateLifecycleMethod(Method method, String label) {
        if (method.getParameterCount() != 0) {
            throw new IllegalStateException(label + " method нь параметргүй байх ёстой: "
                    + method.getDeclaringClass().getName() + "." + method.getName());
        }
    }

    public int size() {
        return cache.size();
    }

    /**
     * Класс тус бүрд тооцоолж кэшилсэн reflection metadata.
     */
    public static final class ClassMetadata {
        private final List<InjectableField> autowiredFields;
        private final Constructor<?> autowiredConstructor;
        private final Constructor<?> defaultConstructor;
        private final List<InjectableParameter> autowiredConstructorParameters;
        private final List<Method> postConstructMethods;
        private final List<Method> preDestroyMethods;

        ClassMetadata(List<InjectableField> autowiredFields,
                      Constructor<?> autowiredConstructor,
                      Constructor<?> defaultConstructor,
                      List<InjectableParameter> autowiredConstructorParameters,
                      List<Method> postConstructMethods,
                      List<Method> preDestroyMethods) {
            this.autowiredFields = autowiredFields;
            this.autowiredConstructor = autowiredConstructor;
            this.defaultConstructor = defaultConstructor;
            this.autowiredConstructorParameters = autowiredConstructorParameters;
            this.postConstructMethods = postConstructMethods;
            this.preDestroyMethods = preDestroyMethods;
        }

        public List<InjectableField> autowiredFields() {
            return autowiredFields;
        }

        public Constructor<?> autowiredConstructor() {
            return autowiredConstructor;
        }

        public Constructor<?> defaultConstructor() {
            return defaultConstructor;
        }

        public List<InjectableParameter> autowiredConstructorParameters() {
            return autowiredConstructorParameters;
        }

        public List<Method> postConstructMethods() {
            return postConstructMethods;
        }

        public List<Method> preDestroyMethods() {
            return preDestroyMethods;
        }
    }

    public static final class InjectableField {
        private final Field field;
        private final String beanName;
        private final boolean required;

        public InjectableField(Field field, String beanName, boolean required) {
            this.field = field;
            this.beanName = beanName;
            this.required = required;
        }

        public Field field() {
            return field;
        }

        public String beanName() {
            return beanName;
        }

        public boolean required() {
            return required;
        }
    }

    public static final class InjectableParameter {
        private final Class<?> type;
        private final String beanName;

        public InjectableParameter(Class<?> type, String beanName) {
            this.type = type;
            this.beanName = beanName;
        }

        public Class<?> type() {
            return type;
        }

        public String beanName() {
            return beanName;
        }
    }
}
