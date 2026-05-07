package mn.edu.num.container;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * {@link ApplicationContext}-ийг fluent API-аар тохируулан үүсгэх builder.
 *
 * <p>Энэ нь developer experience (DX)-г сайжруулах зорилгоор @EnableIoC annotation
 * гэж заавал дамжуулахгүйгээр кодоор тохиргоог уншихад хялбар болгоно. Жишээ нь
 * test-д тус бүр өөр package, lazy/strict тохиргоо хэрэгтэй болоход илүү тохиромжтой.
 *
 * <pre>
 * ApplicationContext ctx = ApplicationContextBuilder.builder()
 *     .basePackage("mn.edu.num.app")
 *     .excludePackages("mn.edu.num.app.broken")
 *     .lazyInit(true)
 *     .strictScope(true)
 *     .build();
 * </pre>
 */
public final class ApplicationContextBuilder {

    private String basePackage;
    private final List<String> excludePackages = new ArrayList<>();
    private boolean lazyInit = false;
    private boolean strictScope = false;
    private boolean visualize = false;

    private ApplicationContextBuilder() {}

    public static ApplicationContextBuilder builder() {
        return new ApplicationContextBuilder();
    }

    /**
     * Scan хийх root package-г заана. Зайлшгүй параметр.
     */
    public ApplicationContextBuilder basePackage(String basePackage) {
        this.basePackage = basePackage;
        return this;
    }

    /**
     * Алгасах package-уудыг нэмж заана. Дахин дуудахад нэмэгдэнэ.
     */
    public ApplicationContextBuilder excludePackages(String... packages) {
        excludePackages.addAll(Arrays.asList(packages));
        return this;
    }

    /**
     * lazyInit=true бол singleton bean-ыг шаардлагатай үед үүсгэнэ.
     */
    public ApplicationContextBuilder lazyInit(boolean enabled) {
        this.lazyInit = enabled;
        return this;
    }

    /**
     * strictScope=true бол @Scope("foo") гэх мэт зөвшөөрөгдөөгүй утга
     * BeanScopeException шиднэ.
     */
    public ApplicationContextBuilder strictScope(boolean enabled) {
        this.strictScope = enabled;
        return this;
    }

    /**
     * visualize=true бол dependency tree HTML дүрслэлийг үүсгэнэ.
     */
    public ApplicationContextBuilder visualize(boolean enabled) {
        this.visualize = enabled;
        return this;
    }

    public ApplicationContext build() {
        if (basePackage == null || basePackage.isBlank()) {
            throw new IllegalStateException(
                    "ApplicationContextBuilder: basePackage заавал тодорхойлох ёстой.");
        }
        ApplicationContext ctx = new ApplicationContext(
                basePackage, List.copyOf(excludePackages), lazyInit, strictScope);
        if (visualize) {
            DependencyTreeHtmlExporter.generateAndOpen(ctx.getTreeBuilder().getNodeMap());
        }
        return ctx;
    }
}
