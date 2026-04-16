package mn.edu.num.container;

import mn.edu.num.annotation.Autowired;
import mn.edu.num.exception.CircularDependencyException;

import java.lang.reflect.Field;
import java.util.*;

/**
 * BeanRegistry дахь бүх BeanDefinition-уудаас dependency tree үүсгэж,
 * DFS ашиглан тойрог хамаарлыг илрүүлнэ.
 */
public class DependencyTreeBuilder {

    private final BeanRegistry registry;
    private final Map<String, DependencyNode> nodeMap = new LinkedHashMap<>();

    public DependencyTreeBuilder(BeanRegistry registry) {
        this.registry = registry;
    }

    /**
     * Бүх BeanDefinition-уудаас DependencyNode үүсгэж, dependency холбоосыг тогтооно.
     */
    public Map<String, DependencyNode> buildTree() {
        nodeMap.clear();

        // 1. Бүх bean-д node үүсгэх
        for (BeanDefinition def : registry.getAllDefinitions()) {
            nodeMap.put(def.getBeanName(),
                    new DependencyNode(def.getBeanName(), def.getBeanClass(), def.getScope()));
        }

        // 2. @Autowired field-ээр dependency холбоос тогтоох
        for (BeanDefinition def : registry.getAllDefinitions()) {
            DependencyNode node = nodeMap.get(def.getBeanName());
            for (Field field : def.getBeanClass().getDeclaredFields()) {
                if (!field.isAnnotationPresent(Autowired.class)) continue;

                Class<?> fieldType = field.getType();
                // Төрлөөр тохирох bean-г хайна
                List<BeanDefinition> candidates = registry.findByType(fieldType);
                if (candidates.size() == 1) {
                    DependencyNode depNode = nodeMap.get(candidates.get(0).getBeanName());
                    if (depNode != null) {
                        node.addDependency(depNode);
                    }
                }
                // Олон candidate байвал одоогоор алгасна (@Qualifier-тай тохиолдол)
            }
        }

        return Collections.unmodifiableMap(nodeMap);
    }

    /**
     * DFS ашиглан тойрог хамаарал байгаа эсэхийг шалгана.
     * Тойрог олдвол CircularDependencyException шидэнэ.
     */
    public void detectCircularDependencies() {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (String beanName : nodeMap.keySet()) {
            if (!visited.contains(beanName)) {
                dfs(beanName, visited, recursionStack, new ArrayList<>());
            }
        }
    }

    private void dfs(String current, Set<String> visited,
                     Set<String> recursionStack, List<String> path) {
        visited.add(current);
        recursionStack.add(current);
        path.add(current);

        DependencyNode node = nodeMap.get(current);
        if (node != null) {
            for (DependencyNode dep : node.getDependencies()) {
                if (!visited.contains(dep.getName())) {
                    dfs(dep.getName(), visited, recursionStack, path);
                } else if (recursionStack.contains(dep.getName())) {
                    // Тойрог хамаарал илэрлээ
                    List<String> cycle = new ArrayList<>(path.subList(path.indexOf(dep.getName()), path.size()));
                    cycle.add(dep.getName());
                    throw new CircularDependencyException(
                            "Тойрог хамаарал илэрлээ: " + String.join(" → ", cycle));
                }
            }
        }

        path.remove(path.size() - 1);
        recursionStack.remove(current);
    }

    /**
     * Tree-г root node-уудаас эхлэн console-д хэвлэнэ.
     * Root node = өөрийг нь dependency болгон ашигладаг бусад node байхгүй node.
     */
    public String printTree() {
        StringBuilder sb = new StringBuilder();
        sb.append("ApplicationContext\n");

        // Root node-уудыг олно (хэн нэгний dependency биш)
        Set<String> nonRoots = new HashSet<>();
        for (DependencyNode node : nodeMap.values()) {
            for (DependencyNode dep : node.getDependencies()) {
                nonRoots.add(dep.getName());
            }
        }

        List<DependencyNode> roots = new ArrayList<>();
        for (DependencyNode node : nodeMap.values()) {
            if (!nonRoots.contains(node.getName())) {
                roots.add(node);
            }
        }

        // Хэрэв бүгд non-root бол (circular бол) бүгдийг root болгоно
        if (roots.isEmpty()) {
            roots.addAll(nodeMap.values());
        }

        Set<String> printed = new HashSet<>();
        for (int i = 0; i < roots.size(); i++) {
            boolean isLast = (i == roots.size() - 1);
            printNode(sb, roots.get(i), "", isLast, printed);
        }

        return sb.toString();
    }

    private void printNode(StringBuilder sb, DependencyNode node,
                           String prefix, boolean isLast, Set<String> printed) {
        String connector = isLast ? " └─ " : " ├─ ";
        sb.append(prefix).append(connector).append(node).append("\n");

        if (printed.contains(node.getName())) {
            return; // Давталт хамгаалалт
        }
        printed.add(node.getName());

        String childPrefix = prefix + (isLast ? "     " : " │   ");
        List<DependencyNode> deps = node.getDependencies();
        for (int i = 0; i < deps.size(); i++) {
            boolean childIsLast = (i == deps.size() - 1);
            printNode(sb, deps.get(i), childPrefix, childIsLast, printed);
        }
    }

    public Map<String, DependencyNode> getNodeMap() {
        return Collections.unmodifiableMap(nodeMap);
    }
}

