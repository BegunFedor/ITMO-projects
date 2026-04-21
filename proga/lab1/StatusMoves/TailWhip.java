package main.StatusMoves;

import ru.ifmo.se.pokemon.*;


public final class TailWhip extends StatusMove {
    public TailWhip() {
        super(Type.NORMAL, 0, 100);
    }

    @Override
    public void applyOppEffects(Pokemon p) {
        p.setMod(Stat.DEFENSE, -1);
    }

    @Override
    public String describe() {
        return "дискредитирует оппонента";
    }
}