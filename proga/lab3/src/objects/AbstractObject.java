package objects;


import enums.LightLevel;

public abstract class AbstractObject {
    private final String name;
    private final LightLevel lightLevel;



    public AbstractObject(String name, LightLevel lightLevel) {
        this.name = name;
        this.lightLevel = lightLevel;
    }

    public String getName() {
        return name;
    }

    public LightLevel getLightLevel() {
        return lightLevel;
    }


    public abstract String getDescription();
}
