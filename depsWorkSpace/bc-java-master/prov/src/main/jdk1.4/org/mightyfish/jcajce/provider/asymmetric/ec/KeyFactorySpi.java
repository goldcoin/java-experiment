package org.mightyfish.jcajce.provider.asymmetric.ec;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import org.mightyfish.asn1.ASN1ObjectIdentifier;
import org.mightyfish.asn1.pkcs.PrivateKeyInfo;
import org.mightyfish.asn1.x509.SubjectPublicKeyInfo;
import org.mightyfish.asn1.x9.X9ObjectIdentifiers;
import org.mightyfish.jcajce.provider.asymmetric.util.BaseKeyFactorySpi;
import org.mightyfish.jcajce.provider.config.ProviderConfiguration;
import org.mightyfish.jcajce.provider.util.AsymmetricKeyInfoConverter;
import org.mightyfish.jce.interfaces.ECPrivateKey;
import org.mightyfish.jce.interfaces.ECPublicKey;
import org.mightyfish.jce.provider.BouncyCastleProvider;
import org.mightyfish.jce.spec.ECParameterSpec;
import org.mightyfish.jce.spec.ECPrivateKeySpec;
import org.mightyfish.jce.spec.ECPublicKeySpec;

public class KeyFactorySpi
    extends BaseKeyFactorySpi
    implements AsymmetricKeyInfoConverter
{
    String algorithm;
    ProviderConfiguration configuration;

    KeyFactorySpi(
        String algorithm,
        ProviderConfiguration configuration)
    {
        this.algorithm = algorithm;
        this.configuration = configuration;
    }

    protected Key engineTranslateKey(
        Key    key)
        throws InvalidKeyException
    {
        if (key instanceof ECPublicKey)
        {
            return new BCECPublicKey((ECPublicKey)key, configuration);
        }
        else if (key instanceof ECPrivateKey)
        {
            return new BCECPrivateKey((ECPrivateKey)key, configuration);
        }

        throw new InvalidKeyException("key type unknown");
    }

    protected KeySpec engineGetKeySpec(
        Key    key,
        Class    spec)
    throws InvalidKeySpecException
    {
       if (spec.isAssignableFrom(org.mightyfish.jce.spec.ECPublicKeySpec.class) && key instanceof ECPublicKey)
       {
           ECPublicKey k = (ECPublicKey)key;
           if (k.getParams() != null)
           {
               return new org.mightyfish.jce.spec.ECPublicKeySpec(k.getQ(), k.getParameters());
           }
           else
           {
               ECParameterSpec implicitSpec = BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa();

               return new org.mightyfish.jce.spec.ECPublicKeySpec(k.getQ(), implicitSpec);
           }
       }
       else if (spec.isAssignableFrom(org.mightyfish.jce.spec.ECPrivateKeySpec.class) && key instanceof ECPrivateKey)
       {
           ECPrivateKey k = (ECPrivateKey)key;

           if (k.getParams() != null)
           {
               return new org.mightyfish.jce.spec.ECPrivateKeySpec(k.getD(), k.getParameters());
           }
           else
           {
               ECParameterSpec implicitSpec = configuration.getEcImplicitlyCa();

               return new org.mightyfish.jce.spec.ECPrivateKeySpec(k.getD(), implicitSpec);
           }
       }
       return super.engineGetKeySpec(key, spec);
    }

    protected PrivateKey engineGeneratePrivate(
        KeySpec keySpec)
        throws InvalidKeySpecException
    {
        if (keySpec instanceof ECPrivateKeySpec)
        {
            return new BCECPrivateKey(algorithm, (ECPrivateKeySpec)keySpec, configuration);
        }

        return super.engineGeneratePrivate(keySpec);
    }

    protected PublicKey engineGeneratePublic(
        KeySpec keySpec)
        throws InvalidKeySpecException
    {
        if (keySpec instanceof ECPublicKeySpec)
        {
            return new BCECPublicKey(algorithm, (ECPublicKeySpec)keySpec, configuration);
        }

        return super.engineGeneratePublic(keySpec);
    }

    public PrivateKey generatePrivate(PrivateKeyInfo keyInfo)
        throws IOException
    {
        ASN1ObjectIdentifier algOid = keyInfo.getPrivateKeyAlgorithm().getAlgorithm();

        if (algOid.equals(X9ObjectIdentifiers.id_ecPublicKey))
        {
            return new BCECPrivateKey(algorithm, keyInfo, configuration);
        }
        else
        {
            throw new IOException("algorithm identifier " + algOid + " in key not recognised");
        }
    }

    public PublicKey generatePublic(SubjectPublicKeyInfo keyInfo)
        throws IOException
    {
        ASN1ObjectIdentifier algOid = keyInfo.getAlgorithm().getAlgorithm();

        if (algOid.equals(X9ObjectIdentifiers.id_ecPublicKey))
        {
            return new BCECPublicKey(algorithm, keyInfo, configuration);
        }
        else
        {
            throw new IOException("algorithm identifier " + algOid + " in key not recognised");
        }
    }

    public static class EC
        extends KeyFactorySpi
    {
        public EC()
        {
            super("EC", BouncyCastleProvider.CONFIGURATION);
        }
    }

    public static class ECDSA
        extends KeyFactorySpi
    {
        public ECDSA()
        {
            super("ECDSA", BouncyCastleProvider.CONFIGURATION);
        }
    }

    public static class ECGOST3410
        extends KeyFactorySpi
    {
        public ECGOST3410()
        {
            super("ECGOST3410", BouncyCastleProvider.CONFIGURATION);
        }
    }

    public static class ECDH
        extends KeyFactorySpi
    {
        public ECDH()
        {
            super("ECDH", BouncyCastleProvider.CONFIGURATION);
        }
    }

    public static class ECDHC
        extends KeyFactorySpi
    {
        public ECDHC()
        {
            super("ECDHC", BouncyCastleProvider.CONFIGURATION);
        }
    }

    public static class ECMQV
        extends KeyFactorySpi
    {
        public ECMQV()
        {
            super("ECMQV", BouncyCastleProvider.CONFIGURATION);
        }
    }
}
