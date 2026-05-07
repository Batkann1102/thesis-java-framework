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

    /**
     * true үед singleton bean-уудыг ApplicationContext эхлэх үед урьдчилан үүсгэхгүй,
     * харин эхний {@code getBean()} дуудалтад "хойшлуулсан" (lazy) хэлбэрээр үүсгэнэ.
     * Энэ нь том суурийн хэмжээнд startup latency-г бууруулна; гэхдээ алдаа
     * хожуу илрэх магадлалтай тул production-д сонголттой ашиглагдана.
     */
    boolean lazyInit() default false;

    /**
     * true үед {@link Scope} утгыг хатуу шалгана. Тодорхойгүй scope утга
     * (singleton/prototype биш) тохиолдолд {@code BeanScopeException} шиднэ.
     * false (default) үед framework нь silently SINGLETON руу буулгана —
     * хуучин кодтой нийцтэй байна.
     */
    boolean strictScope() default false;
}

