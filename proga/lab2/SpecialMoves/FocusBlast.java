package main.SpecialMoves;

import ru.ifmo.se.pokemon.*;


public final class FocusBlast extends SpecialMove {
    public FocusBlast() {
        super(Type.FIGHTING, 120, 70);
    }

    @Override
    public void applyOppEffects(Pokemon p) {
        if (Math.random() < .1)
            p.setMod(Stat.SPECIAL_ATTACK, -1);
    }

    @Override
    public String describe() {
        return "вызывает снежную бурю";
    }
}

