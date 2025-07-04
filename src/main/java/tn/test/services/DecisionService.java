package tn.test.services;

import tn.test.entities.Decision;
import tn.test.entities.User;
import tn.test.tools.Database;
import tn.test.entities.Role;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DecisionService implements CrudService<Decision> {

    private final Connection connection;
    private final UserService userService;

    public DecisionService() {
        connection = Database.getInstance().getConnection();
        userService = new UserService(); // To fetch the User who made the decision
    }

    @Override
    public boolean add(Decision decision) {
        String sql = "INSERT INTO decision (decision_by_id, approved, comment, date) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, decision.getDecisionBy().getId());
            stmt.setBoolean(2, decision.isApproved());
            stmt.setString(3, decision.getComment());
            stmt.setTimestamp(4, Timestamp.valueOf(decision.getDate()));

            int rows = stmt.executeUpdate();
            System.out.println("✅ Decision added successfully.");
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error adding decision: " + e.getMessage());
            return false;
        }
    }


    @Override
    public void update(Decision decision) {
        String sql = "UPDATE decision SET decision_by_id = ?, approved = ?, comment = ?, date = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, decision.getDecisionBy().getId());
            stmt.setBoolean(2, decision.isApproved());
            stmt.setString(3, decision.getComment());
            stmt.setTimestamp(4, Timestamp.valueOf(decision.getDate()));
            stmt.setInt(5, decision.getId());

            stmt.executeUpdate();
            System.out.println("✅ Decision updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM decision WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("🗑️ Decision deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Decision findById(int id) {
        String sql = "SELECT * FROM decision WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractDecision(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Decision> findAll() {
        List<Decision> decisions = new ArrayList<>();
        String sql = "SELECT * FROM decision";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                decisions.add(extractDecision(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return decisions;
    }

    public List<Decision> findByDemandeId(int demandeId) {
        List<Decision> decisions = new ArrayList<>();
        String sql = """
            SELECT d.* FROM decision d
            JOIN demande_conge dc ON
                dc.secretaire_decision_id = d.id OR
                dc.rh_decision_id = d.id OR
                dc.admin_decision_id = d.id
            WHERE dc.id = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, demandeId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                decisions.add(extractDecision(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return decisions;
    }

    public List<Decision> findByDecisionBy(int userId) {
        List<Decision> decisions = new ArrayList<>();
        String sql = "SELECT * FROM decision WHERE decision_by_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                decisions.add(extractDecision(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return decisions;
    }

    private Decision extractDecision(ResultSet rs) throws SQLException {
        Decision decision = new Decision();
        decision.setId(rs.getInt("id"));
        User decisionBy = userService.findById(rs.getInt("decision_by_id"));
        decision.setDecisionBy(decisionBy);
        decision.setApproved(rs.getBoolean("approved"));
        decision.setComment(rs.getString("comment"));
        decision.setDate(rs.getTimestamp("date").toLocalDateTime());
        return decision;
    }
}
