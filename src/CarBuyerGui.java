import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class CarBuyerGui extends JFrame {
    private final CarBuyerAgent myAgent;

    private final JTextField brandField;
    private final JTextField modelField;
    private final JTextField reservationField;

    CarBuyerGui(CarBuyerAgent a) {
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
        p.add(new JLabel("Reservation?:"));
        reservationField = new JTextField(15);
        p.add(reservationField);

        getContentPane().add(p, BorderLayout.CENTER);

        JButton addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    String brand = brandField.getText().trim();
                    String model = modelField.getText().trim();
                    String brandAndModel = brand + " " + model;
                    String reservation = reservationField.getText().trim();

                    myAgent.updateData(brandAndModel, reservation);

                    brandField.setText("");
                    modelField.setText("");
                    reservationField.setText("");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CarBuyerGui.this, "Invalid values. " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        p = new JPanel();
        p.add(addButton);
        getContentPane().add(p, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                myAgent.doDelete();
            }
        });
        setResizable(false);
    }

    public void showGui() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int) screenSize.getWidth() / 2;
        int centerY = (int) screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        super.setVisible(true);
    }
}