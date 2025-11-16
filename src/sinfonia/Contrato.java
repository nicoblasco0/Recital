package sinfonia;

/*
 Representa un "contrato".
 Vincula un ArtistaExterno a una Cancion para un Rol determinado
 y registra el costo en el momento de la contratación.
 */
public class Contrato {

    private ArtistaExterno artista;
    private Cancion cancion;
    private String rolAsignado;
    private double costoPagado;

    public Contrato(ArtistaExterno artista, Cancion cancion, String rolAsignado, double costoPagado) {
        this.artista = artista;
        this.cancion = cancion;
        this.rolAsignado = rolAsignado;
        this.costoPagado = costoPagado;
        
        // Marcamos al artista como contratado para que no pueda ser entrenado
        this.artista.setYaContratado();
    }

    // --- Getters ---
    // Necesitamos estos para los reportes y para
    // verificar el límite de canciones de un artista.

    public ArtistaExterno getArtista() {
        return artista;
    }

    public Cancion getCancion() {
        return cancion;
    }

    public String getRolAsignado() {
        return rolAsignado;
    }

    public double getCostoPagado() {
        return costoPagado;
    }

    @Override
    public String toString() {
        return "Asignacion{" +
                "artista=" + artista.getNombre() +
                ", cancion=" + cancion.getTitulo() +
                ", rol='" + rolAsignado + '\'' +
                ", costo=" + costoPagado +
                '}';
    }
}