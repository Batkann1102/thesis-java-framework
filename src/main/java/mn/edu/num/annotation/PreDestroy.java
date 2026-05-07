package mn.edu.num.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bean-ийг устгах буюу container хаагдахын өмнө дуудах lifecycle method-ийг
 * тэмдэглэх annotation.
 * <p>
 * {@link mn.edu.num.container.ApplicationContext#close()} дуудагдсан үед
 * singleton bean-ууд бүртгэгдсэн дарааллын урвуу дарааллаар (хэн хамгийн
 * сүүлд бүртгэгдсэн нь хамгийн түрүүнд) @PreDestroy method-уудаа ажиллуулна.
 * Энэ нь dependency-уудыг өөрсдөөс нь өмнө хааж дуусгахгүй байх семантикийг
 * хангана.
 * <p>
 * Зөвхөн параметргүй method дээр тавина.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PreDestroy {
}
