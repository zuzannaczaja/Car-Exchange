import jade.Boot;
import jade.core.Agent;

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
    private static final String SELLER_PACKGAE = ":CarSellerAgent";
    private static final String BUYER_PACKGAE = ":CarBuyerAgent";
    //endregion
    //region Lista samochodów
    private static String[] carList = {
            "Renault|Clio", "Renault|Megane",
            "Mazda|2", "Mazda|3", "Mazda|6",
            "Audi|A4", "Audi|A6", "Audi|A8",
            "Citroen|C3", "Citroen|C4",
            "Dacia|Duster", "Dacia|Logan",
            "Hyundai|i20", "Hyundai|i30"
    };
    //endregion

    public static void main(String[] args){
        String carExample = "BMW";
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
            agentList.append(SELLER_PACKGAE);
            agentList.append(";");
        }
        return agentList;
    }

    private static StringBuilder createBuyerAgents(){
        StringBuilder agentList = new StringBuilder();
        for (int i = 0; i < BUYERS_COUNT; i++){
            agentList.append(BUYER_NAME);
            agentList.append(i + 1);
            agentList.append(BUYER_PACKGAE + "(");
            agentList.append(generateBuyerCars());
            agentList.append(");");
        }
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

    private static String generateBuyerCars(){
        String temp = "RENAULT";
        return temp;
    }
}
