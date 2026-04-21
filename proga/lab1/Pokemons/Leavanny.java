package main.Pokemons;

import main.PhysicalMoves.Tackle;
import main.StatusMoves.*;
import ru.ifmo.se.pokemon.Pokemon;
import ru.ifmo.se.pokemon.Type;


public final class Leavanny extends Pokemon {
    public Leavanny (String name, int i) {
        super(name, i);
        super.setType(Type.BUG,Type.GRASS);
        super.setStats(75, 103, 80, 70, 80, 92);
        super.setMove(new Rest(), new StringShot(), new GrassWhistle(), new Tackle());
    }
}
