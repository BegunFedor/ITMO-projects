package main.Pokemons;

import main.StatusMoves.Rest;
import main.StatusMoves.StringShot;
import ru.ifmo.se.pokemon.Pokemon;
import ru.ifmo.se.pokemon.Type;


public class Sewaddle extends Pokemon {
    public Sewaddle(String name, int i) {
        super(name, i);
        super.setType(Type.BUG, Type.GRASS);
        super.setStats(45, 53, 70, 40, 60, 42);
        super.setMove(new Rest(), new StringShot());
    }
}
