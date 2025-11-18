package sinfonia;

import org.jpl7.Atom;
import org.jpl7.Compound;
import org.jpl7.Query;
import org.jpl7.Term;
import org.jpl7.Variable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.net.URL;
import java.nio.file.Paths;

public class IntegracionProlog {

    public IntegracionProlog() {
        try {
        	
            // 1) Buscar entrenamientos.pl en el classpath
            URL resource = IntegracionProlog.class
                    .getClassLoader()
                    .getResource("entrenamientos.pl");

            System.out.println("DEBUG recurso entrenamientos.pl = " + resource);

            if (resource == null) {
                throw new IllegalStateException("No se encontró entrenamientos.pl en el classpath");
            }


            // 2) Convertir a ruta de archivo y normalizar slashes
            String path = Paths.get(resource.toURI())
                    .toString()
                    .replace("\\", "/");

            System.out.println("DEBUG ruta Prolog entrenamientos.pl = " + path);

            // 3) Hacer consult(Ruta) en Prolog
            Query q = new Query(
                    "consult",
                    new Term[]{ new Atom(path) }
            );

            boolean ok = q.hasSolution();
            System.out.println("DEBUG resultado consult('" + path + "') = " + ok);

            if (!ok) {
                throw new IllegalStateException("No se pudo consultar entrenamientos.pl en: " + path);
            }

        } catch (Exception e) {
        	System.out.println(System.getProperty("java.class.path"));

        	e.printStackTrace();
            throw new RuntimeException("Error inicializando Prolog", e);
        }
    }

    private String normalizarRol(String rol) {
        if (rol == null) return "";
        return rol.toLowerCase()
                .replace(" ", "_")
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u");
    }

    private void limpiarHechosPrevios() {
        // Borramos los hechos dinámicos usados por este servicio
        new Query("retractall(requiere(_, _))").hasSolution();
        new Query("retractall(tiene_base(_, _))").hasSolution();
    }

    public int entrenamientosMinimos(List<Cancion> canciones, List<ArtistaBase> artistasBase) {

        // 1) Limpiar hechos viejos en Prolog
        limpiarHechosPrevios();

        // 2) Construir requiere(Rol, CantMaxima)
        //    CantMaxima = máxima cantidad simultánea de ese rol en alguna canción
        Map<String, Integer> maxRequeridosPorRol = new HashMap<>();

        for (Cancion c : canciones) {
            // Asumiendo Map<String, Integer> getRolesRequeridos()
            for (String r : c.getRolesRequeridos()) {
                String rolNorm = normalizarRol(r);
                maxRequeridosPorRol.merge(rolNorm, 1, Integer::sum);
            }
        }

        // 3) Construir tiene_base(Rol, CantBase)
        //    CantBase = cuántos artistas base saben ese rol
        Map<String, Integer> basePorRol = new HashMap<>();

        for (ArtistaBase a : artistasBase) {
            for (String rol : a.getRolesHistoricos()) {
                String rolNorm = normalizarRol(rol);
                basePorRol.merge(rolNorm, 1, Integer::sum);
            }
        }

        // 4) Assertar requiere/2 en Prolog
        for (Map.Entry<String, Integer> e : maxRequeridosPorRol.entrySet()) {
            String rol = e.getKey();
            int cantMax = e.getValue();

            Term rolAtom = new Atom(rol);
            Term cantTerm = new org.jpl7.Integer(cantMax);
            Term fact = new Compound("requiere", new Term[]{rolAtom, cantTerm});

            Query assertQ = new Query("assertz", new Term[]{fact});
            assertQ.hasSolution();
        }

        // 5) Assertar tiene_base/2 en Prolog
        for (Map.Entry<String, Integer> e : basePorRol.entrySet()) {
            String rol = e.getKey();
            int cantBase = e.getValue();

            Term rolAtom = new Atom(rol);
            Term cantTerm = new org.jpl7.Integer(cantBase);
            Term fact = new Compound("tiene_base", new Term[]{rolAtom, cantTerm});

            Query assertQ = new Query("assertz", new Term[]{fact});
            assertQ.hasSolution();
        }

        // 6) Consultar min_entrenamientos(N).
        Variable N = new Variable("N");
        Query q = new Query(
                "min_entrenamientos",
                new Term[]{N}
        );

        Map<String, Term> solution = q.oneSolution();
        if (solution == null) {
            throw new RuntimeException("Sin solución para min_entrenamientos(N).");
        }

        Term nTerm = solution.get("N");
        if (!(nTerm instanceof org.jpl7.Integer)) {
            throw new RuntimeException("N no es un entero Prolog: " + nTerm);
        }
        return ((org.jpl7.Integer) nTerm).intValue();
    }

    public double costoTotalEntrenamientos(List<Cancion> canciones,
                                           List<ArtistaBase> artistasBase,
                                           double costoUnitario) {
        int n = entrenamientosMinimos(canciones, artistasBase);
        return n * costoUnitario;
    }
}
