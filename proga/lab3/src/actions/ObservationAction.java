package actions;

import characters.AbstractCharacter;

public class ObservationAction implements Action {
    private final AbstractCharacter observer;
    private final AbstractCharacter target;

    public ObservationAction(AbstractCharacter observer, AbstractCharacter target) {
        this.observer = observer;
        this.target = target;
    }

    @Override
    public void execute() {
        System.out.println(observer.getName() + " наблюдает за " + target.getName());
    }
}
