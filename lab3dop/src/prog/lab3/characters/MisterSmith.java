package prog.lab3.characters;

import prog.lab3.enums.EmotionalState;
import prog.lab3.objects.AbstractObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MisterSmith extends AbstractCharacter {
    private final List<AbstractObject> items = new ArrayList<>();

    public MisterSmith(String name, EmotionalState emotionalState, AbstractObject location) {
        super(name, emotionalState, location);
    }




    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MisterSmith that = (MisterSmith) o;
        return items.equals(that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), items);
    }

    @Override
    public String toString() {
        return super.toString() + " | Stealing: " + items.size() + " items.";
    }
    public List<AbstractObject> getItems() {
        return items;
    }
    @Override
    public void performAction() {
        System.out.println(getName() + " тоже попадает в зону падения мусора.");
    }
}
