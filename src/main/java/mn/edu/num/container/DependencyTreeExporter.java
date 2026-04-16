package mn.edu.num.container;

import java.util.Map;

/**
 * Dependency tree-г JSON хэлбэрээр гаргах exporter.
 */
public class DependencyTreeExporter {

    /**
     * nodeMap-аас JSON string үүсгэнэ.
     */
    public static String toJson(Map<String, DependencyNode> nodeMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"beans\": [\n");

        var entries = nodeMap.values().stream().toList();
        for (int i = 0; i < entries.size(); i++) {
            DependencyNode node = entries.get(i);
            sb.append("    {\n");
            sb.append("      \"name\": \"").append(node.getName()).append("\",\n");
            sb.append("      \"type\": \"").append(node.getType().getName()).append("\",\n");
            sb.append("      \"scope\": \"").append(node.getScope()).append("\",\n");
            sb.append("      \"dependencies\": [");

            var deps = node.getDependencies();
            if (deps.isEmpty()) {
                sb.append("]");
            } else {
                sb.append("\n");
                for (int j = 0; j < deps.size(); j++) {
                    sb.append("        \"").append(deps.get(j).getName()).append("\"");
                    if (j < deps.size() - 1) sb.append(",");
                    sb.append("\n");
                }
                sb.append("      ]");
            }
            sb.append("\n    }");
            if (i < entries.size() - 1) sb.append(",");
            sb.append("\n");
        }

        sb.append("  ]\n");
        sb.append("}");
        return sb.toString();
    }
}

