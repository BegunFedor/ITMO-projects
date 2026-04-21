package prog.lab3.characters;

import prog.lab3.enums.EmotionalState;
import prog.lab3.objects.AbstractObject;

import java.util.Objects;

public class ElegantGentleman extends AbstractCharacter {
    public ElegantGentleman(String name, EmotionalState emotionalState, AbstractObject location) {
        super(name, emotionalState, location);
    }

    @Override
    public void performAction() {
        System.out.println(getName() + " недовольно бормочет о недостатках мусоропроводной системы.");
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

}

