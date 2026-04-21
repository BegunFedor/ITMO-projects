package main.Pokemons;

import main.PhysicalMoves.WaterFall;
import main.SpecialMoves.FocusBlast;
import main.StatusMoves.*;
import ru.ifmo.se.pokemon.*;


public final class Quagsire extends Wooper {
    public Quagsire (String name, int i) {
        super(name, i);
        super.setType(Type.WATER, Type.GROUND);
        super.setStats(95, 85, 85, 65, 65, 35);
        super.setMove(new TailWhip(), new WaterFall(), new Rest(), new FocusBlast());
    }
}
