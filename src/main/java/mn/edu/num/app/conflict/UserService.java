package mn.edu.num.app.conflict;

import mn.edu.num.annotation.Autowired;
import mn.edu.num.annotation.Component;

@Component
public class UserService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private OrderService orderService ;

    public String register() {
        return emailService.send();
    }

    public String orderRegister() {
        return orderService.getOrder();
    }
}