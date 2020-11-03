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
    // The catalogue of books for sale (maps the title of a book to its price)
    private Hashtable catalogue;

    // Put agent initializations here
    protected void setup() {
        // Create the catalogue
        catalogue = new Hashtable();
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
        String test = (String) args[0] + " " + (String) args[1]+ " " + (String) args[2]+ " " + (String) args[3]
                + " " + (String) args[4]+ " " + (String) args[5]+ " " + (String) args[6]+ " " + (String) args[7];
        System.out.println(test);
        Car car = new Car((String) args[0], (String) args[1], (String) args[2], (String) args[3],
                Float.parseFloat((String) args[4]), Integer.parseInt((String) args[5]), Integer.parseInt((String) args[6]), Integer.parseInt((String) args[7]));
        String brandAndModel = (String) args[0] + " " + (String) args[1];
        updateCatalogue(brandAndModel, car);
        }

        // Register the car-selling service in the yellow pages
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

        // Add the behaviour serving queries from buyer agents
        addBehaviour(new OfferRequestsServer());

        // Add the behaviour serving purchase orders from buyer agents
        addBehaviour(new PurchaseOrdersServer());
    }

    // Put agent clean-up operations here
    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Close the GUI
        //myGui.dispose();
        // Printout a dismissal message
        System.out.println("Agent sprzedający "+getAID().getName()+" kończy działanie.");
    }

    /**
     Wywoływane gdy sprzedający doda nowy samochód na sprzedaż.
     */
    public void updateCatalogue(final String brandAndModel, final Car car) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                catalogue.put(brandAndModel, car);
                System.out.println(brandAndModel + " został dodany do katalogu. Cena = " + car.getTotalPrice());
            }
        } );
    }

    private class OfferRequestsServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // Message received. Process it
                String brand = msg.getContent();
                ACLMessage reply = msg.createReply();
                Car car = (Car) catalogue.get(brand);
                Integer price = null;
                if(car != null){
                   // System.out.println("DEBUG " + car.getBasePrice());
                    price = car.getBasePrice();
                }
                if (price != null) {
                    // The requested book is available for sale. Reply with the price
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(price.intValue()));
                }
                else {
                    // The requested book is NOT available for sale.
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
                // ACCEPT_PROPOSAL Message received. Process it
                String brand = msg.getContent();
                ACLMessage reply = msg.createReply();

                Car car = (Car) catalogue.get(brand);
                //price = car.getBasePrice();
                Integer price = (Integer) car.getBasePrice();
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
}

