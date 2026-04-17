package mn.edu.num.exception;

/**
 * Bean-ийн инстансыг үүсгэх үед үүсэх алдааг (Exception) илэрхийлэх класс.
 * Жишээ нь, классын constructor нь default биш эсвэл дотроо алдаа шидсэн тохиолдолд шидэгдэнэ.
 */
public class BeanCreationException extends RuntimeException {
    public BeanCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanCreationException(String beanName, String message, Throwable cause) {
        super("Bean үүсгэхэд алдаа [" + beanName + "]: " + message, cause);
    }
}
