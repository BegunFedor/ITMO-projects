package prog.lab3.actions;

import prog.lab3.objects.TrashBin;
import prog.lab3.characters.Malysh;
import java.util.Random;

public class GarbageFlowAction implements Action {
    private final TrashBin trashBin;
    private final Malysh malysh;

    public GarbageFlowAction(TrashBin trashBin, Malysh malysh) {
        this.trashBin = trashBin;
        this.malysh = malysh;
    }

    @Override
    public void execute() {
        System.out.println(malysh.getName() + " высыпал мусор из ведра, и он покатился вниз по крыше!");
        simulateGarbageFlow();
    }

    private void simulateGarbageFlow() {
        Random random = new Random();
        String[] trashItems = {"ореховая скорлупа", "вишневые косточки", "скомканная бумага"};
        for (String item : trashItems) {
            if (random.nextBoolean()) {
                System.out.println(item + " устремились вниз и попали на прохожего.");
            }
        }
    }
}
