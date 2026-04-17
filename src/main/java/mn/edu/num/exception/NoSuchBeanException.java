package mn.edu.num.exception;

import java.util.Collection;

/**
 * Контейнерээс (IoC Container) шаардлагатай Bean-ийг хайх үед тухайн төрлийн эсвэл
 * нэртэй Bean бүртгэлд (registry) олдсонгүй гэдгийг илэрхийлэх Exception.
 */
public class NoSuchBeanException extends RuntimeException {
    public NoSuchBeanException(String message) {
        super(message);
    }

    public NoSuchBeanException(String name, Collection<String> registeredBeans) {
        super("Bean олдсонгүй: '" + name + "'. Бүртгэлтэй bean-үүд: " + registeredBeans);
    }

    public NoSuchBeanException(Class<?> type, Collection<String> registeredBeans) {
        super("Bean олдсонгүй: '" + type.getName() + "'. Бүртгэлтэй bean-үүд: " + registeredBeans);
    }
}
