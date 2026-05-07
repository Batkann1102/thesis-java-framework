package mn.edu.num.app.ctorinject;

import mn.edu.num.annotation.Autowired;
import mn.edu.num.annotation.Component;

@Component
public class Service {
    public final Repo repo;

    @Autowired
    public Service(Repo repo) {
        this.repo = repo;
    }

    public String describe() {
        return "service uses " + repo.name();
    }
}
