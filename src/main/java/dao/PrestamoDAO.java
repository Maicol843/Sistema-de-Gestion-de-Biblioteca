package dao;

import java.sql.*;
import java.time.LocalDate;

import config.DatabaseConfig;

public class PrestamoDAO {

    public boolean registrarPrestamo(int idLibro, int idSocio) {
        String sqlPrestamo = "INSERT INTO prestamos (id_libro, id_socio, fecha_prestamo) VALUES (?, ?, ?)";
        String sqlActualizarLibro = "UPDATE libros SET disponible = disponible - 1 WHERE id_libro = ? AND disponible > 0";

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // Iniciamos transacción

            // 1. Reducir stock del libro
            try (PreparedStatement pstLibro = conn.prepareStatement(sqlActualizarLibro)) {
                pstLibro.setInt(1, idLibro);
                int filasAfectadas = pstLibro.executeUpdate();
                if (filasAfectadas == 0) {
                    conn.rollback(); // No hay stock disponible
                    return false;
                }
            }

            // 2. Registrar el préstamo
            try (PreparedStatement pstPrestamo = conn.prepareStatement(sqlPrestamo)) {
                pstPrestamo.setInt(1, idLibro);
                pstPrestamo.setInt(2, idSocio);
                pstPrestamo.setDate(3, Date.valueOf(LocalDate.now()));
                pstPrestamo.executeUpdate();
            }

            conn.commit(); // Confirmamos los cambios
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) { try { conn.close(); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }

    public boolean registrarDevolucion(int idPrestamo, int idLibro) {
        String sqlDevolucion = "UPDATE prestamos SET fecha_devolucion = ? WHERE id_prestamo = ?";
        String sqlActualizarLibro = "UPDATE libros SET disponible = disponible + 1 WHERE id_libro = ?";

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            // 1. Marcar fecha de devolución
            try (PreparedStatement pstPrestamo = conn.prepareStatement(sqlDevolucion)) {
                pstPrestamo.setDate(1, Date.valueOf(LocalDate.now()));
                pstPrestamo.setInt(2, idPrestamo);
                pstPrestamo.executeUpdate();
            }

            // 2. Devolver stock al libro
            try (PreparedStatement pstLibro = conn.prepareStatement(sqlActualizarLibro)) {
                pstLibro.setInt(1, idLibro);
                pstLibro.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) { try { conn.close(); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }

    public java.util.List<Object[]> listarPrestamosActivos() {
        java.util.List<Object[]> lista = new java.util.ArrayList<>();
        // Consulta que junta las 3 tablas para traer nombres en vez de solo números
        String sql = "SELECT p.id_prestamo, l.titulo, s.nombre, p.fecha_prestamo " +
                    "FROM prestamos p " +
                    "JOIN libros l ON p.id_libro = l.id_libro " +
                    "JOIN socios s ON p.id_socio = s.id_socio " +
                    "WHERE p.fecha_devolucion IS NULL"; // Solo los que no se han devuelto

        try (Connection conn = config.DatabaseConfig.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                lista.add(new Object[]{
                    rs.getInt("id_prestamo"),
                    rs.getString("titulo"),
                    rs.getString("nombre"),
                    rs.getDate("fecha_prestamo")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}