import actions.*;
import characters.*;
import enums.*;
import objects.*;
import exceptions.*;
import java.util.ArrayList;
import java.util.List;


public class Main {
    public static void main(String[] args) {
        Coordinates roofCoordinates = new Coordinates(5, 10);
        Coordinates houseCoordinates = new Coordinates(15, 25);
        Coordinates roomCornerCoordinates = new Coordinates(10,15);
        Coordinates streetCoordinates = new Coordinates(10,15);

        try {
            Roof roof = new Roof("Крыша", LightLevel.BRIGHT, roofCoordinates);
            House house = new House("Дом", LightLevel.DIM, houseCoordinates);
            Jewel jewel = new Jewel("Драгоценности", "Сейф");
            TrashBin trashBin = new TrashBin("Мусорное ведро",Fullness.HALF_FULL, roomCornerCoordinates);
            Street street = new Street("Улица", LightLevel.DIM,streetCoordinates);
            EveningScene eveningScene = new EveningScene("Вечерняя сцена", LightLevel.DARK, streetCoordinates);
            Match match = new Match();


            MisterSmith smith = new MisterSmith("Мистер Смит", EmotionalState.NERVOUS, house);
            Karlson karlson = new Karlson("Карлсон", EmotionalState.HAPPY, roof);
            Malysh malysh = new Malysh("Малыш", EmotionalState.HAPPY, roof);
            ElegantGentleman gentleman = new ElegantGentleman("Элегентный джентельмен", EmotionalState.ANGRY,house);
            Police police = new Police(house);
            Mother mother = new Mother("Мама", EmotionalState.HAPPY, house);



            List<Describable> describables = new ArrayList<>();
            describables.add(roof);
            describables.add(house);
            describables.add(jewel);
            describables.add(street);






            for (Describable describable : describables) {
                System.out.println(describable.getDescription());
            }
            for (Describable describable : describables) {
                System.out.println(describable.getDescription());
            }

            try {
                FireAction fireAction = new FireAction(malysh, match);
                fireAction.execute();
                match.ignite();
                malysh.performAction();
                CherryEatingAction cherryEatingAction = new CherryEatingAction(karlson,malysh);
                cherryEatingAction.execute();
                roof.addResident(karlson);
                roof.addResident(malysh);
                GarbageFlowAction garbageAction = new GarbageFlowAction(trashBin, malysh);
                garbageAction.execute();
                System.out.println("Описание объектов:");
                ObservationAction observation1 = new ObservationAction(karlson, smith);
                ObservationAction observation2 = new ObservationAction(malysh, smith);

                observation1.execute();
                observation2.execute();

                house.addResident(smith);
                street.addResident(gentleman);
                karlson.performAction();

                ReactToGarbageAction reactToGarbageAction = new ReactToGarbageAction(gentleman);
                reactToGarbageAction.execute();
                gentleman.performAction();
                smith.performAction();
                ShakeOffDustAction shakeDust = new ShakeOffDustAction(smith);
                shakeDust.execute();
                RejectAction rejectAction = new RejectAction(malysh);
                rejectAction.execute();
                malysh.performAction();
                CleanAction cleanAction = new CleanAction(malysh, trashBin);
                cleanAction.execute();
                ThrowGarbageAction throwGarbage = new ThrowGarbageAction(karlson, trashBin);
                throwGarbage.execute();
                SlideDownAction slideAction = new SlideDownAction(karlson, roof);
                slideAction.execute();


                try {
                    StealAction stealAction = new StealAction(smith, jewel);
                    stealAction.execute(); // Повторная попытка украсть тот же предмет, вызывает UncheckedException
                } catch (UncheckedException e) {
                    System.out.println("Ошибка при краже: " + e.getMessage());
                }

                CallPoliceAction callPolice = new CallPoliceAction(malysh);
                callPolice.execute();

                ArrestAction arrest = new ArrestAction(police, smith);
                arrest.execute();
                police.performAction();

                try {
                    GoHomeAction goHomeKarlson = new GoHomeAction(karlson);
                    goHomeKarlson.execute();

                    GoHomeAction goHomeMalysh = new GoHomeAction(malysh);
                    goHomeMalysh.execute();

                } catch (IllegalActionException e) {
                    System.out.println(e.getMessage());
                }
                eveningScene.addResident(karlson);
                eveningScene.addResident(malysh);
                eveningScene.addResident(mother);
                eveningScene.getDescription();
                eveningScene.describeWindows();
                GoHomeAction goHomeMalysh = new GoHomeAction(malysh);
                goHomeMalysh.execute();
                VisitDoctorAction visitDoctorAction = new VisitDoctorAction(mother);
                visitDoctorAction.execute();
                mother.performAction();
                LieAction lieAction = new LieAction(malysh);
                lieAction.execute();

            } catch (IllegalActionException | MissingItemException e) {
                System.out.println("Ошибка в процессе выполнения действий: " + e.getMessage());
            }



        } catch (Exception e) {
            System.out.println("Произошла непредвиденная ошибка: " + e.getMessage());
        }
    }
}
