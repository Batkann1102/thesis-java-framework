package mn.edu.num.container;

public class BeanDefinition {
    private String name;
    private Class<?> beanClass;
    private ScopeType scope;

    public BeanDefinition(String name, Class<?> beanScope, ScopeType scope){
        this.name = name;
        this.beanClass = beanScope;
        this.scope = scope;
    }

    public String getBeanName() {return this.name;}
    public Class<?> getBeanClass() {return this.beanClass;}
    public ScopeType getScope() {return this.scope;}

    @Override
    public String toString() {
        return "BeanDefinition{" +
                "name='"  + name  + '\'' +
                ", class=" + beanClass.getSimpleName() +
                ", scope=" + scope +
                '}';
    }
}
