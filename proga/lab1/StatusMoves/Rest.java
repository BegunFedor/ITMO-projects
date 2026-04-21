package main.StatusMoves;

import ru.ifmo.se.pokemon.*;


public final class Rest extends StatusMove {
    public Rest() {
        super(Type.PSYCHIC, 0, 100);
    }

    @Override
    public void applySelfEffects(Pokemon p) {
        p.setCondition((new Effect()).turns(2).condition(Status.SLEEP));
        p.restore();
    }

    @Override
    public String describe() {
        return "использует Rest и полностью восстанавливает HP за два пропущенных хода";
    }
}
