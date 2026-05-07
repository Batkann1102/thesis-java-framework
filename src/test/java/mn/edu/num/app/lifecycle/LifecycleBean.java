package mn.edu.num.app.lifecycle;

import mn.edu.num.annotation.Component;
import mn.edu.num.annotation.PostConstruct;
import mn.edu.num.annotation.PreDestroy;

import java.util.ArrayList;
import java.util.List;

@Component
public class LifecycleBean {
    public static final List<String> events = new ArrayList<>();
    public boolean initialized = false;
    public boolean destroyed = false;

    @PostConstruct
    public void init() {
        initialized = true;
        events.add("init:" + getClass().getSimpleName());
    }

    @PreDestroy
    public void shutdown() {
        destroyed = true;
        events.add("destroy:" + getClass().getSimpleName());
    }
}
