package uuuaaaah;

import java.awt.Font;
import javax.swing.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author LAB F
 */
public class LoginForm extends JFrame {
    private JTextField namaField;
    private JComboBox<String> roleCombo;
    private JButton loginButton;

    public LoginForm() {
        setTitle("SmartQueue Login");
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        namaField = new JTextField();
        roleCombo = new JComboBox<>(new String[]{"Pelanggan", "Admin"});
        loginButton = new JButton("Lanjut");

        setLayout(null);
        JLabel namaLabel = new JLabel("Nama:");
        JLabel roleLabel = new JLabel("Login sebagai:");

        namaLabel.setBounds(20, 20, 100, 25);
        namaField.setBounds(120, 20, 140, 25);
        roleLabel.setBounds(20, 60, 100, 25);
        roleCombo.setBounds(120, 60, 140, 25);
        loginButton.setBounds(90, 110, 100, 30);

        add(namaLabel);
        add(namaField);
        add(roleLabel);
        add(roleCombo);
        add(loginButton);

        loginButton.addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String nama = namaField.getText();
        String role = (String) roleCombo.getSelectedItem();

        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama tidak boleh kosong");
            return;
        }

        if (role.equals("Pelanggan")) {
            new PelangganForm(nama).setVisible(true);
        } else {
            new AdminForm(nama).setVisible(true);
        }
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}

// PelangganForm.java
class PelangganForm extends JFrame {
    private String nama;
    private JTextField mejaField;
    private JButton ambilBtn;

    public PelangganForm(String nama) {
        this.nama = nama;
        setTitle("Ambil Antrian - " + nama);
        setSize(300, 150);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel mejaLabel = new JLabel("Nomor Meja:");
        mejaField = new JTextField();
        ambilBtn = new JButton("Ambil Antrian");

        setLayout(null);
        mejaLabel.setBounds(20, 20, 100, 25);
        mejaField.setBounds(120, 20, 140, 25);
        ambilBtn.setBounds(90, 70, 120, 30);

        add(mejaLabel);
        add(mejaField);
        add(ambilBtn);

        ambilBtn.addActionListener(e -> ambilAntrian());
    }

    private void ambilAntrian() {
        try (Connection conn = SmartQueueApp.connectDB()) {
            Pelanggan p = new Pelanggan(nama);
            String meja = mejaField.getText();
            if (!meja.isEmpty() && meja.matches("\\d+")) {
                p.ambilAntrian(conn, Integer.parseInt(meja));
            } else {
                p.ambilAntrian(conn);
            }
            JOptionPane.showMessageDialog(this, "Antrian berhasil diambil.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error DB: " + e.getMessage());
        }
    }
}

// AdminForm.java (tampilan seperti gambar JTable mahasiswa)
class AdminForm extends JFrame {
    private String nama;
    private JTable table;
    private DefaultTableModel model;
    private JTextField namaField, mejaField;
    private JButton btnBaru, btnPanggil, btnHapus, btnTutup;

