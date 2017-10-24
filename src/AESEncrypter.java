import java.security.spec.KeySpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

// implementation borrowed from https://pastebin.com/YiwbCAW8
public class AESEncrypter {

    private static final byte[] SALT = {
            (byte) 0xAA, (byte) 0x9C, (byte) 0xA3, (byte) 0x67,
            (byte) 0x67, (byte) 0x7A, (byte) 0xE9, (byte) 0x12
    };
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 128;
    private Cipher ecipher;
    private Cipher dcipher;
    private byte[] initV;

    AESEncrypter(String passPhrase, byte[] newIV) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(passPhrase.toCharArray(), SALT, ITERATION_COUNT, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

        ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        ecipher.init(Cipher.ENCRYPT_MODE, secret);

        dcipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        if (newIV.length != 16) {
            initV = ecipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
        } else {
            initV = newIV;
        }
        dcipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(initV));
    }

    public String encrypt(String encrypt) throws Exception {
        byte[] bytes = encrypt.getBytes("UTF8");
        byte[] encrypted = encrypt(bytes);
        return new BASE64Encoder().encode(encrypted);
    }

    public byte[] encrypt(byte[] plain) throws Exception {
        return ecipher.doFinal(plain);
    }

    public String decrypt(String encrypt) throws Exception {
        byte[] bytes = new BASE64Decoder().decodeBuffer(encrypt);
        byte[] decrypted = decrypt(bytes);
        return new String(decrypted, "UTF8");
    }

    public byte[] decrypt(byte[] encrypt) throws Exception {
        return dcipher.doFinal(encrypt);
    }

    public byte[] getInitVect() throws Exception {
        return initV;
    }
}