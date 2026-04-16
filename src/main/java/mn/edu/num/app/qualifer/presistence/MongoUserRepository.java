package mn.edu.num.app.qualifer.presistence;

import mn.edu.num.annotation.Component;
import mn.edu.num.app.qualifer.port.UserRepository;

@Component("MongoRepository")
public class MongoUserRepository implements UserRepository {

    @Override
    public String getType() {
        return "MongoDB Repository";
    }
    public MongoUserRepository() {}
}
