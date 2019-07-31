import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

public class RsaTests extends TestCase {
    public void test_rsa_read1() {
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get(this.getClass().getResource("/public.pem").getPath()), StandardCharsets.US_ASCII);

            if (lines.size() < 2)
                throw new IllegalArgumentException("Insufficient input");
            if (!lines.remove(0).startsWith("--"))
                throw new IllegalArgumentException("Expected header");
            if (!lines.remove(lines.size() - 1).startsWith("--"))
                throw new IllegalArgumentException("Expected footer");
            String join = String.join("", lines);
            byte[] raw = Base64.getDecoder().decode(join);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PublicKey pub = factory.generatePublic(new X509EncodedKeySpec(raw));
            System.out.println("uf");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void test_open_rsa_file() {
        get_key();
        return;
    }

    private RSAPublicKey get_key() {
        List<String> lines = new ArrayList<>();
        try {
            InputStream inputStream = getClass().getResource("/public.pem").openStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            while(reader.ready()) {
                String line = reader.readLine();
                lines.add(line);
            }
            if (lines.size() < 2)
                throw new IllegalArgumentException("Insufficient input");
            if (!lines.remove(0).startsWith("--"))
                throw new IllegalArgumentException("Expected header");
            if (!lines.remove(lines.size() - 1).startsWith("--"))
                throw new IllegalArgumentException("Expected footer");
            String join = String.join("", lines);
            byte[] raw = Base64.getDecoder().decode(join);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            RSAPublicKey pub = (RSAPublicKey)factory.generatePublic(new X509EncodedKeySpec(raw));
            return pub;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void test_x() throws Exception{
        String sign = "WIZP2AZT1tp8f9zZifdkN1izAET26rD/OhkntOo5Draw8RAwSW71RYDejWyf8+OW0XI37b6Hv3TvcT+4vsY3kFKIICQnB+R44us304P3kdelgRnRLkesGXpcRmYOmOu1er2InP4wUpFdAN0eVOtwRWZS8eK7HwbdEI6CXPOBPbk90tZSZ+s4luBGESO7ZAQcT82jDHOdBDNnBzUauLEL691wjKtsS+xPtfyebt2mlL0DTAoS+LOoklk9jji0hKv9TEmmQLaizIXLVEMw2fsvaS4bGVTGsG7cqapxEJVOiSLQwWoQdGBlnen9l+F/mNMorYHFmGCIvkkaFkpG37fbnA==";
        byte[] decoded_signature = Base64.getDecoder().decode(sign);
        String file = "eyJsaWNlbnNlX3ZhbGlkX3VudGlsIjogIjIwMTktMDgtMDFUMTI6MzA6MTYuOTYwODY4KzAwOjAwIn0=";
        byte[] ss = Base64.getDecoder().decode(file);

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update("a".getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();
        String x = new String(ss);
        System.out.println(ss);
        System.out.println(x);
//        Provider[] providers = Security.getProviders();
//        for (Provider provider : providers) {
//            System.out.println(provider.getName());
//            Set<Provider.Service> services = provider.getServices();
//            for (Provider.Service service : services) {
//                System.out.println("     - " +  service.getAlgorithm());
//                // find algorithm and retrieve service information
//            }
//        }
        Signature signature1 = Signature.getInstance("SHA512withRSA", "SunRsaSign");
        signature1.initVerify(get_key());
        signature1.update(ss);
        boolean result = signature1.verify(decoded_signature);
        assertTrue(result);
    }
    public static String convertStreamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder(2048); // Define a size if you have an idea of it.
        char[] read = new char[128]; // Your buffer size.
        try (InputStreamReader ir = new InputStreamReader(is, StandardCharsets.US_ASCII)) {
            for (int i; -1 != (i = ir.read(read)); sb.append(read, 0, i));
        }
        return sb.toString();
    }

//    keyBytes = Files.readAllBytes(Paths.get(this.getClass().getResource("/public.pem").getPath()));

}
