package mn.edu.num.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bean-ийг constructor + dependency injection хийсний дараа нэг удаа дуудах
 * lifecycle method-ийг тэмдэглэх annotation.
 * <p>
 * Жишээ хэрэглээ:
 * <pre>
 * &#64;Component
 * public class CacheWarmer {
 *     &#64;PostConstruct
 *     void warmUp() {
 *         // resource бэлдэх, кэш урьдчилан дүүргэх
 *     }
 * }
 * </pre>
 * <p>
 * Зөвхөн параметргүй, void буцаалттай method дээр тавина. Singleton bean-уудад
 * ApplicationContext эхлэхэд автоматаар дуудагдана.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PostConstruct {
}
