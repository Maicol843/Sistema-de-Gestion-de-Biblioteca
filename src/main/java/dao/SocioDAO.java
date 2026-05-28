package dao;

import config.DatabaseConfig;
import model.Socio;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SocioDAO {

    public boolean agregarSocio(Socio socio) {
        String sql = "INSERT INTO socios (nombre, email, telefono) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, socio.getNombre());
            pstmt.setString(2, socio.getEmail());
            pstmt.setString(3, socio.getTelefono());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Socio> listarSocios() {
        List<Socio> lista = new ArrayList<>();
        String sql = "SELECT * FROM socios";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                lista.add(new Socio(
                    rs.getInt("id_socio"),
                    rs.getString("nombre"),
                    rs.getString("email"),
                    rs.getString("telefono")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean modificarSocio(Socio socio) {
        String sql = "UPDATE socios SET nombre = ?, email = ?, telefono = ? WHERE id_socio = ?";
        try (java.sql.Connection con = DatabaseConfig.getConnection();
            java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, socio.getNombre());
            ps.setString(2, socio.getEmail());
            ps.setString(3, socio.getTelefono());
            ps.setInt(4, socio.getIdSocio());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean eliminarSocio(int id) {
        String sql = "DELETE FROM socios WHERE id_socio = ?";
        try (java.sql.Connection con = DatabaseConfig.getConnection();
            java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }   
}