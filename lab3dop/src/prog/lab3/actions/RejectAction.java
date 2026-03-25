package prog.lab3.actions;

import prog.lab3.characters.Malysh;
import prog.lab3.exceptions.IllegalActionException;

public class RejectAction implements Action {
    private final Malysh malysh;

    public RejectAction (Malysh malysh) {
        this.malysh = malysh;
    }

    @Override
    public void execute() throws IllegalActionException {
        System.out.println(malysh.getName() + " осознает ошибку и на этот раз поступает по-другому.");
    }
}