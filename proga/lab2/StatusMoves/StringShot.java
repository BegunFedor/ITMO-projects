package main.StatusMoves;

import ru.ifmo.se.pokemon.*;


public final class StringShot extends StatusMove {
    public StringShot() {
        super(Type.BUG, 0, 95);
    }

    @Override
    public void applyOppEffects(Pokemon p) {
        p.setMod(Stat.SPEED, -2);
    }

    @Override
    public String describe() {
        return "замедляет";
    }
}