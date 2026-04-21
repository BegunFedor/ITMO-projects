package main.PhysicalMoves;

import ru.ifmo.se.pokemon.*;


public final class WaterFall extends PhysicalMove {
    public WaterFall() {
        super(Type.WATER, 80, 100);
    }

    @Override
    public void applyOppEffects(Pokemon p) {
        if (Math.random() < .2) {
            Effect.flinch(p);
        }
    }

    @Override
    public String describe() {
        return "ударяет с напором воды";
    }
}
