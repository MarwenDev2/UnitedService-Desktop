package tn.test.services;

import tn.test.entities.Role;
import tn.test.entities.User;
import tn.test.tools.Database;
import tn.test.tools.PasswordHasher;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserService implements CrudService<User> {

    private Connection connection;
    private final PasswordHasher passwordHasher = new PasswordHasher();

    public UserService() {
        connection = Database.getInstance().getConnection();
    }

    @Override
    public boolean add(User user) {
        String sql = "INSERT INTO user (name, email, password, role, phone, profile_image_path, gender, date_of_birth, address) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, passwordHasher.hashPassword(user.getPassword()));
            stmt.setString(4, user.getRole().name());
            stmt.setString(5, user.getPhone());
            stmt.setString(6, user.getProfileImagePath());
            stmt.setString(7, user.getGender());
            stmt.setDate(8, Date.valueOf(user.getDateOfBirth()));
            stmt.setString(9, user.getAddress());

            int rows = stmt.executeUpdate();
            System.out.println("✅ User added successfully.");
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error adding user: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE user SET name = ?, email = ?, role = ?, phone = ?, profile_image_path = ?, gender = ?, date_of_birth = ?, address = ? " +
                "WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getRole().name());
            stmt.setString(4, user.getPhone());
            stmt.setString(5, user.getProfileImagePath());
            stmt.setString(6, user.getGender());
            stmt.setDate(7, Date.valueOf(user.getDateOfBirth()));
            stmt.setString(8, user.getAddress());
            stmt.setInt(9, user.getId());

            stmt.executeUpdate();
            System.out.println("✅ User updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM user WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("🗑️ User deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public User findById(int id) {
        String sql = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findByCIN(int id) {
        String sql = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(extractUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    public User login(String email, String plainPassword) {
        String sql = "SELECT * FROM user WHERE email = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                if (passwordHasher.verifyPassword(plainPassword, storedHash)) {
                    return extractUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<User> findByRole(Role role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE role = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, role.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(extractUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    private User extractUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password")); // hashed
        user.setRole(Role.valueOf(rs.getString("role")));
        user.setPhone(rs.getString("phone"));
        user.setProfileImagePath(rs.getString("profile_image_path"));
        user.setGender(rs.getString("gender"));
        user.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
        user.setAddress(rs.getString("address"));
        return user;
    }
}
