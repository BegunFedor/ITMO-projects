package prog.lab3.actions;

import prog.lab3.characters.MisterSmith;

public class ShakeOffDustAction implements Action {
    private final MisterSmith character;

    public ShakeOffDustAction(MisterSmith character) {
        this.character = character;
    }

    @Override
    public void execute() {
        System.out.println(character.getName() + " стряхивает пыль.");
    }
}
