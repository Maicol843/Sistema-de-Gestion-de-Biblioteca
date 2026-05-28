package model;

public class Libro {
    private int idLibro;
    private String titulo;
    private String autor;
    private String isbn;
    private int disponible;

    public Libro(int idLibro, String titulo, String autor, String isbn, int disponible) {
        this.idLibro = idLibro;
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.disponible = disponible;
    }

    // Getters y Setters
    public int getIdLibro() { return idLibro; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public String getIsbn() { return isbn; }
    public int getDisponible() { return disponible; }
    
    public void setDisponible(int disponible) { this.disponible = disponible; }
}