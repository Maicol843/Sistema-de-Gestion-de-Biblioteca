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
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Formulario superior para añadir libros
        JPanel panelFormulario = new JPanel(new GridLayout(4, 2, 8, 8));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Registrar Nuevo Libro"));
        
        JTextField txtTitulo = new JTextField();
        JTextField txtAutor = new JTextField();
        JTextField txtIsbn = new JTextField();
        JButton btnAgregar = new JButton("Agregar Libro");

        panelFormulario.add(new JLabel(" Título:")); panelFormulario.add(txtTitulo);
        panelFormulario.add(new JLabel(" Autor:")); panelFormulario.add(txtAutor);
        panelFormulario.add(new JLabel(" ISBN:")); panelFormulario.add(txtIsbn);
        panelFormulario.add(new JLabel("")); panelFormulario.add(btnAgregar);

        // Panel para el buscador dinámico de Libros
        JPanel panelBuscar = new JPanel(new BorderLayout(5, 5));
        panelBuscar.setBorder(BorderFactory.createTitledBorder("Buscar Libro (Filtro en tiempo real)"));
        JTextField txtBuscarLibro = new JTextField();
        panelBuscar.add(new JLabel(" Escribe el Título o Autor a buscar: "), BorderLayout.WEST);
        panelBuscar.add(txtBuscarLibro, BorderLayout.CENTER);

        // Contenedor para juntar el formulario y el buscador en la zona norte
        JPanel panelNorte = new JPanel(new BorderLayout(5, 5));
        panelNorte.add(panelFormulario, BorderLayout.NORTH);
        panelNorte.add(panelBuscar, BorderLayout.SOUTH);

        // Tabla de libros
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Título", "Autor", "ISBN", "Disponible"}, 0);
        tablaLibros = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaLibros);

        // Configurar el enrutador de filtrado (Sorter) para los Libros
        sorterLibros = new TableRowSorter<>(modeloTabla);
        tablaLibros.setRowSorter(sorterLibros);

        panel.add(panelNorte, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);

        // Escuchador dinámico (DocumentListener) para filtrar mientras se escribe
        txtBuscarLibro.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filtrar(); }

            private void filtrar() {
                String texto = txtBuscarLibro.getText().trim();
                if (texto.isEmpty()) {
                    sorterLibros.setRowFilter(null);
                } else {
                    sorterLibros.setRowFilter(RowFilter.regexFilter("(?i)" + texto, 1, 2));
                }
            }
        });

        // Evento para añadir libros
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
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Formulario superior para añadir socios
        JPanel panelFormulario = new JPanel(new GridLayout(4, 2, 8, 8));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Registrar Nuevo Socio"));
        
        JTextField txtNombre = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtTelefono = new JTextField();
        JButton btnAgregarSocio = new JButton("Registrar Socio");

        panelFormulario.add(new JLabel(" Nombre Completo:")); panelFormulario.add(txtNombre);
        panelFormulario.add(new JLabel(" Email:")); panelFormulario.add(txtEmail);
        panelFormulario.add(new JLabel(" Teléfono:")); panelFormulario.add(txtTelefono);
        panelFormulario.add(new JLabel("")); panelFormulario.add(btnAgregarSocio);

        // Panel para el buscador dinámico de Socios
        JPanel panelBuscar = new JPanel(new BorderLayout(5, 5));
        panelBuscar.setBorder(BorderFactory.createTitledBorder("Buscar Socio (Filtro en tiempo real)"));
        JTextField txtBuscarSocio = new JTextField();
        panelBuscar.add(new JLabel(" Escribe el Nombre del socio: "), BorderLayout.WEST);
        panelBuscar.add(txtBuscarSocio, BorderLayout.CENTER);

        // Contenedor para juntar el formulario y el buscador
        JPanel panelNorte = new JPanel(new BorderLayout(5, 5));
        panelNorte.add(panelFormulario, BorderLayout.NORTH);
        panelNorte.add(panelBuscar, BorderLayout.SOUTH);

        // Tabla de socios
        modeloTablaSocios = new DefaultTableModel(new String[]{"ID Socio", "Nombre", "Email", "Teléfono"}, 0);
        tablaSocios = new JTable(modeloTablaSocios);
        JScrollPane scrollTabla = new JScrollPane(tablaSocios);

        // Configurar el enrutador de filtrado (Sorter) para los Socios
        sorterSocios = new TableRowSorter<>(modeloTablaSocios);
        tablaSocios.setRowSorter(sorterSocios);

        panel.add(panelNorte, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);

        // Escuchador dinámico para el campo de búsqueda de socios
        txtBuscarSocio.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filtrar(); }

            private void filtrar() {
                String texto = txtBuscarSocio.getText().trim();
                if (texto.isEmpty()) {
                    sorterSocios.setRowFilter(null);
                } else {
                    sorterSocios.setRowFilter(RowFilter.regexFilter("(?i)" + texto, 1));
                }
            }
        });

        // Evento para añadir socios
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
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Formulario de operaciones de Préstamos (Superior)
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

        // Tabla de Historial Inferior
        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBorder(BorderFactory.createTitledBorder("Libros actualmente prestados"));
        
        modeloTablaPrestamos = new DefaultTableModel(new String[]{"ID Préstamo", "Libro", "Socio", "Fecha Préstamo"}, 0);
        tablaPrestamos = new JTable(modeloTablaPrestamos);
        JScrollPane scrollTabla = new JScrollPane(tablaPrestamos);
        panelTabla.add(scrollTabla, BorderLayout.CENTER);

        // Botón de exportación a PDF (Especificamos java.awt.Font para evitar el choque)
        JButton btnExportarPDF = new JButton("Exportar Reporte en PDF");
        btnExportarPDF.setFont(new java.awt.Font("Helvetica", java.awt.Font.BOLD, 12));
        
        JPanel panelBotonPDF = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotonPDF.add(btnExportarPDF);
        panelTabla.add(panelBotonPDF, BorderLayout.SOUTH); 

        // Evento para gatillar la creación del PDF al hacer clic
        btnExportarPDF.addActionListener(e -> exportarReportePDF());

        panel.add(panelFormulario, BorderLayout.NORTH);
        panel.add(panelTabla, BorderLayout.CENTER);

        // Evento del botón Prestar
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

        // Evento del botón Devolver
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
     * MÉTODOS DE ACTUALIZACIÓN DE DATOS
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

    private void exportarReportePDF() {
        // 1. Obtener los préstamos activos desde el DAO
        List<Object[]> prestamosActivos = prestamoDAO.listarPrestamosActivos();

        if (prestamosActivos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay préstamos activos en este momento para exportar.", "Reporte Vacío", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 2. Usar JFileChooser para que el usuario elija dónde guardar el archivo de forma segura
        JFileChooser selectorArchivos = new JFileChooser();
        selectorArchivos.setDialogTitle("Seleccione dónde guardar el Reporte PDF");
        // Sugerimos un nombre por defecto
        selectorArchivos.setSelectedFile(new File("Reporte_Prestamos_Activos.pdf"));

        int seleccion = selectorArchivos.showSaveDialog(this);

        // Si el usuario cancela o cierra la ventana, detenemos el proceso
        if (seleccion != JFileChooser.APPROVE_OPTION) {
            return; 
        }

        // Obtenemos el archivo seleccionado por el usuario
        File archivoDestino = selectorArchivos.getSelectedFile();
        String rutaCompleta = archivoDestino.getAbsolutePath();

        // Asegurar que termine en .pdf si el usuario lo borró sin querer
        if (!rutaCompleta.toLowerCase().endsWith(".pdf")) {
            rutaCompleta += ".pdf";
        }

        // 3. Crear el documento PDF utilizando iText
        Document documento = new Document(PageSize.A4, 36, 36, 54, 36); 

        try {
            PdfWriter.getInstance(documento, new FileOutputStream(rutaCompleta));
            documento.open();

            // --- DISEÑO Y ESTILOS DEL PDF ---
            com.itextpdf.text.Font fuenteTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 22, com.itextpdf.text.Font.BOLD, new BaseColor(41, 128, 185)); 
            com.itextpdf.text.Font fuenteSubtitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.ITALIC, BaseColor.GRAY);
            com.itextpdf.text.Font fuenteTexto = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);
            com.itextpdf.text.Font fuenteEncabezadoTabla = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.BOLD, BaseColor.WHITE);

            // Encabezado del Reporte
            Paragraph titulo = new Paragraph("SISTEMA DE GESTIÓN DE BIBLIOTECA", fuenteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);

            Paragraph reporteNombre = new Paragraph("Reporte de Préstamos Activos y Deudas", new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD, BaseColor.DARK_GRAY));
            reporteNombre.setAlignment(Element.ALIGN_CENTER);
            reporteNombre.setSpacingBefore(5);
            documento.add(reporteNombre);

            // Fecha y hora de emisión automática
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            Paragraph fecha = new Paragraph("Generado el: " + dtf.format(LocalDateTime.now()), fuenteSubtitulo);
            fecha.setAlignment(Element.ALIGN_CENTER);
            fecha.setSpacingAfter(20);
            documento.add(fecha);

            // Línea divisoria decorativa
            documento.add(new Paragraph(new Chunk(new com.itextpdf.text.pdf.draw.LineSeparator(1f, 100f, new BaseColor(210, 215, 223), Element.ALIGN_CENTER, -1))));
            documento.add(new Paragraph(" ")); 

            // 4. Estructura de la Tabla en el PDF
            PdfPTable tablaPDF = new PdfPTable(4);
            tablaPDF.setWidthPercentage(100); 
            tablaPDF.setWidths(new float[]{1.5f, 4f, 4f, 2.5f}); 

            String[] encabezados = {"ID Préstamo", "Libro Prestado", "Socio / Deudor", "Fecha de Préstamo"};
            BaseColor colorFondoEncabezado = new BaseColor(44, 62, 80); 

            for (String enc : encabezados) {
                PdfPCell celdaEncabezado = new PdfPCell(new Phrase(enc, fuenteEncabezadoTabla));
                celdaEncabezado.setBackgroundColor(colorFondoEncabezado);
                celdaEncabezado.setHorizontalAlignment(Element.ALIGN_CENTER);
                celdaEncabezado.setPadding(8);
                tablaPDF.addCell(celdaEncabezado);
            }

            // 5. Inyectar las filas dinámicamente desde la consulta SQL
            boolean filaAlterna = false;
            BaseColor colorFilaAlterna = new BaseColor(245, 247, 250); 

            for (Object[] filaData : prestamosActivos) {
                for (int i = 0; i < 4; i++) {
                    String dato = (filaData[i] != null) ? filaData[i].toString() : "";
                    PdfPCell celda = new PdfPCell(new Phrase(dato, fuenteTexto));
                    
                    if (i == 0 || i == 3) {
                        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
                    } else {
                        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                    }
                    
                    celda.setPadding(6);

                    if (filaAlterna) {
                        celda.setBackgroundColor(colorFilaAlterna);
                    }
                    
                    tablaPDF.addCell(celda);
                }
                filaAlterna = !filaAlterna;
            }

            documento.add(tablaPDF);
            documento.close();

            // 6. Confirmación al usuario utilizando el nombre real del archivo final
            JOptionPane.showMessageDialog(this, "¡Reporte PDF generado con éxito!\nGuardado en:\n" + rutaCompleta, "Exportación Exitosa", JOptionPane.INFORMATION_MESSAGE);

        } catch (DocumentException | java.io.FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Error al generar el PDF: " + ex.getMessage(), "Error de Exportación", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            com.formdev.flatlaf.FlatDarkLaf.setup();
        } catch (Exception ex) {
            System.err.println("No se pudo inicializar el estilo moderno. Usando por defecto.");
        }

        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}