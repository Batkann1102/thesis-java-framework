package mn.edu.num.annotation;

import java.lang.annotation.*;

/**
 * Framework-ийг асаах эсвэл эхлүүлэх үйл явцыг удирдах үндсэн annotation.
 * Spring Boot-ийн @SpringBootApplication-тэй төстэй зориулалттай.
 *
 * Энэхүү annotation-г ашигласан классын орших пакет болон түүний
 * дотоод пакетуудаар гүйж (scan хийж) @Component-тэй классуудыг бүртгэнэ.
 *
 * Хэрэглэх жишээ:
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
     * Scan хийхийг хүссэн нэмэлт пакетууд.
     * Энэхүү утгыг зааж өгвөл тэдгээр пакетуудаар нэмэлт хайлт хийнэ.
     */
    String[] scanPackages() default {};

    /**
     * Хайлт (scan) хийхдээ алгасах буюу оруулахгүй байх пакетууд.
     */
    String[] excludePackages() default {};

    /**
     * true үед dependency tree-ийг HTML хэлбэрээр хэвлэж хадгална.
     */
    boolean visualize() default false;
}

