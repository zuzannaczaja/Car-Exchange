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
import java.util.Random;

public class CarBuyerAgent extends Agent {

    private String targetCar;
    private AID[] sellerAgents;
    public static HashMap<String, Integer> allCars = new HashMap<>();
    public List<String> wantedCarsBuyer = new ArrayList<>();
    int carIndex;
    int budgetBuyer = 100000;
    private CarBuyerGui myGui;
    public boolean isDelayed = true;

    protected void setup() {

        myGui = new CarBuyerGui(this);
        myGui.showGui();

        System.out.println("Witaj świecie! Agent kupujący " + getAID().getName() + " jest gotowy.");

            allCars.put(getAID().getLocalName(), wantedCarsBuyer.size());

            addBehaviour(new TickerBehaviour(this, 5000) {
                protected void onTick() {
                    Random random = new Random();

                    if(allCars.get(getAID().getLocalName()) == 1){
                        carIndex = 0;
                    } else {
                        carIndex = random.nextInt(allCars.get(getAID().getLocalName()) - 1);
                    }

                    targetCar = wantedCarsBuyer.get(carIndex);
                    System.out.println("Poszukiwany samochód to: " + targetCar);

                    System.out.println("Podejmuję próbę kupna " + targetCar);
                    DFAgentDescription dfAgentDescription = new DFAgentDescription();
                    ServiceDescription serviceDescription = new ServiceDescription();
                    serviceDescription.setType("car-selling");
                    dfAgentDescription.addServices(serviceDescription);
                    try {
                        DFAgentDescription[] result = DFService.search(myAgent, dfAgentDescription);
                        //System.out.println("Wykryto następujących sprzedających:");
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
    }

    protected void takeDown() {
        System.out.println("Agent kupujący " + getAID().getName() + " kończy działanie.");
        myGui.dispose();
    }

    public void updateData(String brandAndModel, String reservation){

        wantedCarsBuyer.add(brandAndModel);
        System.out.println(wantedCarsBuyer.size());
        allCars.put(getAID().getLocalName(), wantedCarsBuyer.size());
        System.out.println(getAID().getLocalName());
        System.out.println(allCars.get(getAID().getLocalName()));


        if(reservation == "yes" || reservation == "Yes"){
            isDelayed = true;
        } else if(reservation == "no" || reservation == "No"){
            isDelayed = false;
        }

    }

    private class RequestPerformer extends Behaviour {
        private AID bestSeller;
        private int bestPrice;
        private int repliesCount = 0;
        private MessageTemplate messageTemplate;
        private int step = 0;

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
                    Random random = new Random();

                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    if(isDelayed){
                        System.out.println(getAID().getLocalName() + " prosi o rezerwację samochodu.");
                        order = new ACLMessage(ACLMessage.UNKNOWN);
                    }
                    //ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
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

                            wantedCarsBuyer.remove(carIndex);
                            allCars.computeIfPresent(getAID().getLocalName(), (k, cars) -> cars - 1);

                            if(budgetBuyer <= 0){
                                doDelete();
                                System.out.println(getAID().getName() + " jest usuwany, bo jego budżet jest równy 0.");
                            }

                            if(allCars.get(getAID().getLocalName()) <= 0){
                                doDelete();
                                System.out.println(getAID().getName() + " jest usuwany, bo kupił wszystkie samochody.");
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