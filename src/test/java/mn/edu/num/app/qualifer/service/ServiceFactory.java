package mn.edu.num.app.qualifer.service;

import mn.edu.num.annotation.Autowired;
import mn.edu.num.annotation.Component;
import mn.edu.num.annotation.Qualifier;
import mn.edu.num.app.qualifer.port.UserRepository;

@Component
public class ServiceFactory {

    @Autowired
    @Qualifier("MongoRepository")
    private UserRepository userRepository;

    public String getRepoType(){
        return userRepository.getType();
    }
    public ServiceFactory(){}
}
