package tn.test.services;

import tn.test.entities.Notification;
import tn.test.entities.User;
import tn.test.tools.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationService implements CrudService<Notification> {

    private final Connection connection;
    private final WorkerService userService;

    public NotificationService() {
        connection = Database.getInstance().getConnection();
        userService = new WorkerService();
    }

    @Override
    public boolean add(Notification notification) {
        String sql = "INSERT INTO notification (recipient_id, message, timestamp, is_read) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, notification.getRecipient().getId());
            stmt.setString(2, notification.getMessage());
            stmt.setTimestamp(3, Timestamp.valueOf(notification.getTimestamp()));
            stmt.setBoolean(4, notification.isRead());

            int rows = stmt.executeUpdate();
            System.out.println("✅ Notification added.");
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error adding notification: " + e.getMessage());
            return false;
        }
    }


    @Override
    public void update(Notification notification) {
        String sql = "UPDATE notification SET recipient_id = ?, message = ?, timestamp = ?, is_read = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, notification.getRecipient().getId());
            stmt.setString(2, notification.getMessage());
            stmt.setTimestamp(3, Timestamp.valueOf(notification.getTimestamp()));
            stmt.setBoolean(4, notification.isRead());
            stmt.setInt(5, notification.getId());

            stmt.executeUpdate();
            System.out.println("✅ Notification updated.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM notification WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("🗑️ Notification deleted.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Notification findById(int id) {
        String sql = "SELECT * FROM notification WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractNotification(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Notification> findAll() {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notification";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(extractNotification(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Notification> findUnreadByUserId(int userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notification WHERE recipient_id = ? AND is_read = false";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(extractNotification(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public void markAsRead(int notificationId) {
        String sql = "UPDATE notification SET is_read = true WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, notificationId);
            stmt.executeUpdate();
            System.out.println("📩 Notification marked as read.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Notification> findByUserId(int userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notification WHERE recipient_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(extractNotification(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Notification> findRecentActionsForAdmin(int adminId, int monthsBack) {
        List<Notification> list = new ArrayList<>();
        String sql = """
        SELECT * FROM notification 
        WHERE recipient_id = ? AND timestamp >= NOW() - INTERVAL ? MONTH
        ORDER BY timestamp DESC
        LIMIT 50
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, adminId);
            stmt.setInt(2, monthsBack);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Notification n = new Notification();
                n.setId(rs.getInt("id"));
                n.setRecipient(userService.findById(rs.getInt("recipient_id")));
                n.setMessage(rs.getString("message"));
                n.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                n.setRead(rs.getBoolean("is_read"));
                list.add(n);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
    public int countUnreadNotifications(int userId) {
        String sql = "SELECT COUNT(*) FROM notification WHERE recipient_id = ? AND is_read = false";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }


    private Notification extractNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setRecipient(userService.findById(rs.getInt("recipient_id")));
        n.setMessage(rs.getString("message"));
        n.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        n.setRead(rs.getBoolean("is_read"));
        return n;
    }
}
