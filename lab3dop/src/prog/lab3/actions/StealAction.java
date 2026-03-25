package prog.lab3.actions;

import prog.lab3.characters.MisterSmith;
import prog.lab3.exceptions.IllegalActionException;
import prog.lab3.exceptions.UncheckedException;
import prog.lab3.objects.AbstractObject;

public class StealAction implements Action {
    private final MisterSmith thief;
    private final AbstractObject item;

    public StealAction(MisterSmith thief, AbstractObject item) {
        this.thief = thief;
        this.item = item;
    }

    @Override
    public void execute() throws IllegalActionException {
        if (item == null) {
            throw new IllegalActionException("Предмет для кражи отсутствует!");
        }
        if (thief.getItems().contains(item)) {
            throw new UncheckedException(thief.getName() + " уже украл этот предмет: " + item.getName());
        }

        System.out.println(thief.getName() + " украл " + item.getName());
        thief.getItems().add(item); // Добавляем предмет в инвентарь
    }
}
