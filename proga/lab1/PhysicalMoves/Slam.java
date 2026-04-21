package main.PhysicalMoves;

import ru.ifmo.se.pokemon.*;

public final class Slam extends PhysicalMove {
    public Slam() {
        super(Type.NORMAL, 85, 75);
    }

    @Override
    public String describe() {
        return "хлестает";
    }
}
