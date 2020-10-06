import jade.core.Agent;

public class CarSellerAgent1 extends Agent {
    protected void setup() {
        // Printout a welcome message
        System.out.println("Hello! Seller-agent "+getAID().getName()+" is ready.");
    }

    protected void takeDown() {
        // operacje wykonywane bezpośrednio przed usunięciem agenta
    }
}