package prog.lab3.actions;

import prog.lab3.characters.Malysh;
import prog.lab3.objects.TrashBin;

public class CleanAction implements Action {
    private final Malysh malysh;
    private final TrashBin trashBin;

    public CleanAction(Malysh malysh, TrashBin trashBin) {
        this.malysh = malysh;
        this.trashBin = trashBin;
    }
    @Override
    public void execute() {
        System.out.println(malysh.getName() + " убирает и выбрасывает мусор в " + trashBin.getName() + ".");
        trashBin.addTrash();
    }
}
