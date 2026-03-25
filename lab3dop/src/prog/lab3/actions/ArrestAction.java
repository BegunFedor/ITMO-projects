package prog.lab3.actions;

import prog.lab3.characters.Police;
import prog.lab3.characters.MisterSmith;

public class ArrestAction implements Action {
    private final Police police;
    private final MisterSmith target;

    public ArrestAction(Police police, MisterSmith target) {
        this.police = police;
        this.target = target;
    }

    @Override
    public void execute() {
        System.out.println(police.getName() + " арестовывает " + target.getName());
    }

}
