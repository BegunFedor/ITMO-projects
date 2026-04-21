package actions;

import characters.ElegantGentleman;

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
