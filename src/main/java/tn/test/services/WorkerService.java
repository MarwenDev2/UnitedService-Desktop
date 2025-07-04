package tn.test.services;

import tn.test.entities.Worker;
import tn.test.tools.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WorkerService implements CrudService<Worker> {

    private final Connection connection;

    public WorkerService() {
        connection = Database.getInstance().getConnection();
    }

    @Override
    public boolean add(Worker worker) {
        String sql = "INSERT INTO worker (name, cin, department, position, phone, email, salary, profile_image_path, gender, date_of_birth, address, creation_date, status, total_conge_days, used_conge_days) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, worker.getName());
            stmt.setString(2, worker.getCin());
            stmt.setString(3, worker.getDepartment());
            stmt.setString(4, worker.getPosition());
            stmt.setString(5, worker.getPhone());
            stmt.setString(6, worker.getEmail());
            stmt.setFloat(7, worker.getSalary());
            stmt.setString(8, worker.getProfileImagePath());
            stmt.setString(9, worker.getGender());
            stmt.setDate(10, Date.valueOf(worker.getDateOfBirth()));
            stmt.setString(11, worker.getAddress());
            stmt.setDate(12, Date.valueOf(worker.getCreationDate()));
            stmt.setString(13, worker.getStatus());
            stmt.setInt(14, worker.getTotalCongeDays());
            stmt.setInt(15, worker.getUsedCongeDays());

            int rows = stmt.executeUpdate();
            System.out.println("✅ Worker added successfully.");
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error adding worker: " + e.getMessage());
            return false;
        }
    }



    @Override
    public void update(Worker worker) {
        String sql = "UPDATE worker SET name = ?, cin = ?, department = ?, position = ?, phone = ?, email = ?, salary = ?, profile_image_path = ?, " +
                "gender = ?, date_of_birth = ?, address = ?, creation_date = ?, status = ?, total_conge_days = ?, used_conge_days = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, worker.getName());
            stmt.setString(2, worker.getCin());
            stmt.setString(3, worker.getDepartment());
            stmt.setString(4, worker.getPosition());
            stmt.setString(5, worker.getPhone());
            stmt.setString(6, worker.getEmail());
            stmt.setFloat(7, worker.getSalary());
            stmt.setString(8, worker.getProfileImagePath());
            stmt.setString(9, worker.getGender());
            stmt.setDate(10, Date.valueOf(worker.getDateOfBirth()));
            stmt.setString(11, worker.getAddress());
            stmt.setDate(12, Date.valueOf(worker.getCreationDate()));
            stmt.setString(13, worker.getStatus());
            stmt.setInt(14, worker.getTotalCongeDays());
            stmt.setInt(15, worker.getUsedCongeDays());
            stmt.setInt(16, worker.getId());

            stmt.executeUpdate();
            System.out.println("✅ Worker updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void delete(int id) {
        String sql = "DELETE FROM worker WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("🗑️ Worker deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Worker findById(int id) {
        String sql = "SELECT * FROM worker WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractWorker(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Worker> findAll() {
        List<Worker> workers = new ArrayList<>();
        String sql = "SELECT * FROM worker";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                workers.add(extractWorker(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return workers;
    }

    public Worker findByCin(String cin) {
        String sql = "SELECT * FROM worker WHERE cin = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cin);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractWorker(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Worker> findByStatus(String status) {
        List<Worker> workers = new ArrayList<>();
        String sql = "SELECT * FROM worker WHERE status = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                workers.add(extractWorker(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return workers;
    }

    public void updateCongeDays(int workerId, int usedDays) {
        String sql = "UPDATE worker SET used_conge_days = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, usedDays);
            stmt.setInt(2, workerId);
            stmt.executeUpdate();
            System.out.println("✅ Congé days updated.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Worker> findByDepartment(String department) {
        List<Worker> workers = new ArrayList<>();
        String sql = "SELECT * FROM worker WHERE department = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, department);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                workers.add(extractWorker(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return workers;
    }

    // Helper method
    private Worker extractWorker(ResultSet rs) throws SQLException {
        Worker worker = new Worker();
        worker.setId(rs.getInt("id"));
        worker.setName(rs.getString("name"));
        worker.setCin(rs.getString("cin"));
        worker.setDepartment(rs.getString("department"));
        worker.setPosition(rs.getString("position"));
        worker.setPhone(rs.getString("phone"));
        worker.setEmail(rs.getString("email"));
        worker.setSalary(rs.getFloat("salary"));
        worker.setProfileImagePath(rs.getString("profile_image_path"));
        worker.setGender(rs.getString("gender"));
        worker.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
        worker.setAddress(rs.getString("address"));
        worker.setCreationDate(rs.getDate("creation_date").toLocalDate());
        worker.setStatus(rs.getString("status"));
        worker.setTotalCongeDays(rs.getInt("total_conge_days"));
        worker.setUsedCongeDays(rs.getInt("used_conge_days"));
        return worker;
    }
}
