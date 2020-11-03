import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CarBuyerAgent extends Agent {

    private String targetCar;
    private AID[] sellerAgents;

    protected void setup() {

        System.out.println("Witaj świecie! Agent kupujący "+getAID().getName()+" jest gotowy.");

        Object[] args = getArguments();
        if (args != null && args.length > 0) {

            targetCar = (String) args[0] + " " + (String) args[1];
            System.out.println("Poszukiwany samochód to: "+ targetCar);

            addBehaviour(new TickerBehaviour(this, 10000) {
                protected void onTick() {
                    System.out.println("Podejmuję próbę kupna "+ targetCar);
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("car-selling");
                    template.addServices(sd);
                    try {
                        DFAgentDescription[] result = DFService.search(myAgent, template);
                        System.out.println("Wykryto następujących sprzedających:");
                        sellerAgents = new AID[result.length];
                        for (int i = 0; i < result.length; ++i) {
                            sellerAgents[i] = result[i].getName();
                            //System.out.println(sellerAgents[i].getName());
                        }
                    }
                    catch (FIPAException fe) {
                        fe.printStackTrace();
                    }
                    myAgent.addBehaviour(new RequestPerformer());
                }
            } );
        } else {
            System.out.println("Agent kupujący nie ma określonego samochodu!");
            doDelete();
        }
    }

    protected void takeDown() {
        System.out.println("Agent kupujący "+getAID().getName()+" kończy działanie.");
    }

    private class RequestPerformer extends Behaviour {
        private AID bestSeller;
        private int bestPrice;
        private int repliesCnt = 0;
        private MessageTemplate mt;
        private int step = 0;
        Object[] args = getArguments();

        public void action() {
            switch (step) {
                case 0:
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < sellerAgents.length; ++i) {
                        cfp.addReceiver(sellerAgents[i]);
                    }
                    cfp.setContent(targetCar);
                    cfp.setConversationId("car-trade");
                    cfp.setReplyWith("cfp"+System.currentTimeMillis());
                    myAgent.send(cfp);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("car-trade"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            int price = Integer.parseInt(reply.getContent());
                            if ((bestSeller == null || price < bestPrice) && price <= Integer.parseInt((String) args[2])) {
                                bestPrice = price;
                                bestSeller = reply.getSender();
                            }
                            if(price > Integer.parseInt((String) args[2])){
                                System.out.println("Nieudana próba kupna: Budżet kupującego jest zbyt niski.");
                            }
                        }
                        repliesCnt++;
                        if (repliesCnt >= sellerAgents.length) {
                            step = 2;
                        }
                    }
                    else {
                        block();
                    }
                    break;
                case 2:
                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    order.addReceiver(bestSeller);
                    order.setContent(targetCar);
                    order.setConversationId("car-trade");
                    order.setReplyWith("order"+System.currentTimeMillis());
                    myAgent.send(order);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("car-trade"),
                            MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                    step = 3;
                    break;
                case 3:
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            System.out.println(targetCar +" został pomyślnie kupiony od: "+reply.getSender().getName());
                            System.out.println("Cena = "+bestPrice);
                            int budget = Integer.parseInt((String) args[2]) - bestPrice;
                            args[2] = Integer.toString(budget);
                            System.out.println("Budżet " + getAID().getName() + " wynosi teraz: " + args[2]);
                            myAgent.doDelete();
                        }
                        else {
                            System.out.println("Nieudana próba kupna: wybrany samochód jest już sprzedany.");
                        }
                        step = 4;
                    }
                    else {
                        block();
                    }
                    break;
            }
        }

        public boolean done() {
            if (step == 2 && bestSeller == null) {
                System.out.println("Nieudana próba kupna: "+ targetCar +" nie jest dostępny na sprzedaż");
            }
            return ((step == 2 && bestSeller == null) || step == 4);
        }
    }
}

