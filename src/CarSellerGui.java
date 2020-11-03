import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class CarSellerGui extends JFrame {
    private CarSellerAgent myAgent;

    private JTextField brandField;
    private JTextField modelField;
    private JTextField bodyTypeField;
    private JTextField engineTypeField;
    private JTextField engineCapacityField;
    private JTextField yearsOfProductionField;
    private JTextField basicPriceField;
    private JTextField additionalCostsField;

    CarSellerGui(CarSellerAgent a) {
        super(a.getLocalName());

        myAgent = a;

        JPanel p = new JPanel();
        p.setLayout(new GridLayout(8, 2));
        p.add(new JLabel("Brand:"));
        brandField = new JTextField(15);
        p.add(brandField);
        p.add(new JLabel("Model:"));
        modelField = new JTextField(15);
        p.add(modelField);
        p.add(new JLabel("Body Type:"));
        bodyTypeField = new JTextField(15);
        p.add(bodyTypeField);
        p.add(new JLabel("Engine Type:"));
        engineTypeField = new JTextField(15);
        p.add(engineTypeField);
        p.add(new JLabel("Engine Capacity:"));
        engineCapacityField = new JTextField(15);
        p.add(engineCapacityField);
        p.add(new JLabel("Years Of Production:"));
        yearsOfProductionField = new JTextField(15);
        p.add(yearsOfProductionField);
        p.add(new JLabel("Basic Price:"));
        basicPriceField = new JTextField(15);
        p.add(basicPriceField);
        p.add(new JLabel("Additional Costs:"));
        additionalCostsField = new JTextField(15);
        p.add(additionalCostsField);
        getContentPane().add(p, BorderLayout.CENTER);

        JButton addButton = new JButton("Add");
        addButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    String brand = brandField.getText().trim();
                    String model = modelField.getText().trim();
                    String bodyType = bodyTypeField.getText().trim();
                    String engineType = engineTypeField.getText().trim();
                    String engineCapacity = engineCapacityField.getText().trim();
                    String yearsOfProduction = yearsOfProductionField.getText().trim();
                    String basicPrice = basicPriceField.getText().trim();
                    String additionalCosts = additionalCostsField.getText().trim();
                    Car car = new Car(brand, model, bodyType, engineType, Integer.parseInt(engineCapacity), Integer.parseInt(yearsOfProduction), Integer.parseInt(basicPrice), Integer.parseInt(additionalCosts));
                    String brandAndModel = brand + " " + model;
                    myAgent.updateCatalogue(brandAndModel, car);
                    brandField.setText("");
                    modelField.setText("");
                    bodyTypeField.setText("");
                    engineTypeField.setText("");
                    engineCapacityField.setText("");
                    yearsOfProductionField.setText("");
                    basicPriceField.setText("");
                    additionalCostsField.setText("");
                }
                catch (Exception e) {
                    JOptionPane.showMessageDialog(CarSellerGui.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } );
        p = new JPanel();
        p.add(addButton);
        getContentPane().add(p, BorderLayout.SOUTH);

        addWindowListener(new	WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                myAgent.doDelete();
            }
        } );
        setResizable(false);
    }

    public void showGui() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int)screenSize.getWidth() / 2;
        int centerY = (int)screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        super.setVisible(true);
    }
}

