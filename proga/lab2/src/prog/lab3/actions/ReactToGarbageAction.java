package prog.lab3.actions;

import prog.lab3.characters.ElegantGentleman;

public class ReactToGarbageAction implements Action {
    private final ElegantGentleman gentleman;

    public ReactToGarbageAction(ElegantGentleman gentleman) {
        this.gentleman = gentleman;
    }

    @Override
    public void execute() {
        System.out.println(gentleman.getName() + " долго и тщательно отряхивается, проклиная сорвавшийся мусор.");
    }
}
