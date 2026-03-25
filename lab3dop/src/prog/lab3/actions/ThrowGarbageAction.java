package prog.lab3.actions;


import prog.lab3.characters.Karlson;
import prog.lab3.objects.TrashBin;

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

