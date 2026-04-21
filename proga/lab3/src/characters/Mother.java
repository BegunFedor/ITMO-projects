package characters;

import enums.EmotionalState;
import objects.AbstractObject;

import java.util.Objects;

public class Mother extends AbstractCharacter {

    private EmotionalState emotionalState;


    public Mother(String name, EmotionalState emotionalState, AbstractObject location) {
        super(name, emotionalState, location);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!super.equals(o)) return false;
        return o instanceof Malysh;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    @Override
    public String toString() {
        return super.toString() + " | Role: Mother";
    }



    @Override
    public void performAction() {
        if (emotionalState == EmotionalState.HAPPY) {
            System.out.println(getName() + " выглядела веселой.");
        } else {
            System.out.println(getName() + " выглядела невеселой.");
        }
    }
}