package actions;

import characters.Malysh;

public class CallPoliceAction implements Action {
    private final Malysh character;

    public CallPoliceAction(Malysh character) {
        this.character = character;
    }

    @Override
    public void execute() {
        System.out.println(character.getName() + " звонит в полицию.");
    }
}
