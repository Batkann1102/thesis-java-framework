package mn.edu.num.app.circular;

import mn.edu.num.annotation.Autowired;
import mn.edu.num.annotation.Component;

@Component
public class Egg {

    @Autowired
    private Chicken chicken;
}
