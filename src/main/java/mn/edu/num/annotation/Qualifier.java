package mn.edu.num.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Нэг төрлийн хэдэн олон Bean байх тохиолдолд, чухам аль Bean-г татаж авахыг (inject) зааж өгөх зориулалттай аннотац.
 * Dependency Injection хийж байх үед @Autowired-тай хамт эсвэл параметр дээр байрлуулж ашиглана.
 */
@Target({ElementType.PARAMETER,  ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Qualifier {
    /**
     * Татаж авах гэж буй Bean-ийн өвөрмөц нэр (Id)-г энд зааж өгнө.
     * @return Заавал заах ёстой Bean-ийн нэр
     */
    String value();
}
