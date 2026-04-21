package actions;

import characters.Malysh;
import objects.Match;
import exceptions.IllegalActionException;

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
