package main.Pokemons;

import main.StatusMoves.*;
import main.SpecialMoves.*;
import main.PhysicalMoves.*;
import ru.ifmo.se.pokemon.*;


public class Wooper extends Pokemon {
    public Wooper (String name, int i) {
        super(name, i);
        super.setType(new Type[]{Type.WATER, Type.GROUND});
        super.setStats(55, 45, 45, 25, 25, 15);
        super.setMove(new TailWhip(), new WaterFall(), new Rest());
    }
}
