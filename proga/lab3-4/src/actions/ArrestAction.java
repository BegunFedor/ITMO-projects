package actions;

import characters.Police;
import characters.MisterSmith;

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
