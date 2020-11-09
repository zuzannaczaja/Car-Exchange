import jade.core.Agent;
import jade.core.behaviours.*;	
import jade.lang.acl.ACLMessage;	
import jade.lang.acl.MessageTemplate;															 
import jade.domain.DFService;	
import jade.domain.FIPAException;							 
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;


import java.util.*;

public class CarSellerAgent extends Agent {

    public Hashtable carCatalogue;
    private CarSellerGui myGui;

    protected void setup() {
        carCatalogue = new Hashtable();
        myGui = new CarSellerGui(this);
        myGui.showGui();

        DFAgentDescription dfAgentDescription = new DFAgentDescription();
        dfAgentDescription.setName(getAID());
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("car-selling");
        serviceDescription.setName("JADE-car-trading");
        dfAgentDescription.addServices(serviceDescription);
        try {
            DFService.register(this, dfAgentDescription);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new OfferRequestsServer());
        addBehaviour(new PurchaseOrdersServer());
        addBehaviour(new DelayedPurchaseOrdersServer());
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        myGui.dispose();
        System.out.println("Agent sprzedający "+getAID().getName()+" kończy działanie.");
    }

    /**
     Wywoływane gdy sprzedający doda nowy samochód na sprzedaż.
     */
    public void updateCatalogue(final String brandAndModel, final Car car) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                addBehaviour(createSellingCarBehaviour(brandAndModel, car));
            }
        } );
    }

    private class OfferRequestsServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage aclMessage = myAgent.receive(messageTemplate);
            if (aclMessage != null) {
                String content = aclMessage.getContent();
                ACLMessage reply = aclMessage.createReply();
                Car car = (Car) carCatalogue.get(content);
                Integer price = null;
                if(car != null){
                    price = car.getTotalPrice();
                }
                if (price != null) {
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(price.intValue()));
                }
                else {
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    }

    private class PurchaseOrdersServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage aclMessage = myAgent.receive(messageTemplate);
            if (aclMessage != null) {
                String content = aclMessage.getContent();
                ACLMessage reply = aclMessage.createReply();

                Car car = (Car) carCatalogue.get(content);
                Integer price = (Integer) car.getTotalPrice();
                carCatalogue.remove(content);
                if (price != null) {
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.println(content+" sprzedany agentowi "+aclMessage.getSender().getName());
                }
                else {
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("not-available");
                }

                if(carCatalogue.isEmpty()){
                    doDelete();
                    System.out.println(getAID().getName() + " jest usuwany, bo sprzedał wszystkie auta.");
                }

                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    }

    private class DelayedPurchaseOrdersServer extends CyclicBehaviour {
        public void action() {

            MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.UNKNOWN);
            ACLMessage aclMessage = myAgent.receive(messageTemplate);
            if (aclMessage != null) {
                System.out.println("Rozpoczynam rezerwację samochodu.");
                try {
                    System.out.println("CZEKAM");
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Zakończono rezerwację samochodu, przechodzę do zakupu.");
                String content = aclMessage.getContent();
                ACLMessage reply = aclMessage.createReply();
                Car car = (Car) carCatalogue.get(content);
                Integer price = null;
                if (car != null) {

                    price = car.getTotalPrice();
                }
                carCatalogue.remove(content);
                if (price != null) {
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.println(content + " sprzedany agentowi " + aclMessage.getSender().getName());
                } else {
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("not-available");
                }

                if(carCatalogue.isEmpty()){
                    doDelete();
                    System.out.println(getAID().getName() + " jest usuwany, bo sprzedał wszystkie auta.");
                }

                myAgent.send(reply);
            } else {
                block();
            }
        }
    }

    private OneShotBehaviour createSellingCarBehaviour(final String brandAndModel, final Car car) {
        return new OneShotBehaviour() {
            private static final long serialVersionUID = 1L;

            public void action() {
                carCatalogue.put(brandAndModel, car);
                System.out.println(brandAndModel + " został dodany do katalogu. Cena = " + car.getTotalPrice());
            }
        };
    }
}

