package mn.edu.num.app.broken;
import mn.edu.num.annotation.Component;

@Component
public class ThrowingConstructor {
    public ThrowingConstructor() {
        throw new RuntimeException("Зориуд гаргасан алдаа");
    }
}
