package mn.edu.num.container;

import mn.edu.num.exception.NoSuchBeanException;

import java.util.*;

public class BeanRegistry {
    // bean нэр → BeanDefinition
    private final Map<String, BeanDefinition> definitions = new HashMap<>();
    // singleton cache
    private final Map<String, Object> singletonCache = new HashMap<>();

    public void register(BeanDefinition definition) {
        definitions.put(definition.getBeanName(), definition);
    }

    public BeanDefinition getDefinition(String name) {
        BeanDefinition def = definitions.get(name);
        if (def == null) {
            throw new NoSuchBeanException(name, definitions.keySet());
        }
        return def;
    }

    public Collection<BeanDefinition> getAllDefinitions() {
        return definitions.values();
    }

    public void cacheSingleton(String name, Object instance) {
        singletonCache.put(name, instance);
    }

    public Object getSingleton(String name) {
        return singletonCache.get(name);
    }

    public boolean hasSingleton(String name) {
        return singletonCache.containsKey(name);
    }

    // Төрлөөр хайх
    public List<BeanDefinition> findByType(Class<?> type) {
        List<BeanDefinition> result = new ArrayList<>();
        for (BeanDefinition def : definitions.values()) {
            if (type.isAssignableFrom(def.getBeanClass())) {
                result.add(def);
            }
        }
        return result;
    }
}
