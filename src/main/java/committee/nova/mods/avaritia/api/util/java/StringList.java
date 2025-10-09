package committee.nova.mods.avaritia.api.util.java;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author: cnlimiter
 */
public class StringList extends ArrayList<String> {
    public StringList() {
    }

    public StringList(String... elements) {
        super(elements.length);
        Collections.addAll(this, elements);
    }

    public StringList put(String... elements) {
        Collections.addAll(this, elements);
        return this;
    }

    public String get(int index) {
        if (index >= this.size()) {
            return super.get(index % this.size());
        } else {
            return super.get(index);
        }
    }
}
