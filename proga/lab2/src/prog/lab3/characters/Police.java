package prog.lab3.characters;

import prog.lab3.enums.EmotionalState;
import prog.lab3.objects.AbstractObject;
import java.util.Objects;

public class Police extends AbstractCharacter {
    public Police(AbstractObject location) {
        super("Полиция", EmotionalState.NERVOUS, location);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!super.equals(o)) return false;
        return o instanceof Police;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    @Override
    public String toString() {
        return super.toString() + " | Role: Police";
    }
    @Override
    public void performAction() {
        System.out.println(getName() + " поедает пончики.");
    }
}
