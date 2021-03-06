import jade.Boot;
import jade.core.Agent;

import java.util.Random;

public class StartCarExchange extends Agent {
    //region Ilość agentów
    private static final int BUYERS_COUNT = 3;
    private static final int SELLERS_COUNT = 10;
    private static final int SELLERS_CARS = 8;
    private static final int BUYERS_CARS = 3;
    //endregion
    //region Nazwy agentów
    private static final String SELLER_NAME = "Seller";
    private static final String BUYER_NAME = "Buyer";
    private static final String SELLER_PACKGAE = ":CarSellerAgentNoGui";
    private static final String BUYER_PACKGAE = ":CarBuyerAgentNoGui";
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

    public static void main(String[] args) {
        StringBuilder agentList = new StringBuilder();
        agentList.append(createSellerAgents());
        agentList.append(createBuyerAgents());

        BootFromContainer(agentList);
    }

    private static StringBuilder createSellerAgents() {
        StringBuilder agentList = new StringBuilder();
        System.out.println("SELLERS: ");
        for (int i = 0; i < SELLERS_COUNT; i++) {
            agentList.append(SELLER_NAME);
            agentList.append(i + 1);
            agentList.append(SELLER_PACKGAE + "(");
            for (int y = 0; y < SELLERS_CARS; y++) {
                agentList.append(generateCars(carList, true));
                agentList.append(generateCars(bodyTypeList, true));
                agentList.append(generateCars(engineTypeList, true));
                agentList.append(generateRandomFloat(ENGINE_CAPACITY_MIN, ENGINE_CAPACITY_MAX) + "-");
                agentList.append(generateRandomInt(YEAR_OF_PRODUCTION_MIN, YEAR_OF_PRODUCTION_MAX) + "-");
                agentList.append(generateRandomInt(BASE_PRICE_MIN, BASE_PRICE_MAX) + "-");
                agentList.append(generateRandomInt(ADDITIONAL_COSTS_MIN, ADDITIONAL_COSTS_MAX));
                if(y + 1 != SELLERS_CARS)
                    agentList.append(",");
            }
            agentList.append(");");
        }
        String temp = String.valueOf(agentList);
        System.out.println(temp.replace(";", "\n"));
        return agentList;
    }

    private static StringBuilder createBuyerAgents() {
        StringBuilder agentList = new StringBuilder();
        System.out.println("BUYERS: ");
        for (int i = 0; i < BUYERS_COUNT; i++) {
            agentList.append(BUYER_NAME);
            agentList.append(i + 1);
            agentList.append(BUYER_PACKGAE + "(");
            for (int y = 0; y < BUYERS_CARS; y++) {
                agentList.append(generateCars(carList, false));
                //agentList.append(100000);
                if(y + 1 != BUYERS_CARS)
                    agentList.append(",");
            }
            agentList.append(");");
        }

        System.out.println(agentList);
        //String temp = String.valueOf(agentList);
        //System.out.println(temp.replace(";", "\n"));
        return agentList;
    }

    private static void BootFromContainer(StringBuilder agentList) {
        String[] container = {
                "-gui",
                "-local-host 127.0.0.1",
                "-container",
                agentList.toString()
        };
        Boot.main(container);
    }

    private static String generateCars(String[] list, boolean dash) {
        Random r = new Random();
        int id = r.nextInt(list.length);
        if (list[id].contains("|")) {
            System.out.println(list[id]);
            if (dash)
                return list[id].replace("|", "-") + "-";
            else return list[id].replace("|", " ");
        }
        else if (!list[id].contains("|") && dash)
            return list[id] + "-";
        else
            return list[id];
    }

    private static String generateRandomFloat(float min, float max) {
        Random r = new Random();
        float randomFloat = min + r.nextFloat() * (max - min);
        int scale = (int) Math.pow(10, 1);
        float roundedFloat = ((float) Math.round(randomFloat * scale) / scale);
        return String.valueOf(roundedFloat);
    }

    private static String generateRandomInt(int min, int max) {
        Random r = new Random();
        int randomInt = r.nextInt(max - min + 1) + min;
        return String.valueOf(randomInt);
    }
}
