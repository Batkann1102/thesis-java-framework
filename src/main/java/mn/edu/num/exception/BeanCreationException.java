package mn.edu.num.exception;

public class BeanCreationException extends RuntimeException {
    public BeanCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanCreationException(String beanName, String message, Throwable cause) {
        super("Bean үүсгэхэд алдаа [" + beanName + "]: " + message, cause);
    }
}
