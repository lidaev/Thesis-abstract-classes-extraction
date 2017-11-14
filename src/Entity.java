import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 17.10.2017.
 */
public class Entity {
    String name;
    List<Method> methods;

    public Entity(String name) {
        methods = new ArrayList<>();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }

    public void addMethod(Method method) {
        methods.add(method);
    }

    public void printMethods() {
        if (!methods.isEmpty())
        for (Method method : methods) {
            System.out.println("Method: " + method.getName() + "\n Attributes: ");
            if (!method.attributes.isEmpty())
                method.getAttributes();
        }
    }

    public Entity getEntity(String name) {
        if (name.equals(this.name))
            return this;
        return null;
    }
}
