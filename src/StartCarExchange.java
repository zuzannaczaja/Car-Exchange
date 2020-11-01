import jade.Boot;
import jade.core.Agent;

import java.util.Random;

public class StartCarExchange extends Agent {
    //region Ilość agentów
    private static final int BUYERS_COUNT = 2;
    private static final int SELLERS_COUNT = 1;
    private static final int SELLERS_CARS = 1;
    private static final int BUYERS_CARS = 1;
    //endregion
    //region Nazwy agentów
    private static final String SELLER_NAME = "Seller";
    private static final String BUYER_NAME = "Buyer";
    private static final String SELLER_PACKGAE = ":CarSellerAgentNoGui";
    private static final String BUYER_PACKGAE = ":CarBuyerAgent";
    //endregion
    //region Listy i wartości
    private static final String[] carList = {
            "Renault|Clio", "Renault|Megane",
            "Mazda|2", "Mazda|3", "Mazda|6",
            "Audi|A4", "Audi|A6", "Audi|A8",
            "Citroen|C3", "Citroen|C4",
            "Dacia|Duster", "Dacia|Logan",
            "Hyundai|i20", "Hyundai|i30"
    };
    private static final String[] bodyTypeList = {
            "Sedan", "Hatchback", "SUV", "Cabrio"
    };
    private static final String[] engineTypeList = {
            "Gasoline", "Diesel", "LPG", "Electric", "Hybrid"
    };
    private static final float engineCapacityMin = 0.9f;
    private static final float engineCapacityMax = 5.0f;
    private static final int yearOfProductionMin = 1990;
    private static final int yearOfProductionMax = 2020;
    //endregion

    public static void main(String[] args){
        StringBuilder agentList = new StringBuilder();
        agentList.append(createSellerAgents());
        agentList.append(createBuyerAgents());

        BootFromContainer(agentList);
    }

    private static StringBuilder createSellerAgents(){
        StringBuilder agentList = new StringBuilder();
        for (int i = 0; i < SELLERS_COUNT; i++){
            agentList.append(SELLER_NAME);
            agentList.append(i + 1);
            agentList.append(SELLER_PACKGAE + "(");
            agentList.append(generateCars(carList));
            agentList.append(generateCars(bodyTypeList));
            agentList.append(generateCars(engineTypeList));
            agentList.append(generateEngineCapacity(engineCapacityMin, engineCapacityMax));
            agentList.append(generateYearOfProduction(yearOfProductionMin, yearOfProductionMax));
            agentList.append("19000, 500");
            agentList.append(");");
        }
        System.out.println("SPRZEDAJACY: " + agentList);
        return agentList;
    }

    private static StringBuilder createBuyerAgents(){
        StringBuilder agentList = new StringBuilder();
        for (int i = 0; i < BUYERS_COUNT; i++){
            agentList.append(BUYER_NAME);
            agentList.append(i + 1);
            agentList.append(BUYER_PACKGAE + "(");
            agentList.append(generateCars(carList));
            agentList.append(100000);
            agentList.append(");");
        }
        System.out.println("KUPUJACY: " + agentList);
        return agentList;
    }

    private static void BootFromContainer(StringBuilder agentList){
        String[] container = {
                "-gui",
                "-local-host 127.0.0.1",
                "-container",
                agentList.toString()
        };
        Boot.main(container);
    }

    private static String generateCars(String[] list){
        Random r=new Random();
        int id = r.nextInt(list.length);
        if(list[id].contains("|"))
            return list[id].replace("|", ", ") + ", ";
        else
            return list[id] + ", ";
    }

    private static String generateEngineCapacity(float min, float max){
        Random r=new Random();
        float randomFloat = min + r.nextFloat() * (max - min);
        int scale = (int) Math.pow(10, 1);
        float roundedFloat = ((float)Math.round(randomFloat * scale) / scale);
        return roundedFloat + ", ";
    }

    private static String generateYearOfProduction(int min, int max){
        Random r=new Random();
        int randomInt = r.nextInt(max-min+1)+min;
        return randomInt + ", ";
    }
}
