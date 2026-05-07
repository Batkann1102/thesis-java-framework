package mn.edu.num.exception;

/**
 * Тодорхойгүй (хүчингүй) scope утга өгсөн үед шиддэг exception.
 * <p>
 * Жишээ нь {@code @Scope("foo")} гэх мэт singleton/prototype биш утга өгсөн
 * тохиолдолд дуудагч нь юу тохируулсан болохоо тодорхой мэдэх боломжтой
 * болж DX сайжруулахад туслана. Анхдагч (default) тохируулгад framework
 * silently singleton-руу буулгадаг боловч {@code strictScope = true} үед
 * энэхүү exception шидэгдэнэ.
 */
public class BeanScopeException extends RuntimeException {
    public BeanScopeException(String beanName, String invalidScope) {
        super("Bean '" + beanName + "' дээрх @Scope утга алдаатай байна: '"
                + invalidScope + "'. Зөвшөөрөгдөх утгууд: singleton, prototype.");
    }
}
