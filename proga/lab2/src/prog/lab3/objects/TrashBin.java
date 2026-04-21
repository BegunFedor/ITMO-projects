package prog.lab3.objects;


import prog.lab3.enums.Fullness;


public class TrashBin extends FullnessAbstractObject implements Describable {
    private boolean hasTrash;
    private String location;
    private final Coordinates coordinates;


    public TrashBin(String name, Fullness fullness, Coordinates coordinates) {
        super(name, fullness);
        this.coordinates = coordinates;
        this.hasTrash = false;
    }


    public void addTrash() {
        this.hasTrash = true;
        System.out.println("Мусор добавлен в " + getName() + ".");
    }

    public boolean isFull() {
        return hasTrash;
    }
    @Override
    public String getDescription() {
        return getName() + " заполнен на: " + getFullness();
    }
    @Override
    public String toString() {
        return "Jewel{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", location='" + "в углу комнаты" + '\'' +
                ", occupancy level=" + getClass() +
                '}';}}
