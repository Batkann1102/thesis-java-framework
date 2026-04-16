package mn.edu.num.app.qualifer.presistence;

import mn.edu.num.annotation.Component;
import mn.edu.num.app.qualifer.port.UserRepository;

@Component("JDBCRepository")
public class JDBCMySQLRepository implements UserRepository {

    @Override
    public String getType() {
        return "JDBC Repository";
    }
    public JDBCMySQLRepository() {}
}
