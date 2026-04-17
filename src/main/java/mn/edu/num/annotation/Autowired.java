package mn.edu.num.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Object-ийн хамаарлыг (dependency injection) автоматаар холбоход ашиглах annotation.
 * Энэхүү annotation-ийг талбар (field) эсвэл байгуулагч (constructor) дээр байрлуулж ашиглана.
 * IoC container нь уг annotation-той талбарт тохирох (уг төрлийн) Bean-ийг автоматаар хайж олж олгодог.
 */
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired {
    /**
     * Хамаарал заавал байх шаардлагатай эсэхийг заана.
     * Хэрэв true бол, тохирох Bean олдохгүй үед алдаа (Exception) заана.
     * @return заавал шаардах эсэх
     */
    boolean required() default true;
}
