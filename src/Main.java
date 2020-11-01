import jade.Boot;
import jade.core.Agent;

public class Main extends Agent {
    public static void main(String[] args){
        String carExample = "BMW";
        int buyerAmount = 3;
        int sellerAmount = 10;
        String agentList = "";

        for (int i = 0; i < buyerAmount; i++){
            agentList += "CarBuyer" + (i+1) + ":CarBuyerAgent(" + carExample + ");";
        }

        for (int i = 0; i < sellerAmount; i++){
            agentList += "CarSeller" + (i+1) + ":CarSellerAgentNoGui;";
        }

        String[] container = {
                "-gui",
                "-local-host 127.0.0.1",
                "-container",
                agentList
        };
        Boot.main(container);
    }
}
