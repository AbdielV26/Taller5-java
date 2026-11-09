import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.*;

public class Autograde {
    static final String WORKSPACE = getEnv("GITHUB_WORKSPACE", ".");
    static final String BRANCH = getEnv("GITHUB_HEAD_REF", getEnv("GITHUB_REF_NAME", "unknown"));
    static final double TOL = 0.01;
    static final int NUM_SEEDS = 15;

    static int notaEj1 = 0, notaEj2 = 0, notaEj3 = 0, notaEj4 = 0, notaEj5 = 0;
    static int notaGit = 0, notaEvid = 0;
    static boolean estructurasProhibidas = false;
    static boolean autogradeModificado = false;
    static boolean workflowModificado = false;
    static boolean hardcode = false;
    static int numCommits = 0;
    static long minEntreCommits = Long.MAX_VALUE;
    static int numAutores = 0;
    static final List<String> banderas = new ArrayList<>();
    static final List<String> errores = new ArrayList<>();

    static String getEnv(String k, String d) {
        String v = System.getenv(k);
        return v == null || v.isEmpty() ? d : v;
    }

    public static void main(String[] args) {
        try {
            System.out.println("=== Autograde Taller 5 ===");
            System.out.println("Rama: " + BRANCH);

            String cedula = leerCedula();
            System.out.println("Semilla (últimos 3 dígitos de cédula): " + cedula);

            checkEstructurasProhibidas();
            checkCommits();
            checkAutogradeNoModificado();

            probarEjercicio1(cedula);
            probarEjercicio2(cedula);
            probarEjercicio3(cedula);
            probarEjercicio4(cedula);
            probarEjercicio5(cedula);

            checkEvidencias();
            calcularNotaGit();

            int notaFinal = notaEj1 + notaEj2 + notaEj3 + notaEj4 + notaEj5 + notaGit + notaEvid;
            if (estructurasProhibidas) notaFinal = Math.max(0, notaFinal - 20);

            String obs = calcularObservacion();
            writeResultado(notaFinal, obs, cedula);
            printSummary(notaFinal, obs);
        } catch (Exception e) {
            System.err.println("ERROR FATAL en autograde: " + e.getMessage());
            e.printStackTrace();
            try { writeResultado(0, "ALTA", "error"); } catch (Exception ignored) {}
            System.exit(1);
        }
    }

    // ---------- Lectura de cédula / semilla ----------
    static String leerCedula() {
        try {
            Path p = Paths.get(WORKSPACE, "informe.md");
            if (!Files.exists(p)) return "000";
            String content = Files.readString(p);
            Matcher m = Pattern.compile("(?i)c[eé]dula\\s*[:\\-]?\\s*([0-9\\-]+)").matcher(content);
            if (m.find()) {
                String c = m.group(1).replaceAll("[^0-9]", "");
                if (c.length() >= 3) return c.substring(c.length() - 3);
            }
            m = Pattern.compile("\\b(\\d{3,})\\b").matcher(content);
            if (m.find()) {
                String c = m.group(1);
                return c.substring(c.length() - 3);
            }
            return "000";
        } catch (IOException e) { return "000"; }
    }

    // ---------- Estructuras prohibidas ----------
    static void checkEstructurasProhibidas() {
        String[] prohibidas = {
            "Scanner", "ArrayList", "HashMap", "HashSet", "LinkedList",
            "extends ", "implements ", "interface ", "abstract ",
            "FileReader", "FileWriter", "BufferedWriter", "System.console",
            "Optional<", "Stream<", "Streams.", "try ("
        };
        try {
            Path src = Paths.get(WORKSPACE, "src");
            if (!Files.exists(src)) return;
            Files.walk(src)
                .filter(p -> p.toString().endsWith(".java"))
                .forEach(p -> {
                    try {
                        String code = Files.readString(p);
                        String limpio = code
                            .replaceAll("//[^\\n]*", "")
                            .replaceAll("(?s)/\\*.*?\\*/", "")
                            .replaceAll("\"(?:\\\\.|[^\"\\\\])*\"", "\"\"");
                        for (String pr : prohibidas) {
                            if (limpio.contains(pr)) {
                                estructurasProhibidas = true;
                                banderas.add("estructura_prohibida:" + pr.trim() + " en " + p.getFileName());
                            }
                        }
                    } catch (IOException ignored) {}
                });
        } catch (IOException e) {
            System.err.println("Error revisando estructuras: " + e.getMessage());
        }
    }

