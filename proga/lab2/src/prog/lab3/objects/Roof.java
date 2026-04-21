package prog.lab3.objects;

import prog.lab3.characters.AbstractCharacter;
import prog.lab3.enums.LightLevel;

import java.util.ArrayList;
import java.util.List;

public class Roof extends AbstractObject implements Describable {
    private final List<AbstractCharacter> occupants;
    private final Coordinates coordinates;

    public Roof(String name, LightLevel lightLevel, Coordinates coordinates) {
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

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public List<AbstractCharacter> getOccupants() {
        return occupants;
    }

    @Override
    public String getDescription() {
        return getName() + " освещена " + getLightLevel() + ". Здесь находятся: " +
                (occupants.isEmpty() ? "никого нет." : occupants) + ", Координаты: " + coordinates;
    }
}
