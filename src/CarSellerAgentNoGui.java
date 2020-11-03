import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;


import java.util.*;

public class CarSellerAgentNoGui extends Agent {

    private Hashtable catalogue;

    protected void setup() {
        catalogue = new Hashtable();
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
        //String test = (String) args[0] + " " + (String) args[1]+ " " + (String) args[2]+ " " + (String) args[3]
        //        + " " + (String) args[4]+ " " + (String) args[5]+ " " + (String) args[6]+ " " + (String) args[7];
        //System.out.println(test);
        Car car = new Car((String) args[0], (String) args[1], (String) args[2], (String) args[3],
                Float.parseFloat((String) args[4]), Integer.parseInt((String) args[5]), Integer.parseInt((String) args[6]), Integer.parseInt((String) args[7]));
        String brandAndModel = (String) args[0] + " " + (String) args[1];
        updateCatalogue(brandAndModel, car);
        }

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("car-selling");
        sd.setName("JADE-car-trading");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new OfferRequestsServer());
        addBehaviour(new PurchaseOrdersServer());
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
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
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String brand = msg.getContent();
                ACLMessage reply = msg.createReply();
                Car car = (Car) catalogue.get(brand);
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
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String brand = msg.getContent();
                ACLMessage reply = msg.createReply();

                Car car = (Car) catalogue.get(brand);

                Integer price = null;
                if(car != null){

                    price = car.getTotalPrice();
                }
                catalogue.remove(brand);
                if (price != null) {
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.println(brand+" sprzedany agentowi "+msg.getSender().getName());
                }
                else {
                    // The requested book has been sold to another buyer in the meanwhile .
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    }

    private OneShotBehaviour createSellingCarBehaviour(final String brandAndModel, final Car car) {
        return new OneShotBehaviour() {
            private static final long serialVersionUID = 1L;

            public void action() {
                catalogue.put(brandAndModel, car);
                System.out.println(brandAndModel + " został dodany do katalogu. Cena = " + car.getTotalPrice());
            }
        };
    }
}

