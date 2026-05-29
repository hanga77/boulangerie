/**
 * Générateur de clés de licence Gestiboul.
 *
 * Usage :
 *   javac KeyGenerator.java
 *   java KeyGenerator CLIENTID
 *
 * Exemple :
 *   java KeyGenerator BOULNGR01
 *   → GESTIBOUL-BOULNGR01-3A7F2C
 */
public class KeyGenerator {

    // DOIT correspondre à app.license.salt dans application.properties
    private static final String SALT = "gestiboul-secret-2024";
    private static final String PREFIX = "GESTIBOUL-";

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: java KeyGenerator <CLIENT_ID>");
            System.err.println("Exemple: java KeyGenerator BOULANGERIE01");
            System.exit(1);
        }
        String clientId = args[0].toUpperCase();
        String key = generateKey(clientId);
        System.out.println("Clé générée pour [" + clientId + "] :");
        System.out.println("  " + key);
    }

    static String generateKey(String clientId) throws Exception {
        String checksum = computeChecksum(clientId);
        return PREFIX + clientId + "-" + checksum;
    }

    static String computeChecksum(String clientId) throws Exception {
        String input = SALT + clientId.toUpperCase();
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02X", b));
        return sb.substring(0, 6);
    }
}
