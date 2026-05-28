package view;

import dao.LibroDAO;
import model.Libro;
import model.Socio;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VentanaPrincipal extends JFrame {
    // Instancias de acceso a datos (DAO)
    private LibroDAO libroDAO = new LibroDAO();
    private dao.SocioDAO socioDAO = new dao.SocioDAO();
    private dao.PrestamoDAO prestamoDAO = new dao.PrestamoDAO();

    // Componentes de la tabla de Libros
    private JTable tablaLibros;
    private DefaultTableModel modeloTabla;

    // Componentes de la tabla de Socios
    private JTable tablaSocios;
    private DefaultTableModel modeloTablaSocios;

    // Componentes de la tabla de Préstamos
    private JTable tablaPrestamos;
    private DefaultTableModel modeloTablaPrestamos;

    public VentanaPrincipal() {
        setTitle("Sistema de Gestión de Biblioteca");
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane pestañas = new JTabbedPane();

        // Agregar las tres pestañas principales de la aplicación
        pestañas.addTab("Inventario de Libros", crearPanelLibros());
        pestañas.addTab("Gestión de Socios", crearPanelSocios());
        pestañas.addTab("Préstamos y Devoluciones", crearPanelPrestamos());

        add(pestañas);

        // Forzar la carga inicial de datos desde MySQL a las tablas de la interfaz
        actualizarTabla();
        actualizarTablaSocios();
        actualizarTablaPrestamos();
    }

    /**
     * PESTAÑA 1: INVENTARIO DE LIBROS
     */
    private JPanel crearPanelLibros() {
        JPanel panel = new JPanel(new BorderLayout());

        // Formulario superior para añadir libros
        JPanel panelFormulario = new JPanel(new GridLayout(4, 2, 5, 5));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Registrar Nuevo Libro"));
        
        JTextField txtTitulo = new JTextField();
        JTextField txtAutor = new JTextField();
        JTextField txtIsbn = new JTextField();
        JButton btnAgregar = new JButton("Agregar Libro");

        panelFormulario.add(new JLabel(" Título:")); panelFormulario.add(txtTitulo);
        panelFormulario.add(new JLabel(" Autor:")); panelFormulario.add(txtAutor);
        panelFormulario.add(new JLabel(" ISBN:")); panelFormulario.add(txtIsbn);
        panelFormulario.add(new JLabel("")); panelFormulario.add(btnAgregar);

        // Tabla central de libros
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Título", "Autor", "ISBN", "Disponible"}, 0);
        tablaLibros = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaLibros);

        panel.add(panelFormulario, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);

        // Evento para añadir libros con validación básica de campos vacíos
        btnAgregar.addActionListener(e -> {
            String titulo = txtTitulo.getText().trim();
            String autor = txtAutor.getText().trim();
            String isbn = txtIsbn.getText().trim();
            
            if(!titulo.isEmpty() && !autor.isEmpty() && !isbn.isEmpty()) {
                Libro nuevoLibro = new Libro(0, titulo, autor, isbn, 1);
                if(libroDAO.agregarLibro(nuevoLibro)) {
                    JOptionPane.showMessageDialog(this, "Libro agregado con éxito.");
                    actualizarTabla();
                    txtTitulo.setText(""); txtAutor.setText(""); txtIsbn.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Error al agregar el libro en la base de datos.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Por favor, llene todos los campos del formulario.");
            }
        });

        return panel;
    }

    /**
     * PESTAÑA 2: GESTIÓN DE SOCIOS
     */
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

        // Evento para añadir socios con validaciones de seguridad
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
                    JOptionPane.showMessageDialog(this, "Error al registrar el socio. Quizás el email ya esté duplicado.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Por favor, complete obligatoriamente Nombre y Email.");
            }
        });

        return panel;
    }

    /**
     * PESTAÑA 3: PRÉSTAMOS Y DEVOLUCIONES
     */
    private JPanel crearPanelPrestamos() {
        JPanel panel = new JPanel(new BorderLayout());

        // Formulario de operaciones de Préstamos (Superior)
        JPanel panelFormulario = new JPanel(new GridLayout(4, 2, 5, 5));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Registrar Préstamo / Devolución"));
        
        JTextField txtIdLibro = new JTextField();
        JTextField txtIdSocio = new JTextField();
        JTextField txtIdPrestamoDevolucion = new JTextField();
        
        JButton btnPrestar = new JButton("Registrar Préstamo");
        JButton btnDevolver = new JButton("Registrar Devolución");

        panelFormulario.add(new JLabel(" ID Libro (para prestar):")); panelFormulario.add(txtIdLibro);
        panelFormulario.add(new JLabel(" ID Socio (para prestar):")); panelFormulario.add(txtIdSocio);
        panelFormulario.add(new JLabel(" ID Préstamo (solo para devolución):")); panelFormulario.add(txtIdPrestamoDevolucion);
        panelFormulario.add(btnPrestar); panelFormulario.add(btnDevolver);

        // Tabla de Historial Inferior (Préstamos Activos con JOIN de nombres)
        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBorder(BorderFactory.createTitledBorder("Libros actualmente prestados"));
        
        modeloTablaPrestamos = new DefaultTableModel(new String[]{"ID Préstamo", "Libro", "Socio", "Fecha Préstamo"}, 0);
        tablaPrestamos = new JTable(modeloTablaPrestamos);
        JScrollPane scrollTabla = new JScrollPane(tablaPrestamos);
        panelTabla.add(scrollTabla, BorderLayout.CENTER);

        panel.add(panelFormulario, BorderLayout.NORTH);
        panel.add(panelTabla, BorderLayout.CENTER);

        // Evento para procesar un nuevo préstamo
        btnPrestar.addActionListener(e -> {
            try {
                int idLibro = Integer.parseInt(txtIdLibro.getText().trim());
                int idSocio = Integer.parseInt(txtIdSocio.getText().trim());
                
                if (prestamoDAO.registrarPrestamo(idLibro, idSocio)) {
                    JOptionPane.showMessageDialog(this, "¡Préstamo registrado con éxito! El stock ha disminuido.");
                    actualizarTabla(); 
                    actualizarTablaPrestamos(); 
                    txtIdLibro.setText(""); txtIdSocio.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo realizar el préstamo. Verifica si hay stock disponible o si los IDs existen.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Por favor, ingresa IDs numéricos válidos en los campos de préstamo.");
            }
        });

        // Evento para procesar una devolución
        btnDevolver.addActionListener(e -> {
            try {
                int idPrestamo = Integer.parseInt(txtIdPrestamoDevolucion.getText().trim());
                int idLibro = Integer.parseInt(txtIdLibro.getText().trim()); 
                
                if (prestamoDAO.registrarDevolucion(idPrestamo, idLibro)) {
                    JOptionPane.showMessageDialog(this, "¡Devolución registrada con éxito! El libro vuelve a estar disponible.");
                    actualizarTabla(); 
                    actualizarTablaPrestamos(); 
                    txtIdPrestamoDevolucion.setText(""); txtIdLibro.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Error al registrar la devolución. Verifica el ID del préstamo.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Completa el ID del Préstamo y el ID del Libro para efectuar la devolución.");
            }
        });

        return panel;
    }   

    /**
     * MÉTODOS DE ACTUALIZACIÓN DE DATOS (Sincronizan base de datos -> JTables)
     */
    private void actualizarTabla() {
        modeloTabla.setRowCount(0);
        List<Libro> libros = libroDAO.listarLibros();
        for (Libro l : libros) {
            modeloTabla.addRow(new Object[]{l.getIdLibro(), l.getTitulo(), l.getAutor(), l.getIsbn(), l.getDisponible()});
        }
    }

    private void actualizarTablaSocios() {
        modeloTablaSocios.setRowCount(0);
        List<Socio> socios = socioDAO.listarSocios();
        for (Socio s : socios) {
            modeloTablaSocios.addRow(new Object[]{s.getIdSocio(), s.getNombre(), s.getEmail(), s.getTelefono()});
        }
    }

    private void actualizarTablaPrestamos() {
        modeloTablaPrestamos.setRowCount(0);
        List<Object[]> prestamos = prestamoDAO.listarPrestamosActivos();
        for (Object[] row : prestamos) {
            modeloTablaPrestamos.addRow(row);
        }
    }

    /**
     * MÉTODO DE ENTRADA PRINCIPAL
     */
    public static void main(String[] args) {
        try {
            // Activamos el tema moderno oscuro (puedes cambiarlo por FlatLightLaf para tema claro)
            com.formdev.flatlaf.FlatDarkLaf.setup();
        } catch (Exception ex) {
            System.err.println("No se pudo inicializar el estilo moderno. Usando por defecto.");
        }

        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}