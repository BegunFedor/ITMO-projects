package actions;
import exceptions.IllegalActionException;
import characters.Karlson;
import characters.Malysh;

public class GoHomeAction implements Action {
    private final Object character;

    public GoHomeAction(Object character) {
        this.character = character;
    }

    @Override
    public void execute() throws IllegalActionException {
        if (character instanceof Karlson) {
            System.out.println(((Karlson) character).getName() + " возвращается домой.");
        } else if (character instanceof Malysh) {
            System.out.println(((Malysh) character).getName() + " возвращается домой.");
        } else {
            throw new IllegalActionException("Только Карлсон и Малыш могут вернуться домой.");
        }
    }
}

