package prog.lab3.objects;
import prog.lab3.characters.AbstractCharacter;
import prog.lab3.enums.LightLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
public class EveningScene extends AbstractObject implements Describable{
    private final List<AbstractCharacter> occupants;
    private final Coordinates coordinates;

    public EveningScene(String name, LightLevel lightLevel, Coordinates coordinates) {
        super(name, lightLevel);
        this.occupants = new ArrayList<>();
        this.coordinates = coordinates;
    }
    public void addResident(AbstractCharacter character) {
        occupants.add(character);
        System.out.println(character.getName() + " теперь участвует " + getName());
    }
    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void removeResident(AbstractCharacter character) {
        occupants.remove(character);
        System.out.println(character.getName() + " покинул " + getName());
    }

    public List<AbstractCharacter> getResidents() {
        return occupants;
    }

    public void describeWindows() {
        Random random = new Random();
        int totalWindows = random.nextInt(20) + 3; //

        System.out.println("Малыш начал считать светящиеся окна.");
        System.out.println("Сначала их было три... Потом десять... А потом так много, что зарябило в глазах.");
        System.out.println("Всего окон: " + totalWindows);

    }


    @Override
    public String getDescription() {
        System.out.println("Вечерело. Мягкие, теплые осенние сумерки спускались на крыши и дома.");
        describeWindows();
        return "В" + getName() + " участвуют " + (occupants.isEmpty() ? "никого нет." : occupants) + ", Координаты: " + coordinates;
    }
}


