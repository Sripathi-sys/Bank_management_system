import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class BankAppSwing {

    private JFrame frame;
    private String loggedUser;
    private int loggedCustomerId;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BankAppSwing().showLogin());
    }

    // LOGIN SCREEN
    private void showLogin() {
        frame = new JFrame("Bank Login");
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel p = new JPanel(new GridLayout(3, 2));

        JTextField user = new JTextField();
        JPasswordField pass = new JPasswordField();
        JButton login = new JButton("Login");
        JButton create = new JButton("Create Account");

        p.add(new JLabel("Username"));
        p.add(user);
        p.add(new JLabel("Password"));
        p.add(pass);
        p.add(login);
        p.add(create);

        frame.add(p);
        frame.setVisible(true);

        login.addActionListener(e -> {
            int cid = BankDAO.login(user.getText(), new String(pass.getPassword()));
            if (cid != -1) {
                loggedUser = user.getText();
                loggedCustomerId = cid;
                showDashboard();
            } else {
                JOptionPane.showMessageDialog(frame, "Login Failed!");
            }
        });

        create.addActionListener(e -> showCreateCustomer());
    }

    // CREATE CUSTOMER
    private void showCreateCustomer() {
        JFrame f = new JFrame("Create User");
        f.setSize(300, 200);

        JPanel p = new JPanel(new GridLayout(4, 2));

        JTextField u = new JTextField();
        JTextField p1 = new JTextField();
        JTextField n = new JTextField();

        JButton ok = new JButton("Create");

        p.add(new JLabel("Username"));
        p.add(u);
        p.add(new JLabel("Password"));
        p.add(p1);
        p.add(new JLabel("Full Name"));
        p.add(n);
        p.add(ok);

        f.add(p);
        f.setVisible(true);

        ok.addActionListener(e -> {
            boolean done = BankDAO.createCustomer(u.getText(), p1.getText(), n.getText());
            JOptionPane.showMessageDialog(f, done ? "Created!" : "Error");
        });
    }

    // DASHBOARD
    private void showDashboard() {
        frame.getContentPane().removeAll();

        JPanel p = new JPanel();

        JButton create = new JButton("Create Account");
        JButton depo = new JButton("Deposit");
        JButton with = new JButton("Withdraw");
        JButton trans = new JButton("Transfer");
        JButton view = new JButton("View Accounts");

        p.add(create);
        p.add(depo);
        p.add(with);
        p.add(trans);
        p.add(view);

        frame.add(p);
        frame.revalidate();
        frame.repaint();

        create.addActionListener(e -> createAccount());
        depo.addActionListener(e -> depositUI());
        with.addActionListener(e -> withdrawUI());
        trans.addActionListener(e -> transferUI());
        view.addActionListener(e -> viewAccounts());
    }

    // CREATE ACCOUNT UI
    private void createAccount() {
        JFrame f = new JFrame("Create Account");
        f.setSize(300, 200);

        JPanel p = new JPanel(new GridLayout(3, 2));
        JTextField owner = new JTextField();
        JTextField dep = new JTextField();
        JButton ok = new JButton("Create");

        p.add(new JLabel("Owner Name"));
        p.add(owner);
        p.add(new JLabel("Initial Deposit"));
        p.add(dep);
        p.add(ok);

        f.add(p);
        f.setVisible(true);

        ok.addActionListener(e -> {
            int acc = BankDAO.createAccount(
                    loggedCustomerId,
                    owner.getText(),
                    Double.parseDouble(dep.getText())
            );
            JOptionPane.showMessageDialog(f, acc != -1 ?
                    "Created Account ID = " + acc : "Error");
        });
    }

    // DEPOSIT UI
    private void depositUI() {
        JFrame f = new JFrame("Deposit");
        f.setSize(300, 200);

        JPanel p = new JPanel(new GridLayout(3, 2));
        JTextField aid = new JTextField();
        JTextField amt = new JTextField();
        JButton ok = new JButton("Deposit");

        p.add(new JLabel("Account ID"));
        p.add(aid);
        p.add(new JLabel("Amount"));
        p.add(amt);
        p.add(ok);

        f.add(p);
        f.setVisible(true);

        ok.addActionListener(e -> {
            boolean done = BankDAO.deposit(
                    Integer.parseInt(aid.getText()),
                    Double.parseDouble(amt.getText())
            );
            JOptionPane.showMessageDialog(f, done ? "Success!" : "Failed!");
        });
    }

    // WITHDRAW UI
    private void withdrawUI() {
        JFrame f = new JFrame("Withdraw");
        f.setSize(300, 200);

        JPanel p = new JPanel(new GridLayout(3, 2));
        JTextField aid = new JTextField();
        JTextField amt = new JTextField();
        JButton ok = new JButton("Withdraw");

        p.add(new JLabel("Account ID"));
        p.add(aid);
        p.add(new JLabel("Amount"));
        p.add(amt);
        p.add(ok);

        f.add(p);
        f.setVisible(true);

        ok.addActionListener(e -> {
            boolean done = BankDAO.withdraw(
                    Integer.parseInt(aid.getText()),
                    Double.parseDouble(amt.getText())
            );
            JOptionPane.showMessageDialog(f, done ? "Success!" : "Failed!");
        });
    }

    // TRANSFER UI
    private void transferUI() {
        JFrame f = new JFrame("Transfer");
        f.setSize(300, 250);

        JPanel p = new JPanel(new GridLayout(4, 2));

        JTextField from = new JTextField();
        JTextField to = new JTextField();
        JTextField amt = new JTextField();

        JButton ok = new JButton("Transfer");

        p.add(new JLabel("From Account"));
        p.add(from);
        p.add(new JLabel("To Account"));
        p.add(to);
        p.add(new JLabel("Amount"));
        p.add(amt);
        p.add(ok);

        f.add(p);
        f.setVisible(true);

        ok.addActionListener(e -> {
            boolean done = BankDAO.transfer(
                    Integer.parseInt(from.getText()),
                    Integer.parseInt(to.getText()),
                    Double.parseDouble(amt.getText())
            );
            JOptionPane.showMessageDialog(f, done ? "Success!" : "Failed!");
        });
    }

    // VIEW ACCOUNTS TABLE
    private void viewAccounts() {
        ArrayList<String[]> list = BankDAO.getAccounts(loggedCustomerId);

        JFrame f = new JFrame("Your Accounts");
        f.setSize(400, 300);

        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Account ID");
        model.addColumn("Owner");
        model.addColumn("Balance");

        for (String[] row : list)
            model.addRow(row);

        JTable t = new JTable(model);

        f.add(new JScrollPane(t));
        f.setVisible(true);
    }
}
