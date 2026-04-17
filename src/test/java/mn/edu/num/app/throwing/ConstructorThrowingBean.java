package mn.edu.num.app.throwing;

import mn.edu.num.annotation.Component;

@Component
public class ConstructorThrowingBean {
    public ConstructorThrowingBean() {
        throw new IllegalStateException("Constructor дотор алдаа гарлаа");
    }
}
