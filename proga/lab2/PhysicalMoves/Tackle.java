package main.PhysicalMoves;

import ru.ifmo.se.pokemon.*;


public final class Tackle extends PhysicalMove {
    public Tackle() {
        super(Type.NORMAL, 40, 100);
    }

    @Override
    public String describe() {
        return "ударяет";
    }
}
