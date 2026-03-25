package prog.lab3.objects;

import prog.lab3.characters.AbstractCharacter;
import prog.lab3.enums.LightLevel;

import java.util.ArrayList;
import java.util.List;

public class House extends AbstractObject implements Describable {
    private final Coordinates coordinates;
    private final List<AbstractCharacter> residents;


    public House(String name, LightLevel lightLevel, Coordinates coordinates) {
        super(name, lightLevel);
        this.residents = new ArrayList<>();
        this.coordinates = coordinates;
    }

    public void addResident(AbstractCharacter character) {
        residents.add(character);
        System.out.println(character.getName() + " теперь находится в " + getName());
    }
    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void removeResident(AbstractCharacter character) {
        residents.remove(character);
        System.out.println(character.getName() + " покинул " + getName());
    }


    public List<AbstractCharacter> getResidents() {
        return residents;
    }

    @Override
    public String getDescription() {
        return getName() + " освещён " + getLightLevel() + ". Здесь находятся: " +
                (residents.isEmpty() ? "никого нет." : residents) + ", Координаты: " + coordinates;
    }
}
