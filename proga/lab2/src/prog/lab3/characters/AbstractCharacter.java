package prog.lab3.characters;

import prog.lab3.enums.EmotionalState;
import prog.lab3.objects.AbstractObject;
import java.util.Objects;

public abstract class AbstractCharacter {
    private final String name;
    private EmotionalState emotionalState;
    private AbstractObject location;

    public AbstractCharacter(String name, EmotionalState emotionalState, AbstractObject location) {
        this.name = name;
        this.emotionalState = emotionalState;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public EmotionalState getEmotionalState() {
        return emotionalState;
    }

    public void setEmotionalState(EmotionalState emotionalState) {
        this.emotionalState = emotionalState;
    }

    public AbstractObject getLocation() {
        return location;
    }

    public void setLocation(AbstractObject location) {
        this.location = location;
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractCharacter that = (AbstractCharacter) o;
        return name.equals(that.name);
    }



    public int hashCode() {
        return Objects.hash(name);
    }


    public String toString() {
        return "Character{" +
                "name='" + name + '\'' +
                ", emotionalState=" + emotionalState +
                ", location=" + location +
                '}';
    }
    public abstract void performAction();
}
