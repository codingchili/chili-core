package Protocols;

import Protocols.Util.HashHelper;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.util.HashMap;

/**
 * @author Robin Duda
 */
public class HashHelperTest {
    private static final int HASH_TIME_LIMIT = 1000;
    private static final String password = "pass";
    private static final String wrong = "wrong";
    private static final String salt = "salt";
    private static final String salt2 = "salt2";


    @Test
    public void generateUniqueSaltTest() {
        HashMap<String, Boolean> salts = new HashMap<>();

        for (int i = 0; i < 1000; i++) {
            String salt = HashHelper.generateSalt();

            Assert.assertFalse(salts.containsKey(salt));
            salts.put(salt, true);
        }
    }

    @Test
    public void checkHashesAreUnique() {
        String hash = HashHelper.hash(password, salt);
        String hash2 = HashHelper.hash(wrong, salt);
        String hash3 = HashHelper.hash(password, salt2);

        Assert.assertNotNull(hash);
        Assert.assertTrue(hash.length() != 0);
        Assert.assertNotEquals(hash, password);
        Assert.assertNotEquals(hash, salt);

        Assert.assertNotEquals(hash, hash2);
        Assert.assertNotEquals(hash, hash3);
        Assert.assertNotEquals(hash2, hash3);
    }

    @Test
    public void verifyPasswordWithSalt() {
        String hash = HashHelper.hash(password, salt);

        Assert.assertEquals(hash, HashHelper.hash(password, salt));
    }

    @Test
    public void failVerifyPasswordWrongSalt() {
        String hash = HashHelper.hash(password, salt);
        String hash2 = HashHelper.hash(password, salt2);

        Assert.assertNotEquals(hash, hash2);
    }

    @Test
    public void failVerifyWrongPassword() {
        String hash1 = HashHelper.hash(password, salt);
        String hash2 = HashHelper.hash(wrong, salt);

        Assert.assertNotEquals(hash1, hash2);
    }

    @Test
    public void checkHandles100HashesPerSecond() {
        long start = getTimeMS();

        for (int i = 0; i < 50; i++) {
            HashHelper.hash(password, salt);
        }

        Assert.assertTrue(getTimeMS() - start < HASH_TIME_LIMIT);
    }

    private long getTimeMS() {
        return Instant.now().toEpochMilli();
    }

    @Test
    public void verifyHashCompareSuccess() {
        String hash = HashHelper.hash(password, salt);
        Assert.assertTrue(HashHelper.compare(hash, hash));
    }

    @Test
    public void verifyHashCompareFailure() {
        String hash1 = HashHelper.hash(password, salt);
        String hash2 = HashHelper.hash(wrong, salt2);

        Assert.assertFalse(HashHelper.compare(hash1, hash2));
    }
}
