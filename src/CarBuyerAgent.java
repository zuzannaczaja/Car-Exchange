import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

public class CarBuyerAgent extends Agent {
    private String targetBookTitle;
    protected void setup() {
        // Printout a welcome message
        System.out.println("Hello! Buyer-agent "+getAID().getName()+" is ready.");

        // Get the title of the book to buy as a start-up argument
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            targetBookTitle = (String) args[0];
            System.out.println("Trying to buy "+targetBookTitle);
            // Add a TickerBehaviour that schedules a request to seller agents every minute
            addBehaviour(new TickerBehaviour(this, 60000) {
                protected void onTick() {
                    myAgent.addBehaviour(new RequestPerformer());
                }
            } );
        }
        else {
            // Make the agent terminate
            System.out.println("No target book title specified");
            doDelete();
        }
    }

}
