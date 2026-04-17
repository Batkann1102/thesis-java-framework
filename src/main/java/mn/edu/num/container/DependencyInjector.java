package mn.edu.num.container;

import mn.edu.num.annotation.Autowired;
import mn.edu.num.annotation.Qualifier;
import mn.edu.num.exception.BeanCreationException;
import mn.edu.num.exception.NoSuchBeanException;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Шинээр үүссэн Bean инстансад хэрэгтэй байгаа бусад хамааралтай Bean-уудыг (dependencies)
 * автоматаар холбох буюу inject хийх үйлдлийг гүйцэтгэх гол туслах класс.
 * @Autowired, @Qualifier зэрэг annotation-үүдийн дагуу талбаруудыг reflection ашиглан шалгаж
 * регистрээс зэргэлдээ Bean-уудыг татаж авчирдаг.
 */
public class DependencyInjector {

    private final BeanRegistry registry;

    public DependencyInjector(BeanRegistry registry) {
        this.registry = registry;
    }

    public void inject(Object instance, BeanDefinition definition,
                       ApplicationContextRef contextRef) {
        for (Field field : definition.getBeanClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(Autowired.class)) continue;

            Class<?> fieldType = field.getType();
            String beanName = null;

            // @Qualifier байвал нэрээр хайна
            if (field.isAnnotationPresent(Qualifier.class)) {
                beanName = field.getAnnotation(Qualifier.class).value();
            }

            Object dependency;
            if (beanName != null) {
                dependency = contextRef.getBean(beanName);
            } else {
                // Төрлөөр хайна
                List<BeanDefinition> candidates = registry.findByType(fieldType);
                if (candidates.isEmpty()) {
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
                field.setAccessible(true);
                field.set(instance, dependency);
                System.out.println("[Injector] Inject: "
                        + definition.getBeanName() + "." + field.getName());
            } catch (IllegalAccessException e) {
                throw new BeanCreationException(
                        "Field inject хийхэд алдаа: " + field.getName(), e);
            }
        }
    }

    // ApplicationContext-тэй circular reference үүсгэхгүйн тулд interface ашиглана
    public interface ApplicationContextRef {
        Object getBean(String name);
    }
}