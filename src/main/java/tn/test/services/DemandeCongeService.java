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
    public boolean add(DemandeConge demande) {
        String sql = """
    INSERT INTO demande_conge (worker_id, start_date, end_date, type, reason, status,
    secretaire_decision_id, rh_decision_id, admin_decision_id, date_demande)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
            stmt.setObject(10, demande.getDateDemande() != null ? Date.valueOf(demande.getDateDemande()) : null, Types.DATE);


            int rowsInserted = stmt.executeUpdate();
            System.out.println("✅ DemandeConge added.");
            return rowsInserted > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error adding DemandeConge: " + e.getMessage());
            return false;
        }
    }


    @Override
    public void update(DemandeConge demande) {
        String sql = """
        UPDATE demande_conge SET worker_id = ?, start_date = ?, end_date = ?, type = ?, reason = ?, status = ?, 
        secretaire_decision_id = ?, rh_decision_id = ?, admin_decision_id = ?, date_demande = ? WHERE id = ?
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
            stmt.setObject(10, demande.getDateDemande() != null ? Date.valueOf(demande.getDateDemande()) : null, Types.DATE);
            stmt.setInt(11, demande.getId());

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

    public int countByStatus(int workerId, String status) {
        String sql = "SELECT COUNT(*) FROM demande_conge WHERE worker_id = ? AND status = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, workerId);
            stmt.setString(2, status);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public List<DemandeConge> getRecentCongeByUser(int workerId, int limit) {
        List<DemandeConge> list = new ArrayList<>();
        String sql = "SELECT * FROM demande_conge WHERE worker_id = ? ORDER BY start_date DESC LIMIT ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, workerId);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(extractDemande(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean hasPendingRequest(int workerId) {
        String query = "SELECT COUNT(*) FROM demande_conge WHERE worker_id = ? AND status IN (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, workerId);
            stmt.setString(2, Status.EN_ATTENTE_SECRETAIRE.name());
            stmt.setString(3, Status.EN_ATTENTE_ADMIN.name());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public int countByStatus(Status status) {
        String sql = "SELECT COUNT(*) FROM demande_conge WHERE status = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void updateSecretaireStatus(int demandeId, boolean isApproved) {
        Status newStatus = isApproved ? Status.EN_ATTENTE_RH : Status.REFUSE_SECRETAIRE;
        updateStatus(demandeId, newStatus);
    }

    public void updateRHStatus(int demandeId, boolean isApproved) {
        Status newStatus = isApproved ? Status.EN_ATTENTE_ADMIN : Status.REFUSE_RH;
        updateStatus(demandeId, newStatus);
    }

    public void finalApprove(int demandeId, boolean isApproved) {
        Status newStatus = isApproved ? Status.ACCEPTE : Status.REFUSE_ADMIN;
        updateStatus(demandeId, newStatus);
    }


    public int countAll() {
        String sql = "SELECT COUNT(*) FROM demande_conge";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }



    private DemandeConge extractDemande(ResultSet rs) throws SQLException {
        DemandeConge d = new DemandeConge();
        d.setId(rs.getInt("id"));
        d.setWorker(workerService.findById(rs.getInt("worker_id")));
        d.setStartDate(rs.getDate("start_date").toLocalDate());
        d.setEndDate(rs.getDate("end_date").toLocalDate());

        // 🛠️ Safe mapping from DB string to Enum
        String dbType = rs.getString("type").toUpperCase();
        TypeConge type;
        switch (dbType) {
            case "PAID", "ANNUEL" -> type = TypeConge.ANNUEL;
            case "SICK", "MALADIE" -> type = TypeConge.MALADIE;
            case "MATERNITY", "MATERNITE" -> type = TypeConge.MATERNITE;
            case "UNPAID", "SANS_SOLDE" -> type = TypeConge.SANS_SOLDE;
            default -> type = TypeConge.AUTRE;
        }
        d.setType(type);

        d.setReason(rs.getString("reason"));

        String dbStatus = rs.getString("status").toUpperCase();
        try {
            d.setStatus(Status.valueOf(dbStatus));
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Unknown status: " + dbStatus);
            d.setStatus(Status.EN_ATTENTE_SECRETAIRE); // default fallback
        }

        int secId = rs.getInt("secretaire_decision_id");
        if (secId != 0) d.setSecretaireDecision(decisionService.findById(secId));

        int rhId = rs.getInt("rh_decision_id");
        if (rhId != 0) d.setRhDecision(decisionService.findById(rhId));

        int admId = rs.getInt("admin_decision_id");
        if (admId != 0) d.setAdminDecision(decisionService.findById(admId));

        if (rs.getDate("date_demande") != null)
            d.setDateDemande(rs.getDate("date_demande").toLocalDate());

        return d;
    }

}
