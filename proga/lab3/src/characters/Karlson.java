package characters;

import enums.EmotionalState;
import objects.AbstractObject;
import java.util.Objects;

public class Karlson extends AbstractCharacter {
    public Karlson(String name, EmotionalState emotionalState, AbstractObject location) {
        super(name, emotionalState, location);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!super.equals(o)) return false;
        return o instanceof Karlson;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    @Override
    public String toString() {
        return super.toString() + " | Role: Karlson";
    }
    @Override
    public void performAction() {
        System.out.println(getName() + " разговаривает с малышом.");
    }
}
