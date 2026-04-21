package actions;

import characters.Karlson;
import characters.Malysh;

public class CherryEatingAction implements Action {
    private final Karlson karlson;
    private final Malysh malysh;

    public CherryEatingAction(Karlson karlson, Malysh malysh) {
        this.karlson = karlson;
        this.malysh = malysh;
    }

    @Override
    public void execute() {
        System.out.println(karlson.getName() + " и " + malysh.getName() + " сидят на крыльце и едят сухие вишни.");
        System.out.println("Косточки весело стучат и катятся вниз по крыше.");
    }
}
