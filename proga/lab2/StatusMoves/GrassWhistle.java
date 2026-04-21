package main.StatusMoves;

import ru.ifmo.se.pokemon.*;


public final class GrassWhistle extends StatusMove {
    public GrassWhistle() {
        super(Type.GRASS, 0, 55);
    }

    @Override
    public void applyOppEffects(Pokemon p) {
        p.setCondition((new Effect()).turns(2).condition(Status.SLEEP));
    }

    @Override
    public String describe() {
        return "усыпляет используя свитсток";
    }
}