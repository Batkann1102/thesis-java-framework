package mn.edu.num.annotation;

import java.lang.annotation.*;

/**
 * Энэхүү annotation-ээр тэмдэглэсэн классуудыг IoC framework автоматаар (scan) олж илрүүлэн,
 * Container дотор Bean болгон бүртгэж удирдах болно.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Component {
    /**
     * Бүртгүүлэх Bean-ийн өвөрмөц нэрийг (Id) тодорхойлно.
     * Хэрэв хоосон үлдээвэл, тухайн классын нэрний эхний үсгийг жижигсгэсэн (camelCase) нэрийг анхдагчаар ашиглана.
     * @return Bean-ийн нэр
     */
    String value() default "";
}
