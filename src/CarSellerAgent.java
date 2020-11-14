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
    private Hashtable reservationList;
    private CarSellerGui myGui;

    protected void setup() {
        carCatalogue = new Hashtable();
        reservationList = new Hashtable();

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

    public void updateReservationList(final String brandAndModel, final Reservation reservation) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                addBehaviour(createReservationBehaviour(brandAndModel, reservation));
            }
        });
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
                String[] parts = content.split(",");

                content = parts[0];
                String isDelayedString = parts[1];
                boolean isDelayedBoolean = Boolean.parseBoolean(isDelayedString);

                ACLMessage reply = aclMessage.createReply();
                Car car = (Car) carCatalogue.get(content);
                Reservation reservation = (Reservation) reservationList.get(content);

                Random random = new Random();
                Integer price = null;
                String buyerName = aclMessage.getSender().getLocalName();

                //sprawdzamy czy istnieje samochód i posiada rezerwację
                if (reservation.getBuyerName() != null){

                    if(reservation.getTimeOfReservation() + reservation.getHowLongNeedsToBeReserved() <= System.currentTimeMillis()
                            && buyerName.equals(reservation.getBuyerName())){
                        //koniec rezerwacji i zakup

                        System.out.println("Koniec rezerwacji u " + getAID().getLocalName() + " przez " + aclMessage.getSender().getLocalName() + "!");

                        if (car != null) {

                            price = car.getTotalPrice();
                        }
                        carCatalogue.remove(content);
                        reservationList.remove(content);

                        if (price != null) {
                            reply.setPerformative(ACLMessage.INFORM);
                            System.out.println(content + " sprzedany agentowi " + aclMessage.getSender().getName());
                        } else {
                            reply.setPerformative(ACLMessage.FAILURE);
                            reply.setContent("not-available");
                        }

                    } else {
                        //rezerwacja istnieje, ale nie dobiegła końca

                        if (price != null) {
                            reply.setPerformative(ACLMessage.FAILURE);
                            reply.setContent("not-available");
                            System.out.println("Nieudana próba kupna: rezerwacja samochodu nadal trwa.");
                        }

                    }
                } else if (reservation.getBuyerName() == null){
                    //istnieje samochód, ale nie posiada rezerwacji
                    //losujemy czy ma podlegac rezerwacji

                    //jezeli tak to losujemy czas rezerwacji i tworzymy jego obiekt
                    if(isDelayedBoolean){
                        int randomTimeOfReservation = random.nextInt(20000 + 1);
                        reservation.setBuyerName(aclMessage.getSender().getLocalName());
                        reservation.setCar(car);
                        reservation.setTimeOfReservation(System.currentTimeMillis());
                        reservation.setHowLongNeedsToBeReserved(randomTimeOfReservation);

                        System.out.println("Zarezerwowano samochód u "  + getAID().getLocalName() + " przez " + aclMessage.getSender().getLocalName() + "!");

                    } else if (!isDelayedBoolean){
                        //nie musi być rezerwowany więc pozwalamy go kupić od razu

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
                            System.out.println("Nieudana próba kupna: wybrany samochód nie jest dostępny.");
                        }
                    }
                } else {
                    //nie ma takiego samochodu w katalogu
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


    private OneShotBehaviour createReservationBehaviour(final String brandAndModel, final Reservation reservation) {
        return new OneShotBehaviour() {
            private static final long serialVersionUID = 1L;

            public void action() {
                reservationList.put(brandAndModel, reservation);
               /* System.out.println("> Rezerwacja kupującego: " + brandAndModel
                        + " została dodana do listy rezerwacji. Zarezerwowany samochód: "
                        + reservation.getCar().getBrand() + " " + reservation.getCar().getModel());*/
            }
        };
    }
}