    // ---------- Commits ----------
    static void checkCommits() {
        try {
            String baseRef = getEnv("GITHUB_BASE_REF", "main");
            // Traer la base por si no está
            new ProcessBuilder("git", "-C", WORKSPACE, "fetch", "origin", baseRef, "--depth=200")
                .redirectErrorStream(true).start().waitFor();

            Process p = new ProcessBuilder("git", "-C", WORKSPACE, "log", "--format=%aI|%an|%H",
                                           "origin/" + baseRef + "..HEAD")
                .redirectErrorStream(true).start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            List<Long> fechas = new ArrayList<>();
            Set<String> autores = new HashSet<>();
            String line;
            while ((line = r.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    autores.add(parts[1]);
                    try {
                        OffsetDateTime odt = OffsetDateTime.parse(parts[0]);
                        fechas.add(odt.toEpochSecond());
                    } catch (Exception ignored) {}
                }
            }
            p.waitFor();

            numCommits = fechas.size();
            numAutores = autores.size();
            if (fechas.size() >= 2) {
                long min = Collections.min(fechas);
                long max = Collections.max(fechas);
                minEntreCommits = (max - min) / 60;
                if (minEntreCommits < 25) banderas.add("commits_muy_juntos:" + minEntreCommits + "min");
            }
            if (numAutores > 1) banderas.add("multiples_autores:" + numAutores);
            if (numCommits < 4) banderas.add("commits_insuficientes:" + numCommits);
        } catch (Exception e) {
            banderas.add("error_leyendo_commits:" + e.getClass().getSimpleName());
        }
    }

    // ---------- Autograde / workflow oficiales ----------
    static void checkAutogradeNoModificado() {
        try {
            Path localAutograde = Paths.get(WORKSPACE, "autograde", "Autograde.java");
            Path oficialAutograde = Paths.get(WORKSPACE, "autograde", "Autograde_oficial.java");
            if (Files.exists(localAutograde) && Files.exists(oficialAutograde)) {
                if (!sha256(localAutograde).equals(sha256(oficialAutograde))) {
                    autogradeModificado = true;
                    banderas.add("autograde_modificado");
                }
            }
            Path localWorkflow = Paths.get(WORKSPACE, ".github", "workflows", "autograde.yml");
            Path oficialWorkflow = Paths.get(WORKSPACE, ".github", "workflows", "autograde_oficial.yml");
            if (Files.exists(localWorkflow) && Files.exists(oficialWorkflow)) {
                if (!sha256(localWorkflow).equals(sha256(oficialWorkflow))) {
                    workflowModificado = true;
                    banderas.add("workflow_modificado");
                }
            }
        } catch (Exception e) {
            banderas.add("error_verificando_integridad");
        }
    }

