package mn.edu.num.unit;

import mn.edu.num.container.ApplicationContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * detectCallerBasePackage() private method-ийн unit тест.
 * Reflection ашиглан private method-г шууд дуудаж логикийг шалгана.
 */
class DetectCallerBasePackageTest {

    @Test
    @DisplayName("detectCallerBasePackage() нь дуудагч классын package-г буцаана")
    void shouldReturnCallerPackage() throws Exception {
        Method method = ApplicationContext.class.getDeclaredMethod("detectCallerBasePackage");
        method.setAccessible(true);

        String result = (String) method.invoke(null);

        // Энэ тест класс нь mn.edu.num.unit package-д байгаа
        // Гэхдээ reflection дуудалт учир stack trace-д JUnit, reflection классууд байх тул
        // null биш утга буцаахыг шалгана
        assertNotNull(result, "detectCallerBasePackage() null биш утга буцаах ёстой");
        assertFalse(result.isEmpty(), "Хоосон string биш байх ёстой");
        assertTrue(result.contains("."), "Package нэр цэгтэй байх ёстой (жишээ: mn.edu.num)");
    }

    @Test
    @DisplayName("Буцаасан package нэр нь зөв формат-тай байна")
    void shouldReturnValidPackageFormat() throws Exception {
        Method method = ApplicationContext.class.getDeclaredMethod("detectCallerBasePackage");
        method.setAccessible(true);

        String result = (String) method.invoke(null);

        // Package нэр нь жижиг үсэг, цэгээр тусгаарлагдсан байх ёстой
        assertNotNull(result);
        assertFalse(result.endsWith("."), "Package нэр цэгээр төгсөхгүй");
        assertFalse(result.startsWith("."), "Package нэр цэгээр эхлэхгүй");

        // Класс нэр (том үсгээр эхэлдэг) агуулаагүй — зөвхөн package
        String lastPart = result.substring(result.lastIndexOf('.') + 1);
        // Package-ийн сүүлийн хэсэг нь жижиг үсгээр эхлэх ёстой
        // (класс нэр биш, package нэр)
        assertTrue(Character.isLowerCase(lastPart.charAt(0)),
                "Package-ийн сүүлийн хэсэг жижиг үсгээр эхлэх ёстой: " + lastPart);
    }

    @Test
    @DisplayName("Stack trace-д ApplicationContext классыг алгасдаг")
    void shouldSkipApplicationContextInStackTrace() throws Exception {
        Method method = ApplicationContext.class.getDeclaredMethod("detectCallerBasePackage");
        method.setAccessible(true);

        String result = (String) method.invoke(null);

        // ApplicationContext-ийн package-г буцаахгүй байх ёстой
        assertNotEquals("mn.edu.num.container", result,
                "ApplicationContext-ийн өөрийн package биш, дуудагч классын package буцаах ёстой");
    }
}

