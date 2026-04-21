package main.Pokemons;
import main.StatusMoves.*;
import ru.ifmo.se.pokemon.*;


public final class Swadloon extends Sewaddle {
    public Swadloon(String name, int i) {
        super(name, i);
        super.setType(Type.BUG,Type.GRASS);
        super.setStats(55,
                63, 90, 50, 80, 42);
        super.setMove(new Rest(), new StringShot(), new GrassWhistle());
    }
}
