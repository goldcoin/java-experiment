package org.mightyfish.jcajce.provider.digest;

import org.mightyfish.asn1.pkcs.PKCSObjectIdentifiers;
import org.mightyfish.crypto.CipherKeyGenerator;
import org.mightyfish.crypto.digests.MD2Digest;
import org.mightyfish.crypto.macs.HMac;
import org.mightyfish.jcajce.provider.config.ConfigurableProvider;
import org.mightyfish.jcajce.provider.symmetric.util.BaseKeyGenerator;
import org.mightyfish.jcajce.provider.symmetric.util.BaseMac;

public class MD2
{
    private MD2()
    {

    }

    static public class Digest
        extends BCMessageDigest
        implements Cloneable
    {
        public Digest()
        {
            super(new MD2Digest());
        }

        public Object clone()
            throws CloneNotSupportedException
        {
            Digest d = (Digest)super.clone();
            d.digest = new MD2Digest((MD2Digest)digest);

            return d;
        }
    }

    /**
     * MD2 HMac
     */
    public static class HashMac
        extends BaseMac
    {
        public HashMac()
        {
            super(new HMac(new MD2Digest()));
        }
    }

    public static class KeyGenerator
        extends BaseKeyGenerator
    {
        public KeyGenerator()
        {
            super("HMACMD2", 128, new CipherKeyGenerator());
        }
    }

    public static class Mappings
        extends DigestAlgorithmProvider
    {
        private static final String PREFIX = MD2.class.getName();

        public Mappings()
        {
        }

        public void configure(ConfigurableProvider provider)
        {
            provider.addAlgorithm("MessageDigest.MD2", PREFIX + "$Digest");
            provider.addAlgorithm("Alg.Alias.MessageDigest." + PKCSObjectIdentifiers.md2, "MD2");

            addHMACAlgorithm(provider, "MD2", PREFIX + "$HashMac", PREFIX + "$KeyGenerator");
        }
    }
}
