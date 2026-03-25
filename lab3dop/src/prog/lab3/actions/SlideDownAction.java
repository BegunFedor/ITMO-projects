package prog.lab3.actions;

import prog.lab3.characters.Karlson;
import prog.lab3.objects.Roof;


public class SlideDownAction implements Action {
    private final Karlson karlson;
    private final Roof roof;

    public SlideDownAction(Karlson karlson, Roof roof) {
        this.karlson = karlson;
        this.roof = roof;
    }
    @Override
    public void execute() {
        System.out.println(karlson.getName() + " скатывается вниз по склону " + roof.getName() + ".");
    }
}
