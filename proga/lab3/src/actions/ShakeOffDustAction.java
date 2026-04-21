package actions;

import characters.MisterSmith;

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
