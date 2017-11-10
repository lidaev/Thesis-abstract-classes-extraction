import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 17.10.2017.
 */
public class Method {
    String name;
    List<String> actions;
    List<String> attributes;

    public Method(String name) {
        this.name = name;
        actions = new ArrayList<>();
    }

    public void addAction(String action) {
        actions.add(action);
    }

    public void getAttributes() {
        if (!attributes.isEmpty()) {
            for (String attr : attributes) System.out.println(attr + " ");
        }
    }

    public void addAttribute(String attribute) {
        attributes.add(attribute);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
