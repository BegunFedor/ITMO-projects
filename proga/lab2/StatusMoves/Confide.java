package main.StatusMoves;

import ru.ifmo.se.pokemon.*;


public final class Confide extends StatusMove {
    public Confide() {
        super(Type.NORMAL, 0, 100);
    }

    @Override
    public void applyOppEffects(Pokemon p) {
        p.setMod(Stat.SPECIAL_ATTACK, -1);
    }

    @Override
    public String describe() {
        return "успокаивает";
    }
}
