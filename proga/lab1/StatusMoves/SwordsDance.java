package main.StatusMoves;

import ru.ifmo.se.pokemon.*;


public final class SwordsDance extends StatusMove {
    public SwordsDance() {
        super(Type.NORMAL, 0, 100);
    }

    @Override
    public void applySelfEffects(Pokemon p) {
        p.setMod(Stat.ATTACK, 2);
    }

    @Override
    public String describe() {
        return "размахивает мечами, повышая свою аттаку на 2";
    }
}
