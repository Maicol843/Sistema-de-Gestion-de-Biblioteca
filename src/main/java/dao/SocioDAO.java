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
}