import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;

public class CarBuyerAgent1 extends Agent {
    // The title of the book to buy
    private String targetBookTitle;
    // The list of known seller agents
    private AID[] sellerAgents = {new AID("seller1", AID.ISLOCALNAME), new AID("seller2", AID.ISLOCALNAME)};

    // Put agent initializations here
    protected void setup() {
        // Printout a welcome message
        System.out.println("Hello! Buyer-agent" + getAID().getName() + " is ready.");

        // Get the title of the book to buy as a start-up argument
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            targetBookTitle = (String) args[0];
            System.out.println("Trying to buy " + targetBookTitle);
        }
        else {
            // Make the agent terminate immediately
            System.out.println("No book title specified");
            doDelete();
        }

        addBehaviour(new TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {
                System.out.println("Agent " + myAgent.getLocalName() + ": tick=" + getTickCount());
            }
        });

        addBehaviour(new WakerBehaviour(this, 10000) {
            protected void handleElapsedTimeout(){
                System.out.println("Agent " + myAgent.getLocalName() + ": It's wakeup-time. Bye...");
                myAgent.doDelete();
            }
        });
    }

    // Put agent clean-up operations here
    protected void takeDown() {
        // Printout a dismissal message
        System.out.println("Buyer-agent "+getAID().getName()+" terminating.");
    }
}
