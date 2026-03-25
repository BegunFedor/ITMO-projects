package prog.lab3.objects;

import prog.lab3.enums.Fullness;

public abstract class FullnessAbstractObject {
    private final String name;
    private Fullness fullness;



    public FullnessAbstractObject(String name, Fullness fullness) {
        this.name = name;
        this.fullness = fullness;
    }

    public String getName() {
        return name;
    }

    public void setFullness(Fullness fullness) {
        this.fullness = fullness;
    }

    public Fullness getFullness() {
        return fullness;
    }

    public abstract String getDescription();
}
