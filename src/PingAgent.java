import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
/**
 @author Giovanni Caire - TILAB
 */
public class PingAgent extends Agent {
    // okresl parametry wiadomosci
    private MessageTemplate template2 = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF),
            MessageTemplate.MatchOntology("presence") );
    protected void setup() {

        // utworzenie opisu agenta
        DFAgentDescription dfad = new DFAgentDescription();
        dfad.setName(getAID());
        // utworzenie opisu usługi
        ServiceDescription sd = new ServiceDescription();
        sd.setType("OBLICZENIA");
        sd.setName("Oblicz_PI");
        dfad.addServices(sd);
        try {
            DFService.register(this, dfad);
        }
        catch (FIPAException ex) {

        }

        // przygotowanie wzorca wyszukiwania
        DFAgentDescription template = new DFAgentDescription();
        // przygotowanie opisu usługi do wyszukiwania
        ServiceDescription sd2 = new ServiceDescription();
        sd2.setType("OBLICZENIA");
        template.addServices(sd);
        try {
            DFAgentDescription[] listaAgentow =
                    DFService.search(this, template);
                    System.out.println("Lista agentów:" + listaAgentow.length);

        // przetwarzanie listy agentów
        }
        catch (FIPAException ex) {

        }

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = myAgent.receive(template2);
                if (msg != null) {
                    // jest wiadomosc
                    System.out.println("Received QUERY_IF message from agent "+msg.getSender().getName());
                    // przygotuj odpowiedz
                    ACLMessage reply = msg.createReply();
                    if ("alive".equals(msg.getContent())) {
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("alive");
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
}