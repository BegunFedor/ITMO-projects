package prog.lab3.actions;

import prog.lab3.characters.Malysh;
import prog.lab3.characters.Mother;

public class VisitDoctorAction implements Action {
    private final Mother character;

    public VisitDoctorAction(Mother character) {
        this.character = character;
    }

    @Override
    public void execute() {
        System.out.println("Пока Малыш гостил у Карлсона, " + character.getName() + " была у доктора. ");
        System.out.println("Она задержалась дольше, чем рассчитывала, а когда вернулась домой, Малыш уже преспокойно сидел в своей комнате и рассматривал марки.");
    }
}