package mn.edu.num.container;

import java.util.ArrayList;
import java.util.List;

/**
 * Dependency tree-ийн нэг node.
 * Тухайн bean-ийг дүрсэлж, түүний @Autowired хамаарлуудыг dependencies жагсаалтад хадгална.
 */
public class DependencyNode {

    private final String name;          // bean-ийн нэр
    private final Class<?> type;        // bean-ийн класс
    private final ScopeType scope;      // bean-ийн scope
    private final List<DependencyNode> dependencies; // энэ bean-ийн шаардлагатай bean-ууд

    public DependencyNode(String name, Class<?> type, ScopeType scope) {
        this.name = name;
        this.type = type;
        this.scope = scope;
        this.dependencies = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public ScopeType getScope() {
        return scope;
    }

    public List<DependencyNode> getDependencies() {
        return dependencies;
    }

    public void addDependency(DependencyNode node) {
        dependencies.add(node);
    }

    @Override
    public String toString() {
        return name + " (" + scope + ")";
    }
}

