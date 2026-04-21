package prog.lab3.objects;

import prog.lab3.enums.LightLevel;
import prog.lab3.characters.*;
import java.util.ArrayList;
import java.util.List;

public class Street extends AbstractObject implements Describable{
    private final List<AbstractCharacter> occupants;
    private final Coordinates coordinates;

    public Street(String name, LightLevel lightLevel, Coordinates coordinates) {
        super(name, lightLevel);
        this.occupants = new ArrayList<>();
        this.coordinates = coordinates;
    }

    public void addResident(AbstractCharacter character) {
        occupants.add(character);
        System.out.println(character.getName() + " теперь на " + getName());
    }

    public void removeResident(AbstractCharacter character) {
        occupants.remove(character);
        System.out.println(character.getName() + " покинул " + getName());
    }


    @Override
    public String getDescription() {
        return "Местоположение: " + getName() + " (Освещенность: " + getLightLevel() + "). Сейчас здесь находятся: " +
                (occupants.isEmpty() ? "никого нет" :occupants);
    }
}

