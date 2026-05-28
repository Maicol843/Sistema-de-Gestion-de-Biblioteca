package view;
import dao.LibroDAO;
import model.Libro;
import model.Socio;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VentanaPrincipal extends JFrame {
    private LibroDAO libroDAO = new LibroDAO();
    private JTable tablaLibros;
    private DefaultTableModel modeloTabla;
    private dao.SocioDAO socioDAO = new dao.SocioDAO();
    private JTable tablaSocios;
    private DefaultTableModel modeloTablaSocios;

    public VentanaPrincipal() {
        setTitle("Sistema de Gestión de Biblioteca");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane pestañas = new JTabbedPane();

        // Panel de Libros
        pestañas.addTab("Inventario de Libros", crearPanelLibros());
        // Panel de Socios
        pestañas.addTab("Gestión de Socios", crearPanelSocios());
        // Panel de Préstamos (puedes expandirlo de forma similar)
        pestañas.addTab("Préstamos y Devoluciones", crearPanelPrestamos());

        add(pestañas);
        actualizarTabla();
        actualizarTablaSocios();
    }

    private JPanel crearPanelLibros() {
        JPanel panel = new JPanel(new BorderLayout());

        // Formulario superior para añadir
        JPanel panelFormulario = new JPanel(new GridLayout(4, 2, 5, 5));
        JTextField txtTitulo = new JTextField();
        JTextField txtAutor = new JTextField();
        JTextField txtIsbn = new JTextField();
        JButton btnAgregar = new JButton("Agregar Libro");

        panelFormulario.add(new JLabel(" Título:")); panelFormulario.add(txtTitulo);
        panelFormulario.add(new JLabel(" Autor:")); panelFormulario.add(txtAutor);
        panelFormulario.add(new JLabel(" ISBN:")); panelFormulario.add(txtIsbn);
        panelFormulario.add(new JLabel("")); panelFormulario.add(btnAgregar);

        // Tabla central
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Título", "Autor", "ISBN", "Disponible"}, 0);
        tablaLibros = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaLibros);

        panel.add(panelFormulario, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);

        // Evento del botón
        btnAgregar.addActionListener(e -> {
            String titulo = txtTitulo.getText();
            String autor = txtAutor.getText();
            String isbn = txtIsbn.getText();
            
            if(!titulo.isEmpty() && !autor.isEmpty() && !isbn.isEmpty()) {
                Libro nuevoLibro = new Libro(0, titulo, autor, isbn, 1);
                if(libroDAO.agregarLibro(nuevoLibro)) {
                    JOptionPane.showMessageDialog(this, "Libro agregado con éxito.");
                    actualizarTabla();
                    txtTitulo.setText(""); txtAutor.setText(""); txtIsbn.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Error al agregar el libro.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Por favor, llene todos los campos.");
            }
        });

        return panel;
    }

    private JPanel crearPanelPrestamos() {
        JPanel panel = new JPanel(new BorderLayout());
        dao.PrestamoDAO prestamoDAO = new dao.PrestamoDAO();

        // Formulario de Préstamos
        JPanel panelFormulario = new JPanel(new GridLayout(4, 2, 5, 5));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Registrar Préstamo / Devolución"));
        
        JTextField txtIdLibro = new JTextField();
        JTextField txtIdSocio = new JTextField();
        JTextField txtIdPrestamoDevolucion = new JTextField();
        
        JButton btnPrestar = new JButton("Registrar Préstamo");
        JButton btnDevolver = new JButton("Registrar Devolución");

        // Sección Préstamo
        panelFormulario.add(new JLabel(" ID Libro (para prestar):")); panelFormulario.add(txtIdLibro);
        panelFormulario.add(new JLabel(" ID Socio (para prestar):")); panelFormulario.add(txtIdSocio);
        panelFormulario.add(new JLabel(" ID Préstamo (solo para devolución):")); panelFormulario.add(txtIdPrestamoDevolucion);
        panelFormulario.add(btnPrestar); panelFormulario.add(btnDevolver);

        panel.add(panelFormulario, BorderLayout.NORTH);

        // Evento del botón Prestar
        btnPrestar.addActionListener(e -> {
            try {
                int idLibro = Integer.parseInt(txtIdLibro.getText());
                int idSocio = Integer.parseInt(txtIdSocio.getText());
                
                if (prestamoDAO.registrarPrestamo(idLibro, idSocio)) {
                    JOptionPane.showMessageDialog(this, "¡Préstamo registrado con éxito! El stock del libro ha disminuido.");
                    actualizarTabla(); // Actualiza el stock en la otra pestaña
                    txtIdLibro.setText(""); txtIdSocio.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo realizar el préstamo. Verifica si el libro tiene copias disponibles.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Por favor, ingresa IDs numéricos válidos.");
            }
        });

        // Evento del botón Devolver
        btnDevolver.addActionListener(e -> {
            try {
                int idPrestamo = Integer.parseInt(txtIdPrestamoDevolucion.getText());
                // Para simplificar la interfaz, pediremos el ID del libro en el cuadro de arriba para saber a cuál sumarle stock
                int idLibro = Integer.parseInt(txtIdLibro.getText()); 
                
                if (prestamoDAO.registrarDevolucion(idPrestamo, idLibro)) {
                    JOptionPane.showMessageDialog(this, "¡Devolución registrada! El libro vuelve a estar disponible.");
                    actualizarTabla();
                    txtIdPrestamoDevolucion.setText(""); txtIdLibro.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Error al registrar la devolución.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Completa el ID del Préstamo y el ID del Libro para devolver.");
            }
        });

        return panel;
    }

    private void actualizarTabla() {
        modeloTabla.setRowCount(0);
        List<Libro> libros = libroDAO.listarLibros();
        for (Libro l : libros) {
            modeloTabla.addRow(new Object[]{l.getIdLibro(), l.getTitulo(), l.getAutor(), l.getIsbn(), l.getDisponible()});
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }

    private JPanel crearPanelSocios() {
        JPanel panel = new JPanel(new BorderLayout());

        // Formulario superior para añadir socios
        JPanel panelFormulario = new JPanel(new GridLayout(4, 2, 5, 5));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Registrar Nuevo Socio"));
        
        JTextField txtNombre = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtTelefono = new JTextField();
        JButton btnAgregarSocio = new JButton("Registrar Socio");

        panelFormulario.add(new JLabel(" Nombre Completo:")); panelFormulario.add(txtNombre);
        panelFormulario.add(new JLabel(" Email:")); panelFormulario.add(txtEmail);
        panelFormulario.add(new JLabel(" Teléfono:")); panelFormulario.add(txtTelefono);
        panelFormulario.add(new JLabel("")); panelFormulario.add(btnAgregarSocio);

        // Tabla central para listar socios
        modeloTablaSocios = new DefaultTableModel(new String[]{"ID Socio", "Nombre", "Email", "Teléfono"}, 0);
        tablaSocios = new JTable(modeloTablaSocios);
        JScrollPane scrollTabla = new JScrollPane(tablaSocios);

        panel.add(panelFormulario, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);

        // Evento del botón registrar socio
        btnAgregarSocio.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            String email = txtEmail.getText().trim();
            String telefono = txtTelefono.getText().trim();
            
            if (!nombre.isEmpty() && !email.isEmpty()) {
                Socio nuevoSocio = new Socio(0, nombre, email, telefono);
                if (socioDAO.agregarSocio(nuevoSocio)) {
                    JOptionPane.showMessageDialog(this, "Socio registrado con éxito.");
                    actualizarTablaSocios();
                    txtNombre.setText(""); txtEmail.setText(""); txtTelefono.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Error al registrar el socio. Quizás el email ya existe.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Por favor, complete al menos Nombre y Email.");
            }
        });

        return panel;
    }

    private void actualizarTablaSocios() {
        modeloTablaSocios.setRowCount(0);
        List<Socio> socios = socioDAO.listarSocios();
        for (Socio s : socios) {
            modeloTablaSocios.addRow(new Object[]{s.getIdSocio(), s.getNombre(), s.getEmail(), s.getTelefono()});
        }
    }
}