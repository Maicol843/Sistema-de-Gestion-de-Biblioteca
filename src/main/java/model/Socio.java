package model;

public class Socio {
    private int idSocio;
    private String nombre;
    private String email;
    private String telefono;

    public Socio(int idSocio, String nombre, String email, String telefono) {
        this.idSocio = idSocio;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
    }

    // Getters
    public int getIdSocio() { return idSocio; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
}