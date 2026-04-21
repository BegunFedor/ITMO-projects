package main.Pokemons;

import main.PhysicalMoves.LeafBlade;
import main.PhysicalMoves.Slam;
import main.StatusMoves.Confide;
import main.StatusMoves.SwordsDance;
import ru.ifmo.se.pokemon.Pokemon;
import ru.ifmo.se.pokemon.Type;




public final class Tropius extends Pokemon {
    public Tropius (String name, int i) {
        super(name, i);
        super.setType(Type.GRASS,Type.FLYING);
        super.setStats(99, 68, 83, 72, 87, 51);
        super.setMove(new SwordsDance(), new LeafBlade(), new Confide(), new Slam());
    }
}
