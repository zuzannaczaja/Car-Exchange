import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;
import java.util.Random;

public class CarSellerAgentNoGui extends Agent {

    private Hashtable carCatalogue;
    private Hashtable reservationList;
    Reservation reservation;

    protected void setup() {
        carCatalogue = new Hashtable();

        Object[] args = getArguments();

        if (args != null && args.length > 0) {

            String brand;
            String model;
            String bodyType;
            String engineType;
            float engineCapacity;
            int yearOfProduction;
            int basePrice;
            int additionalCosts;

            for(Object o : args) {
                String[] s = ((String)o).split("-");
                brand = s[0].toString();
                model = s[1].toString();
                bodyType = s[2].toString();
                engineType = s[3].toString();
                engineCapacity = Float.parseFloat(s[4]);
                yearOfProduction = Integer.parseInt(s[5]);
                basePrice = Integer.parseInt(s[6]);
                additionalCosts = Integer.parseInt(s[7]);

                Car car = new Car(brand, model, bodyType, engineType, engineCapacity, yearOfProduction, basePrice, additionalCosts);
                String brandAndModel = s[0] + " " + s[1];
                updateCatalogue(brandAndModel, car);
            }
        }

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("car-selling");
        sd.setName("JADE-car-trading");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new OfferRequestsServer());
        addBehaviour(new PurchaseOrdersServer());
        addBehaviour(new DelayedPurchaseOrdersServer());
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Agent sprzedający " + getAID().getName() + " kończy działanie.");
    }

    /**
     * Wywoływane gdy sprzedający doda nowy samochód na sprzedaż.
     */
    public void updateCatalogue(final String brandAndModel, final Car car) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                addBehaviour(createSellingCarBehaviour(brandAndModel, car));
            }
        });
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
                if (car != null) {
                    price = car.getTotalPrice();
                }
                if (price != null) {
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(price.intValue()));
                } else {
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            } else {
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

    private class DelayedPurchaseOrdersServer extends CyclicBehaviour {
        public void action() {

            MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.UNKNOWN);
            ACLMessage aclMessage = myAgent.receive(messageTemplate);

            if (aclMessage != null) {
                /*System.out.println("Rozpoczynam rezerwację samochodu.");
                try {
                    System.out.println("CZEKAM");
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Zakończono rezerwację samochodu, przechodzę do zakupu.");*/
                String content = aclMessage.getContent();
                ACLMessage reply = aclMessage.createReply();
                Car car = (Car) carCatalogue.get(content);
                Reservation reservation = (Reservation) reservationList.get(content);
                CarBuyerAgentNoGui carBuyerAgentNoGui = new CarBuyerAgentNoGui();
                //Reservation reservation = new Reservation(aclMessage.getSender().getLocalName(), car, System.currentTimeMillis());

                Random random = new Random();
                //int randomInt = random.nextInt(20000 - 0 + 1) + 0;

                //sprawdzamy czy istnieje samochód i posiada rezerwację
                if (carCatalogue.contains(reservation.getCar().getBrand() + " " + reservation.getCar().getModel()) && reservation != null){
                    if(reservation.getTimeOfReservation() + reservation.getHowLongNeedsToBeReserved() >= System.currentTimeMillis()
                            && reservation.getBuyerName().equals(aclMessage.getSender().getLocalName())){
                        //koniec rezerwacji i zakup
                    } else {
                        //rezerwacja istnieje, ale nie dobiegła końca
                    }
                } else if (reservation == null){
                    //istnieje samochód, ale nie posiada rezerwacji
                    //losujemy czy ma podlegac rezerwacji

                    //jezeli tak to losujemy czas rezerwacji i tworzymy jeo obiekt
                    if(carBuyerAgentNoGui.isDelayed){
                        int randomTimeOfReservation = random.nextInt(20000 + 1);
                        reservation = new Reservation(aclMessage.getSender().getLocalName(), car, System.currentTimeMillis(), randomTimeOfReservation);
                        updateReservationList(aclMessage.getSender().getLocalName(), reservation);
                    } else if (!carBuyerAgentNoGui.isDelayed){
                        //nie musi być rezerwowany więc pozwalamy go kupić od razu
                    }
                } else {
                    //nie ma takiego samochodu w katalogu
                }

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

    private OneShotBehaviour createReservationBehaviour(final String brandAndModel, final Reservation reservation) {
        return new OneShotBehaviour() {
            private static final long serialVersionUID = 1L;

            public void action() {
                reservationList.put(brandAndModel, reservation);
                System.out.println("> Rezerwacja kupującego: " + brandAndModel
                        + " została dodana do listy rezerwacji. Zarezerwowany samochód: "
                        + reservation.getCar().getBrand() + " " + reservation.getCar().getModel());
            }
        };
    }
}

