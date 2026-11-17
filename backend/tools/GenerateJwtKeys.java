import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Set;

public class GenerateJwtKeys {
    public static void main(String[] args) throws Exception {
        Path directory = Path.of(".secrets");
        Path privateKey = directory.resolve("jwt-private.pem");
        Path publicKey = directory.resolve("jwt-public.pem");
        boolean hasPrivateKey = Files.exists(privateKey, LinkOption.NOFOLLOW_LINKS);
        boolean hasPublicKey = Files.exists(publicKey, LinkOption.NOFOLLOW_LINKS);

        if (hasPrivateKey && hasPublicKey) {
            System.out.println("Both JWT key files already exist. Kept them as-is.");
            return;
        }
        if (hasPrivateKey || hasPublicKey) {
            System.err.println("Only one JWT key file exists. Restore its matching file before retrying.");
            System.exit(1);
        }

        Files.createDirectories(directory);
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(3072);
        var pair = generator.generateKeyPair();
        writePrivateKey(privateKey, pem("PRIVATE KEY", pair.getPrivate().getEncoded()));
        Files.writeString(publicKey, pem("PUBLIC KEY", pair.getPublic().getEncoded()),
                StandardCharsets.US_ASCII, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        System.out.println("Created RSA-3072 JWT keys in " + directory.toAbsolutePath().normalize());
    }

    private static String pem(String label, byte[] der) {
        var encoder = Base64.getMimeEncoder(64, new byte[]{'\n'});
        return "-----BEGIN " + label + "-----\n"
                + encoder.encodeToString(der) + "\n-----END " + label + "-----\n";
    }

    private static void writePrivateKey(Path path, String pem) throws IOException {
        var options = Set.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        if (!Files.getFileStore(path.getParent()).supportsFileAttributeView("posix")) {
            Files.writeString(path, pem, StandardCharsets.US_ASCII, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            return;
        }
        var ownerOnly = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"));
        try (var channel = Files.newByteChannel(path, options, ownerOnly)) {
            var contents = StandardCharsets.US_ASCII.encode(pem);
            while (contents.hasRemaining()) {
                channel.write(contents);
            }
        }
    }
}
