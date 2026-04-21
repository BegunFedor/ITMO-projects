package prog.lab3.objects;

import prog.lab3.enums.LightLevel;

public class Match {
    private boolean used;

    public Match() {
        this.used = false;
    }

    public void ignite() {
        if (used) {
            System.out.println("Спичка уже была использована и больше не может быть зажжена.");
        } else {
            System.out.println("Спичка зажжена. Появилось пламя!");
            this.used = true;
        }
    }

    public boolean isUsed() {
        return used;
    }

    public String getDescription() {
        return "Спичка"+ ", состояние: " + (used ? "использована" : "новая");
    }
}

