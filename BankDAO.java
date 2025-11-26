import java.sql.*;
import java.util.ArrayList;

public class BankDAO {

    private static final String URL = "jdbc:mysql://localhost:3306/bankdb";
    private static final String USER = "root";
    private static final String PASS = "Root@1234";   // YOUR PASSWORD HERE

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    // LOGIN
    public static int login(String username, String password) {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT customer_id FROM customers WHERE username=? AND password=?"
            );
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }

        return -1;
    }

    // CREATE CUSTOMER
    public static boolean createCustomer(String username, String password, String name) {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO customers(username, password, fullname) VALUES (?, ?, ?)"
            );
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, name);

            ps.executeUpdate();
            return true;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // CREATE ACCOUNT
    public static int createAccount(int customerId, String owner, double balance) {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO accounts(customer_id, owner, balance) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setInt(1, customerId);
            ps.setString(2, owner);
            ps.setDouble(3, balance);

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    // DEPOSIT
    public static boolean deposit(int accountId, double amt) {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE accounts SET balance = balance + ? WHERE account_id = ?"
            );
            ps.setDouble(1, amt);
            ps.setInt(2, accountId);

            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // WITHDRAW
    public static boolean withdraw(int accountId, double amt) {
        try (Connection conn = getConnection()) {

            PreparedStatement check = conn.prepareStatement(
                "SELECT balance FROM accounts WHERE account_id=?"
            );
            check.setInt(1, accountId);
            ResultSet rs = check.executeQuery();

            if (!rs.next() || rs.getDouble(1) < amt)
                return false;

            PreparedStatement ps = conn.prepareStatement(
                "UPDATE accounts SET balance = balance - ? WHERE account_id=?"
            );
            ps.setDouble(1, amt);
            ps.setInt(2, accountId);

            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // TRANSFER MONEY (transaction-safe)
    public static boolean transfer(int fromAcc, int toAcc, double amt) {
        try (Connection conn = getConnection()) {

            conn.setAutoCommit(false);

            // withdraw
            PreparedStatement check1 = conn.prepareStatement(
                "SELECT balance FROM accounts WHERE account_id=?"
            );
            check1.setInt(1, fromAcc);
            ResultSet rs1 = check1.executeQuery();
            if (!rs1.next() || rs1.getDouble(1) < amt) {
                conn.rollback();
                return false;
            }

            PreparedStatement w = conn.prepareStatement(
                "UPDATE accounts SET balance = balance - ? WHERE account_id=?"
            );
            w.setDouble(1, amt);
            w.setInt(2, fromAcc);
            w.executeUpdate();

            // deposit
            PreparedStatement d = conn.prepareStatement(
                "UPDATE accounts SET balance = balance + ? WHERE account_id=?"
            );
            d.setDouble(1, amt);
            d.setInt(2, toAcc);
            d.executeUpdate();

            conn.commit();
            return true;

        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // GET ACCOUNTS LIST
    public static ArrayList<String[]> getAccounts(int customerId) {
        ArrayList<String[]> list = new ArrayList<>();

        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT account_id, owner, balance FROM accounts WHERE customer_id=?"
            );
            ps.setInt(1, customerId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("account_id"),
                    rs.getString("owner"),
                    rs.getString("balance")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }

        return list;
    }
}