    public AdminForm(String nama) {
        this.nama = nama;
        setTitle("Data Antrian - Admin");
        setSize(700, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel labelJudul = new JLabel("Data Antrian");
        labelJudul.setFont(new Font("Arial", Font.BOLD, 18));
        labelJudul.setBounds(20, 10, 300, 30);
        add(labelJudul);

        model = new DefaultTableModel(new String[]{"ID", "Nama Pelanggan", "Status"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(20, 50, 640, 150);
        add(scrollPane);

        JLabel lblNama = new JLabel("Nama:");
        lblNama.setBounds(20, 210, 80, 25);
        add(lblNama);

        namaField = new JTextField();
        namaField.setBounds(100, 210, 200, 25);
        add(namaField);

        JLabel lblMeja = new JLabel("No. Meja:");
        lblMeja.setBounds(320, 210, 80, 25);
        add(lblMeja);

        mejaField = new JTextField();
        mejaField.setBounds(400, 210, 100, 25);
        add(mejaField);

        btnBaru = new JButton("Baru");
        btnBaru.setBounds(20, 250, 100, 30);
        add(btnBaru);

        btnPanggil = new JButton("Panggil");
        btnPanggil.setBounds(130, 250, 100, 30);
        add(btnPanggil);

        btnHapus = new JButton("Hapus");
        btnHapus.setBounds(240, 250, 100, 30);
        add(btnHapus);

        btnTutup = new JButton("Tutup");
        btnTutup.setBounds(350, 250, 100, 30);
        add(btnTutup);

        tampilkanData();

        btnBaru.addActionListener(e -> tambahAntrian());
        btnPanggil.addActionListener(e -> panggilAntrian());
        btnHapus.addActionListener(e -> hapusAntrian());
        btnTutup.addActionListener(e -> dispose());
    }

    private void tampilkanData() {
        try (Connection conn = SmartQueueApp.connectDB()) {
            model.setRowCount(0);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM antrian");
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nama_pelanggan"),
                        rs.getString("status")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal load data: " + e.getMessage());
        }
    }

    private void tambahAntrian() {
        String nama = namaField.getText();
        String meja = mejaField.getText();
        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama tidak boleh kosong.");
            return;
        }
        try (Connection conn = SmartQueueApp.connectDB()) {
            String namaFinal = meja.isEmpty() ? nama : nama + " (Meja " + meja + ")";
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO antrian(nama_pelanggan, status) VALUES (?, 'Menunggu')"
            );
            stmt.setString(1, namaFinal);
            stmt.executeUpdate();
            tampilkanData();
            namaField.setText("");
            mejaField.setText("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal tambah: " + e.getMessage());
        }
    }

    private void hapusAntrian() {
        int selected = table.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data terlebih dahulu!");
            return;
        }
        int id = (int) model.getValueAt(selected, 0);
        try (Connection conn = SmartQueueApp.connectDB()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM antrian WHERE id=?");
            stmt.setInt(1, id);
            stmt.executeUpdate();
            tampilkanData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal hapus: " + e.getMessage());
        }
    }

    private void panggilAntrian() {
        try (Connection conn = SmartQueueApp.connectDB()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM antrian WHERE status='Menunggu' ORDER BY id ASC LIMIT 1");
            if (rs.next()) {
                int id = rs.getInt("id");
                String namaPelanggan = rs.getString("nama_pelanggan");
                PreparedStatement update = conn.prepareStatement("UPDATE antrian SET status='Dipanggil' WHERE id=?");
                update.setInt(1, id);
                update.executeUpdate();
                tampilkanData();
                JOptionPane.showMessageDialog(this, "Memanggil: " + namaPelanggan);
            } else {
                JOptionPane.showMessageDialog(this, "Tidak ada antrian.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error panggil: " + e.getMessage());
        }
    }
}

  

// Tetap gunakan SmartQueueApp untuk koneksi DB
class SmartQueueApp {
    public static Connection connectDB() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/smartqueue";
        String user = "root";
        String pass = "";
        return DriverManager.getConnection(url, user, pass);
    }
}

// User.java
class User {
    protected String nama;
    protected String role;

    public User(String nama, String role) {
        this.nama = nama;
        this.role = role;
    }

    public void login() {
        System.out.println(nama + " login sebagai " + role);
    }
}

// Pelanggan.java
class Pelanggan extends User {
    public Pelanggan(String nama) {
        super(nama, "Pelanggan");
    }

    @Override
    public void login() {
        System.out.println("Selamat datang, pelanggan " + nama);
    }

    public void ambilAntrian(Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO antrian(nama_pelanggan, status) VALUES (?, ?)");
        stmt.setString(1, this.nama);
        stmt.setString(2, "Menunggu");
        stmt.executeUpdate();
    }

    // Overloading method
    public void ambilAntrian(Connection conn, int nomorMeja) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO antrian(nama_pelanggan, status) VALUES (?, ?)");
        stmt.setString(1, this.nama + " (Meja " + nomorMeja + ")");
        stmt.setString(2, "Menunggu");
        stmt.executeUpdate();
    }
}

// Admin.java
class Admin extends User {
    public Admin(String nama) {
        super(nama, "Admin");
    }

    @Override
    public void login() {
        System.out.println("Admin " + nama + " masuk ke sistem.");
    }

    public void panggilAntrian(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM antrian WHERE status='Menunggu' ORDER BY id ASC LIMIT 1");
        if (rs.next()) {
            int id = rs.getInt("id");
            String namaPelanggan = rs.getString("nama_pelanggan");
            PreparedStatement update = conn.prepareStatement("UPDATE antrian SET status='Dipanggil' WHERE id=?");
            update.setInt(1, id);
            update.executeUpdate();
            System.out.println("Memanggil: " + namaPelanggan);
        } else {
            System.out.println("Tidak ada antrian.");
        }
    }
}
