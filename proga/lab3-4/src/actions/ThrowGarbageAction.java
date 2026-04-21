package actions;


import characters.Karlson;
import objects.TrashBin;

public class ThrowGarbageAction implements Action{
    private final Karlson karlson;
    private final TrashBin trashBin;

    public ThrowGarbageAction(Karlson karlson, TrashBin trashBin) {
        this.karlson = karlson;
        this.trashBin = trashBin;
    }
    @Override
    public void execute() {
        System.out.println(karlson.getName() + " выбрасывает ореховую скорлупу и бумагу из " + trashBin.getName() + " вниз с крыши.");
    }
}

