package mn.edu.num.exception;

import java.util.Collection;

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
