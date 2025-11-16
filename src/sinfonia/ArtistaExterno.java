package sinfonia;

import java.util.List;

/*
 Representa a un artista externo (candidato) que puede ser contratado.
 Hereda de Artista y maneja la lógica de costos, entrenamientos y límites.
 */
public class ArtistaExterno extends Artista {

    private double costoBase;
    private int maxCanciones;
    private int rolesEntrenados;
    private boolean yaContratado;

    public ArtistaExterno(String nombre, List<String> rolesHistoricos, List<String> bandasHistoricas,
                           double costoBase, int maxCanciones) {
        super(nombre, rolesHistoricos, bandasHistoricas);
        this.costoBase = costoBase;
        this.maxCanciones = maxCanciones;
        this.rolesEntrenados = 0; // Inicia sin entrenamientos
        this.yaContratado = false; // Inicia como no contratado
    }

    /**
     Calcula el costo de contratación actual, aplicando el aumento
     compuesto del 50% por cada rol entrenado.
     @return El costo de contratación final.
     */
    @Override
    public double getCostoContratacion() {
        // Aplica un 50% de aumento (1.5) por cada rol entrenado, de forma compuesta
        return this.costoBase * Math.pow(1.5, this.rolesEntrenados);
    }

    /**
     Devuelve el costo base original, sin incluir entrenamientos.
     Útil para calcular el descuento del 50%.
     @return El costo base.
     */
    public double getCostoBase() {
        return this.costoBase;
    }

    @Override
    public int getMaxCanciones() {
        return this.maxCanciones;
    }

    public boolean YaContratado() {
        return yaContratado;
    }

    /*
     Marca al artista como contratado.
     Esto impide que sea entrenado.
     */
    public void setYaContratado() {
        this.yaContratado = true;
    }

    /**
     Entrena al artista para un nuevo rol, si no está contratado.
     Incrementa su costo y añade el rol a su historial.
     @param nuevoRol El rol a aprender.
     @return true si el entrenamiento fue exitoso, false si no se pudo (ya estaba contratado).
     */
    public boolean entrenar(String nuevoRol) {
        if (this.yaContratado) {
            System.out.println("Error: No se puede entrenar a " + this.nombre + " porque ya fue contratado.");
            return false;
        }

        if (!this.rolesHistoricos.contains(nuevoRol)) {
            this.rolesHistoricos.add(nuevoRol);
            this.rolesEntrenados++;
            System.out.println(this.nombre + " ha sido entrenado para " + nuevoRol + ". Nuevo costo: " + getCostoContratacion());
            return true;
        } else {
            System.out.println(this.nombre + " ya sabía tocar " + nuevoRol + ".");
            return false;
        }
    }
}