    static String sha256(Path p) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(Files.readAllBytes(p));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    // ---------- Ejercicios ----------
    static void probarEjercicio1(String cedula) {
        try {
            Class<?> c = Class.forName("taller5.Ejercicio1");
            Method prop = c.getMethod("calcularPropina", double.class, double.class);
            Method tot = c.getMethod("calcularTotal", double.class, double.class);
            int ok = 0;
            for (int s = 0; s < NUM_SEEDS; s++) {
                double monto = 50.0 + ((s * 17 + Integer.parseInt(cedula)) % 150);
                double pct = 5.0 + ((s * 3) % 20);
                double espP = monto * pct / 100.0;
                double espT = monto + espP;
                double obtP = (Double) prop.invoke(null, monto, pct);
                double obtT = (Double) tot.invoke(null, monto, pct);
                if (Math.abs(obtP - espP) <= TOL && Math.abs(obtT - espT) <= TOL) ok++;
            }
            notaEj1 = (int) Math.round(15.0 * ok / NUM_SEEDS);
            if (ok < NUM_SEEDS / 2) { hardcode = true; banderas.add("ej1_posible_hardcode"); }
        } catch (Exception e) {
            errores.add("Ejercicio1: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    static void probarEjercicio2(String cedula) {
        try {
            Class<?> c = Class.forName("taller5.Ejercicio2");
            Method prom = c.getMethod("calcularPromedio", double.class, double.class, double.class);
            Method est = c.getMethod("obtenerEstado", double.class);
            int ok = 0;
            for (int s = 0; s < NUM_SEEDS; s++) {
                double n1 = 50 + ((s * 7 + Integer.parseInt(cedula)) % 50);
                double n2 = 40 + ((s * 11) % 60);
                double n3 = 30 + ((s * 13) % 70);
                double esp = (n1 + n2 + n3) / 3.0;
                String espE = esp >= 70 ? "APROBADO" : "REPROBADO";
                double obt = (Double) prom.invoke(null, n1, n2, n3);
                String obtE = (String) est.invoke(null, obt);
                if (Math.abs(obt - esp) <= TOL && espE.equals(obtE)) ok++;
            }
            notaEj2 = (int) Math.round(15.0 * ok / NUM_SEEDS);
            if (ok < NUM_SEEDS / 2) { hardcode = true; banderas.add("ej2_posible_hardcode"); }
        } catch (Exception e) {
            errores.add("Ejercicio2: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    static void probarEjercicio3(String cedula) {
        try {
            Class<?> c = Class.forName("taller5.Ejercicio3");
            Method area = c.getMethod("calcularArea", double.class, double.class);
            Method per = c.getMethod("calcularPerimetro", double.class, double.class);
            int ok = 0;
            for (int s = 0; s < NUM_SEEDS; s++) {
                double b = 2 + ((s * 3 + Integer.parseInt(cedula)) % 20);
                double h = 1 + ((s * 5) % 15);
                double espA = b * h;
                double espP = 2 * (b + h);
                double oA = (Double) area.invoke(null, b, h);
                double oP = (Double) per.invoke(null, b, h);
                if (Math.abs(oA - espA) <= TOL && Math.abs(oP - espP) <= TOL) ok++;
            }
            notaEj3 = (int) Math.round(15.0 * ok / NUM_SEEDS);
            if (ok < NUM_SEEDS / 2) { hardcode = true; banderas.add("ej3_posible_hardcode"); }
        } catch (Exception e) {
            errores.add("Ejercicio3: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    static void probarEjercicio4(String cedula) {
        try {
            Class<?> c = Class.forName("taller5.Ejercicio4");
            Method conv = c.getMethod("convertirFahrenheit", double.class);
            int ok = 0;
            for (int s = 0; s < NUM_SEEDS; s++) {
                double celsius = -10 + ((s * 5 + Integer.parseInt(cedula)) % 100);
                double esp = celsius * 9.0 / 5.0 + 32;
                double obt = (Double) conv.invoke(null, celsius);
                if (Math.abs(obt - esp) <= TOL) ok++;
            }
            notaEj4 = (int) Math.round(15.0 * ok / NUM_SEEDS);
            if (ok < NUM_SEEDS / 2) { hardcode = true; banderas.add("ej4_posible_hardcode"); }
        } catch (Exception e) {
            errores.add("Ejercicio4: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    static void probarEjercicio5(String cedula) {
        try {
            Class<?> c = Class.forName("taller5.Estudiante");
            Constructor<?> ctor = c.getConstructor(String.class, int.class, double.class, double.class, double.class);
            Method getProm = c.getMethod("getPromedio");
            int ok = 0;
            for (int s = 0; s < NUM_SEEDS; s++) {
                double n1 = 50 + ((s * 7 + Integer.parseInt(cedula)) % 50);
                double n2 = 40 + ((s * 11) % 60);
                double n3 = 30 + ((s * 13) % 70);
                double esp = (n1 + n2 + n3) / 3.0;
                Object est = ctor.newInstance("Test" + s, 18 + (s % 10), n1, n2, n3);
                double obt = (Double) getProm.invoke(est);
                if (Math.abs(obt - esp) <= TOL) ok++;
            }
            notaEj5 = (int) Math.round(20.0 * ok / NUM_SEEDS);
            if (ok < NUM_SEEDS / 2) { hardcode = true; banderas.add("ej5_posible_hardcode"); }
        } catch (Exception e) {
            errores.add("Ejercicio5: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    // ---------- Evidencias ----------
    static void checkEvidencias() {
        try {
            Path dir = Paths.get(WORKSPACE, "evidencias");
            if (!Files.exists(dir)) { errores.add("Carpeta evidencias no encontrada"); return; }
            final int[] validas = {0};
            Files.list(dir).forEach(p -> {
                String n = p.getFileName().toString().toLowerCase();
                if (n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg")) {
                    try {
                        java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(p.toFile());
                        if (img != null && img.getWidth() >= 800 && img.getHeight() >= 600) validas[0]++;
                    } catch (Exception ignored) {}
                }
            });
            notaEvid = (int) Math.round(10.0 * Math.min(validas[0], 6) / 6.0);
            if (validas[0] < 6) errores.add("Evidencias válidas (>=800x600): " + validas[0] + " de 6");
        } catch (IOException e) {
            errores.add("Error revisando evidencias: " + e.getMessage());
        }
    }

    // ---------- Nota Git ----------
    static void calcularNotaGit() {
        int pts = 10;
        if (numCommits < 4) pts -= 3;
        if (minEntreCommits < 25 && numCommits >= 2) pts -= 3;
        if (numAutores > 1) pts -= 2;
        if (workflowModificado || autogradeModificado) pts -= 5;
        notaGit = Math.max(0, pts);
    }

    // ---------- Observación de plagio ----------
    static String calcularObservacion() {
        int score = 0;
        if (estructurasProhibidas) score += 3;
        if (hardcode) score += 3;
        if (numCommits < 4) score += 1;
        if (minEntreCommits < 25 && numCommits >= 2) score += 1;
        if (numAutores > 1) score += 2;
        if (autogradeModificado || workflowModificado) score += 3;
        if (score == 0) return "NINGUNA";
        if (score <= 1) return "BAJA";
        if (score <= 3) return "MEDIA";
        return "ALTA";
    }

    // ---------- Salida JSON + resumen ----------
    static void writeResultado(int nota, String obs, String cedula) {
        try {
            StringBuilder sb = new StringBuilder("{\n");
            sb.append("  \"nota\": ").append(nota).append(",\n");
            sb.append("  \"observacion_plagio\": \"").append(obs).append("\",\n");
            sb.append("  \"cedula_semilla\": \"").append(cedula).append("\",\n");
            sb.append("  \"rama\": \"").append(BRANCH).append("\",\n");
            sb.append("  \"banderas\": [");
            for (int i = 0; i < banderas.size(); i++) {
                sb.append("\"").append(banderas.get(i).replace("\"", "'")).append("\"");
                if (i < banderas.size() - 1) sb.append(", ");
            }
            sb.append("],\n");
            sb.append("  \"detalle_ejercicios\": {\n");
            sb.append("    \"ej1\": ").append(notaEj1).append(",\n");
            sb.append("    \"ej2\": ").append(notaEj2).append(",\n");
            sb.append("    \"ej3\": ").append(notaEj3).append(",\n");
            sb.append("    \"ej4\": ").append(notaEj4).append(",\n");
            sb.append("    \"ej5\": ").append(notaEj5).append(",\n");
            sb.append("    \"git\": ").append(notaGit).append(",\n");
            sb.append("    \"evidencias\": ").append(notaEvid).append("\n");
            sb.append("  },\n");
            sb.append("  \"semillas_probadas\": ").append(NUM_SEEDS).append(",\n");
            sb.append("  \"errores\": [");
            for (int i = 0; i < errores.size(); i++) {
                sb.append("\"").append(errores.get(i).replace("\"", "'").replace("\n", " ")).append("\"");
                if (i < errores.size() - 1) sb.append(", ");
            }
            sb.append("]\n}\n");
            Files.writeString(Paths.get(WORKSPACE, "resultado_autograde.json"), sb.toString());
        } catch (IOException e) {
            System.err.println("No se pudo escribir JSON: " + e.getMessage());
        }
    }

    static void printSummary(int nota, String obs) {
        System.out.println("\n========== RESUMEN AUTOGRADE ==========");
        System.out.println("Nota: " + nota + "/100");
        System.out.println("Observación de plagio: " + obs);
        System.out.println("Ej1: " + notaEj1 + "/15  Ej2: " + notaEj2 + "/15  Ej3: " + notaEj3 + "/15");
        System.out.println("Ej4: " + notaEj4 + "/15  Ej5: " + notaEj5 + "/20  Git: " + notaGit + "/10  Evid: " + notaEvid + "/10");
        System.out.println("Commits: " + numCommits + " | tiempo entre: " + minEntreCommits + "min | autores: " + numAutores);
        if (!banderas.isEmpty()) System.out.println("Banderas: " + banderas);
        if (!errores.isEmpty()) System.out.println("Errores: " + errores);
        System.out.println("=======================================\n");
    }
}
