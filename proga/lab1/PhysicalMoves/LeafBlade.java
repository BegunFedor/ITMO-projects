package main.PhysicalMoves;

import ru.ifmo.se.pokemon.*;


public final class LeafBlade extends PhysicalMove {
    public LeafBlade() {
        super(Type.GRASS, 90, 100);
    }

    @Override
    public String describe() {
        return "хлестает листком";
    }
}
