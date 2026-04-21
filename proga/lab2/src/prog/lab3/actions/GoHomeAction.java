package prog.lab3.actions;
import prog.lab3.exceptions.IllegalActionException;
import prog.lab3.characters.Karlson;
import prog.lab3.characters.Malysh;

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

