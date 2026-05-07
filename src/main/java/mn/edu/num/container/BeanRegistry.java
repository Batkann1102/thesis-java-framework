package mn.edu.num.container;

import mn.edu.num.exception.NoSuchBeanException;
import mn.edu.num.util.Levenshtein;

import java.util.*;

/**
 * Бүртгэгдсэн Bean Definition буюу тохируулгауд болон
 * инстансуудыг (Singleton cache гм) хадгалж удирдах зориулалттай хуваарьт санах ойн Registry класс.
 * Бусад модулиуд нь BeanRegistry-руу хандаж бүртгэх эсвэл хайж сураг гаргах үүрэгтэй.
 *
 * <p>Энэхүү класс нь {@link LinkedHashMap}-ийг ашиглан bean-уудын бүртгэлийн дарааллыг
 * хадгалдаг бөгөөд энэ нь:
 * <ul>
 *   <li>singleton-уудыг урьдчилан үүсгэхэд тогтвортой дараалал хангах</li>
 *   <li>{@link ApplicationContext#close()} үед урвуу дарааллаар @PreDestroy дуудах</li>
 *   <li>тестийн reproducibility</li>
 * </ul>
 * зэрэг чанарыг хангадаг.
 */
public class BeanRegistry {
    // bean нэр → BeanDefinition. Бүртгэлийн дарааллыг хадгална.
    private final Map<String, BeanDefinition> definitions = new LinkedHashMap<>();
    // singleton cache. Бүртгэлийн дарааллыг хадгална.
    private final Map<String, Object> singletonCache = new LinkedHashMap<>();

    public void register(BeanDefinition definition) {
        definitions.put(definition.getBeanName(), definition);
    }

    public BeanDefinition getDefinition(String name) {
        BeanDefinition def = definitions.get(name);
        if (def == null) {
            // "Did-you-mean" санал зорилгоор хамгийн ойр нэрсийг олно.
            List<String> suggestions = Levenshtein.suggest(name, definitions.keySet(), 3);
            if (!suggestions.isEmpty()) {
                throw new NoSuchBeanException(
                        "Bean олдсонгүй: '" + name + "'. Магадгүй та "
                                + suggestions + " гэснийг хэлсэн үү? "
                                + "Бүртгэлтэй bean-үүд: " + definitions.keySet());
            }
            throw new NoSuchBeanException(name, definitions.keySet());
        }
        return def;
    }

    public Collection<BeanDefinition> getAllDefinitions() {
        return definitions.values();
    }

    /** Бүх bean нэрсийг бүртгэлийн дарааллаар буцаана. */
    public Set<String> getRegisteredNames() {
        return definitions.keySet();
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

    /**
     * Бүх singleton-уудыг бүртгэгдсэн дарааллаар буцаана.
     * @return immutable view of registered singleton entries
     */
    public List<Map.Entry<String, Object>> getSingletonEntriesInRegistrationOrder() {
        return List.copyOf(singletonCache.entrySet());
    }

    /** Singleton кэшийг бүхэлд нь цэвэрлэнэ (close-ын дараа дахин ашиглахаас сэргийлнэ). */
    public void clearSingletons() {
        singletonCache.clear();
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
