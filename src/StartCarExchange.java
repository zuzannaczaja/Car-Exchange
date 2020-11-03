import jade.Boot;
import jade.core.Agent;

import java.util.Random;

public class StartCarExchange extends Agent {
    //region Ilość agentów
    private static final int BUYERS_COUNT = 4;
    private static final int SELLERS_COUNT = 10;
    private static final int SELLERS_CARS = 2;
    private static final int BUYERS_CARS = 2;
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
            "Gasoline", "Diesel", "LPG", "Hybrid", //"Electric"
    };
    private static final float ENGINE_CAPACITY_MIN = 0.9f;
    private static final float ENGINE_CAPACITY_MAX = 5.0f;
    private static final int YEAR_OF_PRODUCTION_MIN = 1990;
    private static final int YEAR_OF_PRODUCTION_MAX = 2020;
    private static final int BASE_PRICE_MIN = 2000;
    private static final int BASE_PRICE_MAX = 100000;
    private static final int ADDITIONAL_COSTS_MIN = 0;
    private static final int ADDITIONAL_COSTS_MAX = 10000;
    //endregion

    public static void main(String[] args){
        StringBuilder agentList = new StringBuilder();
        agentList.append(createSellerAgents());
        agentList.append(createBuyerAgents());

        BootFromContainer(agentList);
    }

    private static StringBuilder createSellerAgents(){
        StringBuilder agentList = new StringBuilder();
        System.out.println("SELLERS: ");
        for (int i = 0; i < SELLERS_COUNT; i++){
            for (int y = 0; y < SELLERS_CARS; y++){
                agentList.append(SELLER_NAME);
                agentList.append(i + 1);
                //agentList.append(SELLER_PACKGAE + ";");
                agentList.append(SELLER_PACKGAE + "(");
                agentList.append(generateCars(carList));
                agentList.append(generateCars(bodyTypeList));
                agentList.append(generateCars(engineTypeList));
                agentList.append(generateRandomFloat(ENGINE_CAPACITY_MIN, ENGINE_CAPACITY_MAX));
                agentList.append(generateRandomInt(YEAR_OF_PRODUCTION_MIN, YEAR_OF_PRODUCTION_MAX, true));
                agentList.append(generateRandomInt(BASE_PRICE_MIN, BASE_PRICE_MAX, true));
                agentList.append(generateRandomInt(ADDITIONAL_COSTS_MIN, ADDITIONAL_COSTS_MAX, false));
                agentList.append(");");
            }
        }
        String temp = String.valueOf(agentList);
        System.out.println(temp.replace(";", "\n"));
        return agentList;
    }

    private static StringBuilder createBuyerAgents(){
        StringBuilder agentList = new StringBuilder();
        System.out.println("BUYERS: ");
        for (int i = 0; i < BUYERS_COUNT; i++){
            for (int y = 0; y < BUYERS_CARS; y++){
                agentList.append(BUYER_NAME);
                agentList.append(i + 1);
                agentList.append(BUYER_PACKGAE + "(");
                agentList.append(generateCars(carList));
                agentList.append(100000);
                agentList.append(");");
            }
        }
        String temp = String.valueOf(agentList);
        System.out.println(temp.replace(";", "\n"));
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

    private static String generateRandomFloat(float min, float max){
        Random r = new Random();
        float randomFloat = min + r.nextFloat() * (max - min);
        int scale = (int) Math.pow(10, 1);
        float roundedFloat = ((float)Math.round(randomFloat * scale) / scale);
        return roundedFloat + ", ";
    }

    private static String generateRandomInt(int min, int max, boolean coma){
        Random r = new Random();
        int randomInt = r.nextInt(max-min+1)+min;
        if(coma)
            return randomInt + ", ";
        else
            return String.valueOf(randomInt);
    }
}
