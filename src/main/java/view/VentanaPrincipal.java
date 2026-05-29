package view;

import dao.LibroDAO;
import model.Libro;
import model.Socio;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.util.List;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VentanaPrincipal extends JFrame {
    // Instancias de acceso a datos (DAO)
    private LibroDAO libroDAO = new LibroDAO();
    private dao.SocioDAO socioDAO = new dao.SocioDAO();
    private dao.PrestamoDAO prestamoDAO = new dao.PrestamoDAO();

    // Componentes de la tabla de Libros
    private JTable tablaLibros;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorterLibros; 

    // Componentes de la tabla de Socios
    private JTable tablaSocios;
    private DefaultTableModel modeloTablaSocios;
    private TableRowSorter<DefaultTableModel> sorterSocios; 

    // Componentes de la tabla de Préstamos y su nuevo buscador
    private JTable tablaPrestamos;
    private DefaultTableModel modeloTablaPrestamos;
    private TableRowSorter<DefaultTableModel> sorterPrestamos; // Sorter para préstamos

    // Variables de control para saber qué ID se está editando
    private int idLibroSeleccionado = -1;
    private int idSocioSeleccionado = -1;

    public VentanaPrincipal() {
        setTitle("Sistema de Gestión de Biblioteca");
        setSize(950, 750); // Incrementado ligeramente para dar espacio al buscador de préstamos
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane pestañas = new JTabbedPane();

        pestañas.addTab("Inventario de Libros", crearPanelLibros());
        pestañas.addTab("Gestión de Socios", crearPanelSocios());
        pestañas.addTab("Préstamos y Devoluciones", crearPanelPrestamos());

        add(pestañas);

        actualizarTabla();
        actualizarTablaSocios();
        actualizarTablaPrestamos();
    }

    /**
     * PESTAÑA 1: INVENTARIO DE LIBROS (ALTAS, BAJAS, MODIFICACIONES)
     */
    private JPanel crearPanelLibros() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelFormulario = new JPanel(new GridLayout(5, 2, 8, 8));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Formulario de Libros (Registrar / Editar)"));
        
        JTextField txtTitulo = new JTextField();
        JTextField txtAutor = new JTextField();
        JTextField txtIsbn = new JTextField();
        
        JButton btnAgregar = new JButton("Agregar Nuevo");
        JButton btnModificar = new JButton("Guardar Cambios");
        JButton btnEliminar = new JButton("Eliminar Seleccionado");
        btnModificar.setEnabled(false);
        btnEliminar.setEnabled(false);

        panelFormulario.add(new JLabel(" Título:")); panelFormulario.add(txtTitulo);
        panelFormulario.add(new JLabel(" Autor:")); panelFormulario.add(txtAutor);
        panelFormulario.add(new JLabel(" ISBN:")); panelFormulario.add(txtIsbn);
        
        JPanel panelBotonesAccion = new JPanel(new GridLayout(1, 3, 5, 5));
        panelBotonesAccion.add(btnAgregar);
        panelBotonesAccion.add(btnModificar);
        panelBotonesAccion.add(btnEliminar);
        
        panelFormulario.add(new JLabel(" Acciones:")); panelFormulario.add(panelBotonesAccion);

        JPanel panelBuscar = new JPanel(new BorderLayout(5, 5));
        panelBuscar.setBorder(BorderFactory.createTitledBorder("Buscar Libro (Filtro en tiempo real)"));
        JTextField txtBuscarLibro = new JTextField();
        panelBuscar.add(new JLabel(" Escribe el Título o Autor a buscar: "), BorderLayout.WEST);
        panelBuscar.add(txtBuscarLibro, BorderLayout.CENTER);

        JPanel panelNorte = new JPanel(new BorderLayout(5, 5));
        panelNorte.add(panelFormulario, BorderLayout.NORTH);
        panelNorte.add(panelBuscar, BorderLayout.SOUTH);

        modeloTabla = new DefaultTableModel(new String[]{"ID", "Título", "Autor", "ISBN", "Disponible"}, 0);
        tablaLibros = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaLibros);

        sorterLibros = new TableRowSorter<>(modeloTabla);
        tablaLibros.setRowSorter(sorterLibros);

        panel.add(panelNorte, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);

        tablaLibros.getSelectionModel().addListSelectionListener(e -> {
            int filaSeleccionada = tablaLibros.getSelectedRow();
            if (filaSeleccionada != -1) {
                int filaModelo = tablaLibros.convertRowIndexToModel(filaSeleccionada);
                
                idLibroSeleccionado = Integer.parseInt(modeloTabla.getValueAt(filaModelo, 0).toString());
                txtTitulo.setText(modeloTabla.getValueAt(filaModelo, 1).toString());
                txtAutor.setText(modeloTabla.getValueAt(filaModelo, 2).toString());
                txtIsbn.setText(modeloTabla.getValueAt(filaModelo, 3).toString());
                
                btnAgregar.setEnabled(false);
                btnModificar.setEnabled(true);
                btnEliminar.setEnabled(true);
            }
        });

        txtBuscarLibro.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
            private void filtrar() {
                String texto = txtBuscarLibro.getText().trim();
                if (texto.isEmpty()) { sorterLibros.setRowFilter(null); } 
                else { sorterLibros.setRowFilter(RowFilter.regexFilter("(?i)" + texto, 1, 2)); }
            }
        });

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
                }
            } else { JOptionPane.showMessageDialog(this, "Complete todos los campos."); }
        });

        btnModificar.addActionListener(e -> {
            String titulo = txtTitulo.getText().trim();
            String autor = txtAutor.getText().trim();
            String isbn = txtIsbn.getText().trim();
            if(idLibroSeleccionado != -1 && !titulo.isEmpty() && !autor.isEmpty() && !isbn.isEmpty()) {
                int disponible = Integer.parseInt(modeloTabla.getValueAt(tablaLibros.convertRowIndexToModel(tablaLibros.getSelectedRow()), 4).toString());
                Libro libroEditado = new Libro(idLibroSeleccionado, titulo, autor, isbn, disponible);
                
                if(libroDAO.modificarLibro(libroEditado)) {
                    JOptionPane.showMessageDialog(this, "Libro actualizado con éxito.");
                    actualizarTabla();
                    idLibroSeleccionado = -1;
                    txtTitulo.setText(""); txtAutor.setText(""); txtIsbn.setText("");
                    btnAgregar.setEnabled(true); btnModificar.setEnabled(false); btnEliminar.setEnabled(false);
                    tablaLibros.clearSelection();
                }
            }
        });

        btnEliminar.addActionListener(e -> {
            if (idLibroSeleccionado != -1) {
                int disponible = Integer.parseInt(modeloTabla.getValueAt(tablaLibros.convertRowIndexToModel(tablaLibros.getSelectedRow()), 4).toString());
                if (disponible == 0) {
                    JOptionPane.showMessageDialog(this, "No se puede eliminar el libro porque se encuentra prestado actualmente.", "Baja Denegada", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                int respuesta = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar este libro de forma permanente?", "Confirmar Baja", JOptionPane.YES_NO_OPTION);
                if (respuesta == JOptionPane.YES_OPTION) {
                    if (libroDAO.eliminarLibro(idLibroSeleccionado)) {
                        JOptionPane.showMessageDialog(this, "Libro eliminado correctamente.");
                        actualizarTabla();
                        idLibroSeleccionado = -1;
                        txtTitulo.setText(""); txtAutor.setText(""); txtIsbn.setText("");
                        btnAgregar.setEnabled(true); btnModificar.setEnabled(false); btnEliminar.setEnabled(false);
                        tablaLibros.clearSelection();
                    }
                }
            }
        });

        return panel;
    }

    /**
     * PESTAÑA 2: GESTIÓN DE SOCIOS (ALTAS, BAJAS, MODIFICACIONES)
     */
    private JPanel crearPanelSocios() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelFormulario = new JPanel(new GridLayout(5, 2, 8, 8));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Formulario de Socios (Registrar / Editar)"));
        
        JTextField txtNombre = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtTelefono = new JTextField();
        
        JButton btnAgregarSocio = new JButton("Registrar Socio");
        JButton btnModificarSocio = new JButton("Guardar Cambios");
        JButton btnEliminarSocio = new JButton("Dar de Baja");
        btnModificarSocio.setEnabled(false);
        btnEliminarSocio.setEnabled(false);

        panelFormulario.add(new JLabel(" Nombre Completo:")); panelFormulario.add(txtNombre);
        panelFormulario.add(new JLabel(" Email:")); panelFormulario.add(txtEmail);
        panelFormulario.add(new JLabel(" Teléfono:")); panelFormulario.add(txtTelefono);
        
        JPanel panelBotonesAccion = new JPanel(new GridLayout(1, 3, 5, 5));
        panelBotonesAccion.add(btnAgregarSocio);
        panelBotonesAccion.add(btnModificarSocio);
        panelBotonesAccion.add(btnEliminarSocio);
        panelFormulario.add(new JLabel(" Acciones:")); panelFormulario.add(panelBotonesAccion);

        JPanel panelBuscar = new JPanel(new BorderLayout(5, 5));
        panelBuscar.setBorder(BorderFactory.createTitledBorder("Buscar Socio (Filtro en tiempo real)"));
        JTextField txtBuscarSocio = new JTextField();
        panelBuscar.add(new JLabel(" Escribe el Nombre del socio: "), BorderLayout.WEST);
        panelBuscar.add(txtBuscarSocio, BorderLayout.CENTER);

        JPanel panelNorte = new JPanel(new BorderLayout(5, 5));
        panelNorte.add(panelFormulario, BorderLayout.NORTH);
        panelNorte.add(panelBuscar, BorderLayout.SOUTH);

        modeloTablaSocios = new DefaultTableModel(new String[]{"ID Socio", "Nombre", "Email", "Teléfono"}, 0);
        tablaSocios = new JTable(modeloTablaSocios);
        JScrollPane scrollTabla = new JScrollPane(tablaSocios);

        sorterSocios = new TableRowSorter<>(modeloTablaSocios);
        tablaSocios.setRowSorter(sorterSocios);

        panel.add(panelNorte, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);

        tablaSocios.getSelectionModel().addListSelectionListener(e -> {
            int filaSeleccionada = tablaSocios.getSelectedRow();
            if (filaSeleccionada != -1) {
                int filaModelo = tablaSocios.convertRowIndexToModel(filaSeleccionada);
                
                idSocioSeleccionado = Integer.parseInt(modeloTablaSocios.getValueAt(filaModelo, 0).toString());
                txtNombre.setText(modeloTablaSocios.getValueAt(filaModelo, 1).toString());
                txtEmail.setText(modeloTablaSocios.getValueAt(filaModelo, 2).toString());
                txtTelefono.setText(modeloTablaSocios.getValueAt(filaModelo, 3) != null ? modeloTablaSocios.getValueAt(filaModelo, 3).toString() : "");
                
                btnAgregarSocio.setEnabled(false);
                btnModificarSocio.setEnabled(true);
                btnEliminarSocio.setEnabled(true);
            }
        });

        txtBuscarSocio.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
            private void filtrar() {
                String texto = txtBuscarSocio.getText().trim();
                if (texto.isEmpty()) { sorterSocios.setRowFilter(null); } 
                else { sorterSocios.setRowFilter(RowFilter.regexFilter("(?i)" + texto, 1)); }
            }
        });

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
                }
            } else { JOptionPane.showMessageDialog(this, "Nombre y Email obligatorios."); }
        });

        btnModificarSocio.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            String email = txtEmail.getText().trim();
            String telefono = txtTelefono.getText().trim();
            if (idSocioSeleccionado != -1 && !nombre.isEmpty() && !email.isEmpty()) {
                Socio socioEditado = new Socio(idSocioSeleccionado, nombre, email, telefono);
                if (socioDAO.modificarSocio(socioEditado)) {
                    JOptionPane.showMessageDialog(this, "Datos del socio actualizados.");
                    actualizarTablaSocios();
                    idSocioSeleccionado = -1;
                    txtNombre.setText(""); txtEmail.setText(""); txtTelefono.setText("");
                    btnAgregarSocio.setEnabled(true); btnModificarSocio.setEnabled(false); btnEliminarSocio.setEnabled(false);
                    tablaSocios.clearSelection();
                }
            }
        });

        btnEliminarSocio.addActionListener(e -> {
            if (idSocioSeleccionado != -1) {
                String nombreSocio = modeloTablaSocios.getValueAt(tablaSocios.convertRowIndexToModel(tablaSocios.getSelectedRow()), 1).toString();
                
                boolean tieneDeudas = prestamoDAO.listarPrestamosActivos().stream()
                        .anyMatch(fila -> fila[2] != null && fila[2].toString().equalsIgnoreCase(nombreSocio));
                
                if (tieneDeudas) {
                    JOptionPane.showMessageDialog(this, "No se puede dar de baja al socio porque tiene libros bajo préstamo activo.", "Baja Denegada", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int respuesta = JOptionPane.showConfirmDialog(this, "¿Desea eliminar permanentemente a este socio?", "Confirmar Baja", JOptionPane.YES_NO_OPTION);
                if (respuesta == JOptionPane.YES_OPTION) {
                    if (socioDAO.eliminarSocio(idSocioSeleccionado)) {
                        JOptionPane.showMessageDialog(this, "Socio eliminado correctamente.");
                        actualizarTablaSocios();
                        idSocioSeleccionado = -1;
                        txtNombre.setText(""); txtEmail.setText(""); txtTelefono.setText("");
                        btnAgregarSocio.setEnabled(true); btnModificarSocio.setEnabled(false); btnEliminarSocio.setEnabled(false);
                        tablaSocios.clearSelection();
                    }
                }
            }
        });

        return panel;
    }

    /**
     * PESTAÑA 3: PRÉSTAMOS Y DEVOLUCIONES 
     */
    private JPanel crearPanelPrestamos() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelFormulario = new JPanel(new GridLayout(4, 2, 8, 8));
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

        // Panel de búsqueda específico para los préstamos 
        JPanel panelBuscarPrestamo = new JPanel(new BorderLayout(5, 5));
        panelBuscarPrestamo.setBorder(BorderFactory.createTitledBorder("Buscar Préstamo Activo (Filtro en tiempo real)"));
        JTextField txtBuscarPrestamo = new JTextField();
        panelBuscarPrestamo.add(new JLabel(" Escribe el Libro o Socio a buscar: "), BorderLayout.WEST);
        panelBuscarPrestamo.add(txtBuscarPrestamo, BorderLayout.CENTER);

        // Agrupamos el Formulario y el Buscador en un panel contenedor al Norte
        JPanel panelNortePestaña = new JPanel(new BorderLayout(5, 5));
        panelNortePestaña.add(panelFormulario, BorderLayout.NORTH);
        panelNortePestaña.add(panelBuscarPrestamo, BorderLayout.SOUTH);

        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBorder(BorderFactory.createTitledBorder("Libros actualmente prestados"));
        
        modeloTablaPrestamos = new DefaultTableModel(new String[]{"ID Préstamo", "Libro", "Socio", "Fecha Préstamo"}, 0);
        tablaPrestamos = new JTable(modeloTablaPrestamos);
        JScrollPane scrollTabla = new JScrollPane(tablaPrestamos);
        panelTabla.add(scrollTabla, BorderLayout.CENTER);

        // Sorter y DocumentListener para Préstamos 
        sorterPrestamos = new TableRowSorter<>(modeloTablaPrestamos);
        tablaPrestamos.setRowSorter(sorterPrestamos);

        txtBuscarPrestamo.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
            private void filtrar() {
                String texto = txtBuscarPrestamo.getText().trim();
                if (texto.isEmpty()) { 
                    sorterPrestamos.setRowFilter(null); 
                } else { 
                    // Filtra buscando coincidencias en la columna Libro (columna 1) o Socio (columna 2)
                    sorterPrestamos.setRowFilter(RowFilter.regexFilter("(?i)" + texto, 1, 2)); 
                }
            }
        });

        tablaPrestamos.getSelectionModel().addListSelectionListener(e -> {
            int fila = tablaPrestamos.getSelectedRow();
            if (fila != -1) {
                int filaModelo = tablaPrestamos.convertRowIndexToModel(fila);
                txtIdPrestamoDevolucion.setText(modeloTablaPrestamos.getValueAt(filaModelo, 0).toString());
            }
        });

        JButton btnExportarPDF = new JButton("Exportar Reporte en PDF");
        btnExportarPDF.setFont(new java.awt.Font("Helvetica", java.awt.Font.BOLD, 12));
        
        JPanel panelBotonPDF = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotonPDF.add(btnExportarPDF);
        panelTabla.add(panelBotonPDF, BorderLayout.SOUTH); 

        btnExportarPDF.addActionListener(e -> exportarReportePDF());

        panel.add(panelNortePestaña, BorderLayout.NORTH);
        panel.add(panelTabla, BorderLayout.CENTER);

        btnPrestar.addActionListener(e -> {
            try {
                String txtLibro = txtIdLibro.getText().trim();
                String txtSocio = txtIdSocio.getText().trim();
                
                if (txtLibro.isEmpty() || txtSocio.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Complete ID Libro e ID Socio.", "Campos Incompletos", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int idLibro = Integer.parseInt(txtLibro);
                int idSocio = Integer.parseInt(txtSocio);
                
                model.Socio socioExistente = socioDAO.listarSocios().stream()
                        .filter(s -> s.getIdSocio() == idSocio).findFirst().orElse(null);

                if (socioExistente == null) {
                    JOptionPane.showMessageDialog(this, "El ID de Socio no existe.", "Socio No Encontrado", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                model.Libro libroExistente = libroDAO.listarLibros().stream()
                        .filter(l -> l.getIdLibro() == idLibro).findFirst().orElse(null);

                if (libroExistente == null) {
                    JOptionPane.showMessageDialog(this, "El ID de Libro no existe.", "Libro No Encontrado", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (libroExistente.getDisponible() == 0) {
                    JOptionPane.showMessageDialog(this, "El libro ya está prestado.", "Sin Stock", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                long prestamosActivosDelSocio = prestamoDAO.listarPrestamosActivos().stream()
                        .filter(fila -> fila[2] != null && fila[2].toString().equalsIgnoreCase(socioExistente.getNombre()))
                        .count();

                if (prestamosActivosDelSocio >= 3) {
                    JOptionPane.showMessageDialog(this, "El socio ya tiene 3 libros sin devolver.", "Límite Superado", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (prestamoDAO.registrarPrestamo(idLibro, idSocio)) {
                    JOptionPane.showMessageDialog(this, "¡Préstamo registrado con éxito!");
                    actualizarTabla();
                    actualizarTablaPrestamos();
                    txtIdLibro.setText(""); txtIdSocio.setText("");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ingrese números válidos.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnDevolver.addActionListener(e -> {
            try {
                int idPrestamo = Integer.parseInt(txtIdPrestamoDevolucion.getText().trim());
                
                int idLibro = -1;
                List<Object[]> activos = prestamoDAO.listarPrestamosActivos();
                for (Object[] row : activos) {
                    if (Integer.parseInt(row[0].toString()) == idPrestamo) {
                        String tituloLibro = row[1].toString();
                        Libro lab = libroDAO.listarLibros().stream()
                                .filter(l -> l.getTitulo().equalsIgnoreCase(tituloLibro)).findFirst().orElse(null);
                        if (lab != null) idLibro = lab.getIdLibro();
                        break;
                    }
                }

                if (idLibro == -1) {
                    JOptionPane.showMessageDialog(this, "ID de préstamo inválido o ya devuelto.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (prestamoDAO.registrarDevolucion(idPrestamo, idLibro)) {
                    JOptionPane.showMessageDialog(this, "¡Devolución registrada con éxito!");
                    actualizarTabla();
                    actualizarTablaPrestamos();
                    txtIdPrestamoDevolucion.setText("");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Complete el ID del Préstamo.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
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
     * REPORTE PDF UNIFICADO: Lee directamente de las filas visibles del JTable respetando el filtro
     */
    private void exportarReportePDF() {
        int filasVisibles = tablaPrestamos.getRowCount(); // Obtiene las filas filtradas visibles
        if (filasVisibles == 0) {
            JOptionPane.showMessageDialog(this, "No hay préstamos visibles en la tabla para exportar.", "Reporte Vacío", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser selectorArchivos = new JFileChooser();
        selectorArchivos.setSelectedFile(new File("Reporte_Prestamos_Visibles.pdf"));
        if (selectorArchivos.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        String rutaCompleta = selectorArchivos.getSelectedFile().getAbsolutePath();
        if (!rutaCompleta.toLowerCase().endsWith(".pdf")) rutaCompleta += ".pdf";

        Document documento = new Document(PageSize.A4, 36, 36, 54, 36);
        try {
            PdfWriter.getInstance(documento, new FileOutputStream(rutaCompleta));
            documento.open();

            com.itextpdf.text.Font fuenteTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 22, com.itextpdf.text.Font.BOLD, new BaseColor(41, 128, 185));
            com.itextpdf.text.Font fuenteSubtitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.ITALIC, BaseColor.GRAY);
            com.itextpdf.text.Font fuenteTexto = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);
            com.itextpdf.text.Font fuenteEncabezadoTabla = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.BOLD, BaseColor.WHITE);

            Paragraph titulo = new Paragraph("SISTEMA DE GESTIÓN DE BIBLIOTECA", fuenteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);

            Paragraph reporteNombre = new Paragraph("Reporte de Préstamos Activos (Vista Filtrada)", new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD, BaseColor.DARK_GRAY));
            reporteNombre.setAlignment(Element.ALIGN_CENTER); documento.add(reporteNombre);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            Paragraph fecha = new Paragraph("Generado el: " + dtf.format(LocalDateTime.now()), fuenteSubtitulo);
            fecha.setAlignment(Element.ALIGN_CENTER); fecha.setSpacingAfter(20); documento.add(fecha);

            PdfPTable tablaPDF = new PdfPTable(4);
            tablaPDF.setWidthPercentage(100); tablaPDF.setWidths(new float[]{1.5f, 4f, 4f, 2.5f});

            String[] encabezados = {"ID Préstamo", "Libro Prestado", "Socio / Deudor", "Fecha de Préstamo"};
            for (String enc : encabezados) {
                PdfPCell celda = new PdfPCell(new Phrase(enc, fuenteEncabezadoTabla));
                celda.setBackgroundColor(new BaseColor(44, 62, 80)); celda.setHorizontalAlignment(Element.ALIGN_CENTER); celda.setPadding(8);
                tablaPDF.addCell(celda);
            }

            // Recorre exclusivamente las filas visibles de la vista filtrada en pantalla
            for (int i = 0; i < filasVisibles; i++) {
                for (int j = 0; j < 4; j++) {
                    Object valorCelda = tablaPrestamos.getValueAt(i, j); // Lee directamente de la JTable con sorter aplicado
                    String dato = (valorCelda != null) ? valorCelda.toString() : "";
                    
                    PdfPCell celda = new PdfPCell(new Phrase(dato, fuenteTexto));
                    celda.setHorizontalAlignment(j == 0 || j == 3 ? Element.ALIGN_CENTER : Element.ALIGN_LEFT);
                    celda.setPadding(6);
                    tablaPDF.addCell(celda);
                }
            }
            documento.add(tablaPDF);
            documento.close();
            JOptionPane.showMessageDialog(this, "PDF generado con éxito.");
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public static void main(String[] args) {
        try { com.formdev.flatlaf.FlatDarkLaf.setup(); } catch (Exception ex) {}
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}