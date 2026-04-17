package mn.edu.num.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Идэвхтэй баригдах Bean-ийн хамрах хүрээг (үүсгэх давтамж буюу lifecycle) тодорхойлох аннотац юм.
 * Анхдагчаар (default) бүх бурдэж буй Bean нь "singleton" (нэг л удаа үүсэх) байна.
 * Мөн "prototype" (хандах бүрт шинээр үүсгэх) байдлаар тохируулах боломжтой.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Scope {
    /**
     * Хамрах хүрээний нэр буюу төрөл (жишээлбэл, "singleton" эсвэл "prototype").
     * @return Scope-ийн нэр
     */
    String value() default "singleton";
}
