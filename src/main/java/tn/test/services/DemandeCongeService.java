package tn.test.services;

import tn.test.entities.*;
import tn.test.tools.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DemandeCongeService implements CrudService<DemandeConge> {

    private final Connection connection;
    private final WorkerService workerService;
    private final DecisionService decisionService;

    public DemandeCongeService() {
        connection = Database.getInstance().getConnection();
        workerService = new WorkerService();
        decisionService = new DecisionService();
    }

    @Override
    public void add(DemandeConge demande) {
        String sql = """
            INSERT INTO demande_conge (worker_id, start_date, end_date, type, reason, status, secretaire_decision_id, rh_decision_id, admin_decision_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, demande.getWorker().getId());
            stmt.setDate(2, Date.valueOf(demande.getStartDate()));
            stmt.setDate(3, Date.valueOf(demande.getEndDate()));
            stmt.setString(4, demande.getType().name());
            stmt.setString(5, demande.getReason());
            stmt.setString(6, demande.getStatus().name());

            stmt.setObject(7, demande.getSecretaireDecision() != null ? demande.getSecretaireDecision().getId() : null, Types.INTEGER);
            stmt.setObject(8, demande.getRhDecision() != null ? demande.getRhDecision().getId() : null, Types.INTEGER);
            stmt.setObject(9, demande.getAdminDecision() != null ? demande.getAdminDecision().getId() : null, Types.INTEGER);

            stmt.executeUpdate();
            System.out.println("✅ DemandeConge added.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(DemandeConge demande) {
        String sql = """
            UPDATE demande_conge SET worker_id = ?, start_date = ?, end_date = ?, type = ?, reason = ?, status = ?, 
            secretaire_decision_id = ?, rh_decision_id = ?, admin_decision_id = ? WHERE id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, demande.getWorker().getId());
            stmt.setDate(2, Date.valueOf(demande.getStartDate()));
            stmt.setDate(3, Date.valueOf(demande.getEndDate()));
            stmt.setString(4, demande.getType().name());
            stmt.setString(5, demande.getReason());
            stmt.setString(6, demande.getStatus().name());

            stmt.setObject(7, demande.getSecretaireDecision() != null ? demande.getSecretaireDecision().getId() : null, Types.INTEGER);
            stmt.setObject(8, demande.getRhDecision() != null ? demande.getRhDecision().getId() : null, Types.INTEGER);
            stmt.setObject(9, demande.getAdminDecision() != null ? demande.getAdminDecision().getId() : null, Types.INTEGER);

            stmt.setInt(10, demande.getId());
            stmt.executeUpdate();
            System.out.println("✅ DemandeConge updated.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM demande_conge WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("🗑️ DemandeConge deleted.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public DemandeConge findById(int id) {
        String sql = "SELECT * FROM demande_conge WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractDemande(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<DemandeConge> findAll() {
        List<DemandeConge> list = new ArrayList<>();
        String sql = "SELECT * FROM demande_conge";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(extractDemande(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<DemandeConge> findByWorkerId(int workerId) {
        List<DemandeConge> list = new ArrayList<>();
        String sql = "SELECT * FROM demande_conge WHERE worker_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, workerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(extractDemande(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<DemandeConge> findPendingByRole(Role role) {
        List<DemandeConge> list = new ArrayList<>();
        String sql = switch (role) {
            case SECRETAIRE -> "SELECT * FROM demande_conge WHERE status = 'PENDING_SECRETAIRE'";
            case RH         -> "SELECT * FROM demande_conge WHERE status = 'PENDING_RH'";
            case ADMIN      -> "SELECT * FROM demande_conge WHERE status = 'PENDING_ADMIN'";
            default         -> null;
        };

        if (sql == null) return list;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(extractDemande(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public void updateStatus(int demandeId, Status status) {
        String sql = "UPDATE demande_conge SET status = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, demandeId);
            stmt.executeUpdate();
            System.out.println("✅ Status updated.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<DemandeConge> findByStatus(Status status) {
        List<DemandeConge> list = new ArrayList<>();
        String sql = "SELECT * FROM demande_conge WHERE status = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(extractDemande(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<DemandeConge> findByDateRange(LocalDate start, LocalDate end) {
        List<DemandeConge> list = new ArrayList<>();
        String sql = "SELECT * FROM demande_conge WHERE start_date >= ? AND end_date <= ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(start));
            stmt.setDate(2, Date.valueOf(end));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(extractDemande(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    private DemandeConge extractDemande(ResultSet rs) throws SQLException {
        DemandeConge d = new DemandeConge();
        d.setId(rs.getInt("id"));
        d.setWorker(workerService.findById(rs.getInt("worker_id")));
        d.setStartDate(rs.getDate("start_date").toLocalDate());
        d.setEndDate(rs.getDate("end_date").toLocalDate());
        d.setType(TypeConge.valueOf(rs.getString("type")));
        d.setReason(rs.getString("reason"));
        d.setStatus(Status.valueOf(rs.getString("status")));

        int secId = rs.getInt("secretaire_decision_id");
        if (secId != 0) d.setSecretaireDecision(decisionService.findById(secId));

        int rhId = rs.getInt("rh_decision_id");
        if (rhId != 0) d.setRhDecision(decisionService.findById(rhId));

        int admId = rs.getInt("admin_decision_id");
        if (admId != 0) d.setAdminDecision(decisionService.findById(admId));

        return d;
    }
}
