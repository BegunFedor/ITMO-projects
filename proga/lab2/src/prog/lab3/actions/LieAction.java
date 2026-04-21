package prog.lab3.actions;

import prog.lab3.characters.Malysh;

public class LieAction implements Action {
    private final Malysh character;

    public LieAction(Malysh character) {
        this.character = character;
    }

    @Override
    public void execute() {
        System.out.println(character.getName() + " о том, что он всего несколько минут назад вернулся с крыши, он просто умолчал.");
        System.out.println("Конечно, мама очень умная и почти все понимает, но поймет ли она, что ему обязательно нужно было лезть на крышу...");
    }
}