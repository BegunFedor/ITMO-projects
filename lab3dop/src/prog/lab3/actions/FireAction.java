package prog.lab3.actions;

import prog.lab3.characters.Malysh;
import prog.lab3.objects.Match;
import prog.lab3.exceptions.IllegalActionException;

public class FireAction implements Action {
    private final Malysh malysh;
    private final Match match;

    public FireAction(Malysh malysh, Match match) {
        this.malysh = malysh;
        this.match = match;
    }

    @Override
    public void execute() throws IllegalActionException {
        if (match.isUsed()) {
            throw new IllegalActionException("Спичка уже использована и не может быть зажжена снова.");
        }

        // Зажигаем спичку
        match.ignite();
        System.out.println(malysh.getName() + " чиркнул спичкой. Пламя взметнулось, дрова затрещали и загудели.");
    }
}
