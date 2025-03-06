import java.math.BigInteger;
import java.security.SecureRandom;

//NOT USED ANYMORE.

public class EncryptionStuff {
    //Stays hidden
    static BigInteger p;
    static BigInteger q;
    static BigInteger phi;
    static BigInteger d;

    //A pretty number omg???? 2^16+1
    private static final BigInteger E = BigInteger.valueOf(65537);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    //goes to publicKey
    static BigInteger N;
    static BigInteger e=BigInteger.valueOf(65537);

    //PS there exists a library of PublicKey, but I did not realize and thus created my own, if it works, let it be for some time,
    //and we can edit it as per our needs later.
    public static class PublicKey {
        final BigInteger n;
        final BigInteger e;

        public PublicKey(BigInteger n,BigInteger e) {
            this.n = n;
            this.e = e;
        }
    }

    public static class PrivateKey {
        final BigInteger d;
        final BigInteger n;

        public PrivateKey(BigInteger n, BigInteger d) {
            this.d = d;
            this.n = n;
        }
    }

    public record KeyPair(PublicKey publicKey, PrivateKey privateKey) {
    }

    //nvm I proved that getting a number coPrime to e which is less than e would be coPrime to N
    // Generate a random coprime number
    public static BigInteger findCoprime(BigInteger a, BigInteger b, int bitLength) {
        SecureRandom rand = new SecureRandom();
        BigInteger candidate;

        // Keep generating random candidates until we find one that is coprime with both a and b
        do {
            //huhhhhh so keep on randomly looking for numbers until you get the co primes!?!?!? That is not a cool way ngl
            candidate = new BigInteger(bitLength/2, rand);  // Generates a random BigInteger with the given bit length
        } while (!a.gcd(candidate).equals(BigInteger.ONE) || !b.gcd(candidate).equals(BigInteger.ONE));

        return candidate;
    }
    private static BigInteger boundedRandomNo(BigInteger bound) {
        BigInteger upperBound = new BigInteger(String.valueOf(bound)); // Example upper bound
        SecureRandom random = new SecureRandom();

        // Generate a random BigInteger in the range [0, upperBound)
        BigInteger randomBigInt;
        do {
            randomBigInt = new BigInteger(upperBound.bitLength(), random);
        } while (randomBigInt.compareTo(upperBound) >= 0);
        return randomBigInt;
    }

    //Finders for e and d
    //very simple but inefficient when e is random!!!
    //!!!###NEVER USE THIS###!!!!!
    private static void findE1(BigInteger N, BigInteger phi) {
        //Why do I feel like this would cause memory issues?
        //Oh helll nawwww never use this
        do{
            e = boundedRandomNo(phi);
        }while(!e.gcd(phi).equals(BigInteger.ONE) || !N.gcd(e).equals(BigInteger.ONE));
    }

    private static void findE(BigInteger N, BigInteger phi) {
        // Use a known, commonly used value for e
        e = new BigInteger("65537"); // A widely used public exponent
        if (!e.gcd(phi).equals(BigInteger.ONE)) {
            // This should not happen in most cases
            throw new IllegalArgumentException("e is not coprime with phi");
        }
    }
    private static void findD(BigInteger e, BigInteger phi){
        do{
            d = boundedRandomNo(phi);
        }while (!d.multiply(e).mod(phi).equals(BigInteger.ONE));
    }



    //Setters
    private static void setP(int bitLength, SecureRandom secureRandom){
        p = BigInteger.probablePrime(bitLength/2, secureRandom);

    }
    private static void setQ(int bitLength, SecureRandom secureRandom){
        q = BigInteger.probablePrime(bitLength/2, secureRandom);
    }
    private static void setPhi(BigInteger p, BigInteger q){
        phi= p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
    }
    private static void setN(BigInteger p, BigInteger q){
        N= p.multiply(q);
    }




    //Getters
    private static BigInteger getP() {
        return p;
    }
    private static BigInteger getQ() {
        return q;
    }
    private static BigInteger getPhi() {
        return phi;
    }
    private static BigInteger getN() {
        return N;
    }
    private static BigInteger getE() {
        return e;
    }



