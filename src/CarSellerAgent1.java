import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CarSellerAgent1 extends Agent {

    private MessageTemplate template = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
            MessageTemplate.MatchOntology("Kupno") );

    protected void setup() {

        // Printout a welcome message
        System.out.println("Hello! Seller-agent "+getAID().getName()+" is ready.");

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = myAgent.receive(template);
                if (msg != null) {
                    // jest wiadomosc
                    System.out.println("Received QUERY_IF message from agent "+msg.getSender().getName());
                    // przygotuj odpowiedz
                    ACLMessage reply = msg.createReply();
                    if ("Chcę kupić".equals(msg.getContent())) {
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("Chcę kupić");
                    }
                    else {
                        reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                        reply.setContent("Unknown-content");
                    }
                    myAgent.send(reply);
                } else {
                    block();
                }
            }
        } );
    }

    protected void takeDown() {
        // operacje wykonywane bezpośrednio przed usunięciem agenta
    }
}