/**
 * Générateur de clés de licence Gestiboul.
 *
 * Workflow :
 *   1. Le client ouvre /admin/licence et copie son "ID Poste" (ex: A3F7B2C1)
 *   2. Il vous envoie cet identifiant (WhatsApp, email…)
 *   3. Vous générez sa clé avec cet outil :
 *
 *      javac KeyGenerator.java
 *      java KeyGenerator A3F7B2C1
 *      → GESTIBOUL-A3F7B2C1-3A7F2C
 *
 *   4. Vous lui communiquez la clé
 *   5. Il la saisit dans /admin/licence → activé uniquement sur son poste
 */
public class KeyGenerator {

    // DOIT correspondre à app.license.salt dans application.properties
    private static final String SALT = "gestiboul-secret-2024";
    private static final String PREFIX = "GESTIBOUL-";

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: java KeyGenerator <ID_POSTE>");
            System.err.println("Exemple: java KeyGenerator A3F7B2C1");
            System.err.println();
            System.err.println("L'ID Poste est visible sur la page /admin/licence du client.");
            System.exit(1);
        }
        String machineId = args[0].toUpperCase();
        String key = generateKey(machineId);
        System.out.println("Clé pour le poste [" + machineId + "] :");
        System.out.println("  " + key);
    }

    static String generateKey(String machineId) throws Exception {
        return PREFIX + machineId + "-" + computeChecksum(machineId);
    }

    static String computeChecksum(String machineId) throws Exception {
        String input = SALT + machineId.toUpperCase();
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02X", b));
        return sb.substring(0, 6);
    }
}