    //This generates my public key and private key

    /*
public static KeyPair generateRSAKeys(int bitLength) {
    SecureRandom secureRandom = new SecureRandom();

    // Generate p and q in parallel for better performance
    BigInteger p, q;
    do {
        p = generateStrongPrime(bitLength / 2, secureRandom);
        q = generateStrongPrime(bitLength / 2, secureRandom);
    } while (p.equals(q)); // Ensure p and q are different

    BigInteger n = p.multiply(q);
    BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

    // Calculate private exponent d using modular multiplicative inverse
    BigInteger d = E.modInverse(phi);

    return new KeyPair(new PublicKey(n), new PrivateKey(n, d));

    }

     */


    public static KeyPair generateRSAKeys(int bitLength) {
        // Generate p and q directly, without strong prime checks
        BigInteger p = BigInteger.probablePrime(bitLength / 2, SECURE_RANDOM);
        BigInteger q;

        // Ensure p and q are different
        do {
            q = BigInteger.probablePrime(bitLength / 2, SECURE_RANDOM);
        } while (p.equals(q));

        BigInteger n = p.multiply(q);

        // Calculate phi = (p-1)(q-1)
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

        // Calculate d as modular multiplicative inverse of e mod phi
        BigInteger d = E.modInverse(phi);

        return new KeyPair(new PublicKey(n,e), new PrivateKey(n, d));
    }


/*
public static publicKeyPair generateRSAKeys(int bitLength) {
        SecureRandom secureRandom = new SecureRandom();
        //Creating my random parts of the keys
        setP(bitLength,secureRandom);
        setQ(bitLength,secureRandom);
        setN(getP(),getQ());
        setPhi(getP(),getQ());

        //finding suitable e and d
        findE(getN(),getPhi());
        findD(getE(),getPhi());
        return new publicKeyPair(new publicKey(getN()),new privateKey(getN(),d));

    }
    */


    // Generate strong primes for better security
    private static BigInteger generateStrongPrime(int bitLength, SecureRandom random) {
        BigInteger prime;
        do {
            prime = BigInteger.probablePrime(bitLength, random);
        } while (!isStrongPrime(prime));
        return prime;
    }

    // Check if prime is strong (p-1 and p+1 have large prime factors)
    private static boolean isStrongPrime(BigInteger p) {
        BigInteger pMinus1 = p.subtract(BigInteger.ONE);
        BigInteger pPlus1 = p.add(BigInteger.ONE);

        // Check if p-1 and p+1 have at least one large prime factor
        return hasLargePrimeFactor(pMinus1) && hasLargePrimeFactor(pPlus1);
    }
    private static boolean hasLargePrimeFactor(BigInteger n) {
        // Simple check for demonstration.
        return n.divide(BigInteger.valueOf(2)).isProbablePrime(50);
    }


    public static BigInteger encryptRSA(BigInteger message, PublicKey publicKey) {
        return message.modPow(publicKey.e, publicKey.n);
    }

    public static BigInteger decryptRSA(BigInteger encryptedMessage, PrivateKey privateKey) {
        return encryptedMessage.modPow(privateKey.d, privateKey.n);
    }








    public static String encryptAES(String message, byte[] aesKey) {
        byte[] bytes = message.getBytes();
        byte[] encryptedBytes = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            encryptedBytes[i] = (byte) (aesKey[i% aesKey.length] ^ bytes[i]);
        }
        return new BigInteger(1, encryptedBytes).toString(16);
    }
    public static String decryptAES(String encryptedMessage, byte[] aesKey) {
        byte[] bytes = encryptedMessage.getBytes();
        byte[] decryptedBytes = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            decryptedBytes[i] = (byte) (aesKey[i% aesKey.length] ^ bytes[i]);
        }
        return new String(decryptedBytes);
    }
    public static byte[] generateAESKey() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[16];
        secureRandom.nextBytes(key);
        return key;
    }


}
