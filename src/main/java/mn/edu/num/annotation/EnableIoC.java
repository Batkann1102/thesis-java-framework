package mn.edu.num.annotation;

import java.lang.annotation.*;

/**
 * Төслийн эхлэл цэгийг тэмдэглэх annotation.
 * Spring Boot-ийн @SpringBootApplication-тай адил үүрэгтэй.
 *
 * Энэ annotation-г тавьсан классын package-аас эхлэн
 * бүх дэд package-уудыг scan хийж @Component bean-уудыг олно.
 *
 * Хэрэглээ:
 * <pre>
 * &#64;EnableIoC
 * public class MyApp {
 *     public static void main(String[] args) {
 *         ApplicationContext ctx = ApplicationContext.run(MyApp.class);
 *     }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableIoC {
    /**
     * Scan хийх нэмэлт package-ууд.
     * Хоосон байвал зөвхөн энэ классын package-аас scan хийнэ.
     */
    String[] scanPackages() default {};

    /**
     * Алгасах package-ууд.
     */
    String[] excludePackages() default {};

    /**
     * true бол dependency tree-г HTML файлаар үүсгэж browser-д автоматаар нээнэ.
     */
    boolean visualize() default false;
}

