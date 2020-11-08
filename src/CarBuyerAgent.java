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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CarBuyerAgent extends Agent {

    private String targetCar;
    private AID[] sellerAgents;
    public static HashMap<String, Integer> allCars = new HashMap<>();
    private List<String> wantedCarsBuyer = new ArrayList<>();
    int carIndex;
    int budgetBuyer;

    protected void setup() {

        System.out.println("Witaj świecie! Agent kupujący " + getAID().getName() + " jest gotowy.");

        Object[] args = getArguments();
        if (args != null && args.length > 0) {

            int counter = 1;

            for(Object car : args){
                String carName = car.toString().trim();

                System.out.println(" > " + counter + ".) " + carName);
                wantedCarsBuyer.add(carName);
                counter++;

            }

            allCars.put(getAID().getLocalName(), wantedCarsBuyer.size());

            carIndex = CarBuyerAgent.allCars.get(getAID().getLocalName()) - 1;

            targetCar = wantedCarsBuyer.get(carIndex);
            System.out.println("Poszukiwany samochód to: " + targetCar);

            addBehaviour(new TickerBehaviour(this, 10000) {
                protected void onTick() {
                    System.out.println("Podejmuję próbę kupna " + targetCar);
                    DFAgentDescription dfAgentDescription = new DFAgentDescription();
                    ServiceDescription serviceDescription = new ServiceDescription();
                    serviceDescription.setType("car-selling");
                    dfAgentDescription.addServices(serviceDescription);
                    try {
                        DFAgentDescription[] result = DFService.search(myAgent, dfAgentDescription);
                        System.out.println("Wykryto następujących sprzedających:");
                        sellerAgents = new AID[result.length];
                        for (int i = 0; i < result.length; ++i) {
                            sellerAgents[i] = result[i].getName();
                            //System.out.println(sellerAgents[i].getName());
                        }
                    } catch (FIPAException fe) {
                        fe.printStackTrace();
                    }
                    myAgent.addBehaviour(new RequestPerformer());
                }
            });
        } else {
            System.out.println("Agent kupujący nie ma określonego samochodu!");
            //doDelete();
        }
    }

    protected void takeDown() {
        System.out.println("Agent kupujący " + getAID().getName() + " kończy działanie.");
    }

    private class RequestPerformer extends Behaviour {
        private AID bestSeller;
        private int bestPrice;
        private int repliesCount = 0;
        private MessageTemplate messageTemplate;
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
                    cfp.setReplyWith("cfp" + System.currentTimeMillis());
                    myAgent.send(cfp);
                    messageTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId("car-trade"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    ACLMessage reply = myAgent.receive(messageTemplate);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            int price = Integer.parseInt(reply.getContent());
                            if ((bestSeller == null || price < bestPrice) && price <= budgetBuyer) {
                                bestPrice = price;
                                bestSeller = reply.getSender();
                            }
                            if (price > budgetBuyer) {
                                System.out.println(price + "   " + budgetBuyer);
                                System.out.println("Nieudana próba kupna: Budżet kupującego jest zbyt niski.");
                            }
                        }
                        repliesCount++;
                        if (repliesCount >= sellerAgents.length) {
                            step = 2;
                        }
                    } else {
                        block();
                    }
                    break;
                case 2:
                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    order.addReceiver(bestSeller);
                    order.setContent(targetCar);
                    order.setConversationId("car-trade");
                    order.setReplyWith("order" + System.currentTimeMillis());
                    myAgent.send(order);
                    messageTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId("car-trade"),
                            MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                    step = 3;
                    break;
                case 3:
                    reply = myAgent.receive(messageTemplate);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            System.out.println(targetCar + " został pomyślnie kupiony od: " + reply.getSender().getName());
                            System.out.println("Cena = " + bestPrice);
                            budgetBuyer  = budgetBuyer - bestPrice;
                            System.out.println("Budżet " + getAID().getName() + " wynosi teraz: " + budgetBuyer);

                            if(CarBuyerAgent.allCars.get(getAID().getLocalName()) <= 0 || budgetBuyer <= 0){
                                wantedCarsBuyer.remove(carIndex);
                                allCars.computeIfPresent(getAID().getLocalName(), (k, cars) -> cars - 1);
                                doDelete();
                            }

                        } else {
                            System.out.println("Nieudana próba kupna: wybrany samochód jest już sprzedany.");
                        }
                        step = 4;
                    } else {
                        block();
                    }
                    break;
            }
        }

        public boolean done() {
            if (step == 2 && bestSeller == null) {
                System.out.println("Nieudana próba kupna: " + targetCar + " nie jest dostępny na sprzedaż");
            }
            return ((step == 2 && bestSeller == null) || step == 4);
        }
    }
}

