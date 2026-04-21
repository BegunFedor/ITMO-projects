package main;

import main.Pokemons.*;
import ru.ifmo.se.pokemon.*;


public class Main {
    public static void main(String[] args) {
        Leavanny p6 = new Leavanny("Левани", 96);
        Sewaddle p1 = new Sewaddle("Севадл", 74);
        Quagsire p5= new Quagsire("Квагсаер", 55);
        Swadloon p2 = new Swadloon("Свадлун", 93);
        Tropius p4 = new Tropius("Тропиус", 51);
        Wooper p3 = new Wooper("Вупер", 69);
        Battle b = new Battle();
        b.addAlly(p1);
        b.addAlly(p2);
        b.addAlly(p3);
        b.addFoe(p4);
        b.addFoe(p5);
        b.addFoe(p6);
        b.go();
    }
}