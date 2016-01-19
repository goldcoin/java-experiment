package org.mightyfish.jcajce.provider.asymmetric.dstu;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;

import org.mightyfish.asn1.ASN1ObjectIdentifier;
import org.mightyfish.asn1.ua.DSTU4145NamedCurves;
import org.mightyfish.crypto.AsymmetricCipherKeyPair;
import org.mightyfish.crypto.generators.DSTU4145KeyPairGenerator;
import org.mightyfish.crypto.generators.ECKeyPairGenerator;
import org.mightyfish.crypto.params.ECDomainParameters;
import org.mightyfish.crypto.params.ECKeyGenerationParameters;
import org.mightyfish.crypto.params.ECPrivateKeyParameters;
import org.mightyfish.crypto.params.ECPublicKeyParameters;
import org.mightyfish.jcajce.provider.asymmetric.util.EC5Util;
import org.mightyfish.jce.provider.BouncyCastleProvider;
import org.mightyfish.jce.spec.ECNamedCurveGenParameterSpec;
import org.mightyfish.jce.spec.ECNamedCurveSpec;
import org.mightyfish.jce.spec.ECParameterSpec;
import org.mightyfish.math.ec.ECCurve;
import org.mightyfish.math.ec.ECPoint;

public class KeyPairGeneratorSpi
    extends java.security.KeyPairGenerator
{
    Object ecParams = null;
    ECKeyPairGenerator engine = new DSTU4145KeyPairGenerator();

    String algorithm = "DSTU4145";
    ECKeyGenerationParameters param;
    //int strength = 239;
    SecureRandom random = null;
    boolean initialised = false;

    public KeyPairGeneratorSpi()
    {
        super("DSTU4145");
    }

    public void initialize(
        int strength,
        SecureRandom random)
    {
        this.random = random;

        if (ecParams != null)
        {
            try
            {
                initialize((ECGenParameterSpec)ecParams, random);
            }
            catch (InvalidAlgorithmParameterException e)
            {
                throw new InvalidParameterException("key size not configurable.");
            }
        }
        else
        {
            throw new InvalidParameterException("unknown key size.");
        }
    }

    public void initialize(
        AlgorithmParameterSpec params,
        SecureRandom random)
        throws InvalidAlgorithmParameterException
    {
        if (params instanceof ECParameterSpec)
        {
            ECParameterSpec p = (ECParameterSpec)params;
            this.ecParams = params;

            param = new ECKeyGenerationParameters(new ECDomainParameters(p.getCurve(), p.getG(), p.getN()), random);

            engine.init(param);
            initialised = true;
        }
        else if (params instanceof java.security.spec.ECParameterSpec)
        {
            java.security.spec.ECParameterSpec p = (java.security.spec.ECParameterSpec)params;
            this.ecParams = params;

            ECCurve curve = EC5Util.convertCurve(p.getCurve());
            ECPoint g = EC5Util.convertPoint(curve, p.getGenerator(), false);

            param = new ECKeyGenerationParameters(new ECDomainParameters(curve, g, p.getOrder(), BigInteger.valueOf(p.getCofactor())), random);

            engine.init(param);
            initialised = true;
        }
        else if (params instanceof ECGenParameterSpec || params instanceof ECNamedCurveGenParameterSpec)
        {
            String curveName;

            if (params instanceof ECGenParameterSpec)
            {
                curveName = ((ECGenParameterSpec)params).getName();
            }
            else
            {
                curveName = ((ECNamedCurveGenParameterSpec)params).getName();
            }

            //ECDomainParameters ecP = ECGOST3410NamedCurves.getByName(curveName);
            ECDomainParameters ecP = DSTU4145NamedCurves.getByOID(new ASN1ObjectIdentifier(curveName));
            if (ecP == null)
            {
                throw new InvalidAlgorithmParameterException("unknown curve name: " + curveName);
            }

            this.ecParams = new ECNamedCurveSpec(
                curveName,
                ecP.getCurve(),
                ecP.getG(),
                ecP.getN(),
                ecP.getH(),
                ecP.getSeed());

            java.security.spec.ECParameterSpec p = (java.security.spec.ECParameterSpec)ecParams;

            ECCurve curve = EC5Util.convertCurve(p.getCurve());
            ECPoint g = EC5Util.convertPoint(curve, p.getGenerator(), false);

            param = new ECKeyGenerationParameters(new ECDomainParameters(curve, g, p.getOrder(), BigInteger.valueOf(p.getCofactor())), random);

            engine.init(param);
            initialised = true;
        }
        else if (params == null && BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa() != null)
        {
            ECParameterSpec p = BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa();
            this.ecParams = params;

            param = new ECKeyGenerationParameters(new ECDomainParameters(p.getCurve(), p.getG(), p.getN()), random);

            engine.init(param);
            initialised = true;
        }
        else if (params == null && BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa() == null)
        {
            throw new InvalidAlgorithmParameterException("null parameter passed but no implicitCA set");
        }
        else
        {
            throw new InvalidAlgorithmParameterException("parameter object not a ECParameterSpec: " + params.getClass().getName());
        }
    }

    public KeyPair generateKeyPair()
    {
        if (!initialised)
        {
            throw new IllegalStateException("DSTU Key Pair Generator not initialised");
        }

        AsymmetricCipherKeyPair pair = engine.generateKeyPair();
        ECPublicKeyParameters pub = (ECPublicKeyParameters)pair.getPublic();
        ECPrivateKeyParameters priv = (ECPrivateKeyParameters)pair.getPrivate();

        if (ecParams instanceof ECParameterSpec)
        {
            ECParameterSpec p = (ECParameterSpec)ecParams;

            BCDSTU4145PublicKey pubKey = new BCDSTU4145PublicKey(algorithm, pub, p);
            return new KeyPair(pubKey,
                new BCDSTU4145PrivateKey(algorithm, priv, pubKey, p));
        }
        else if (ecParams == null)
        {
            return new KeyPair(new BCDSTU4145PublicKey(algorithm, pub),
                new BCDSTU4145PrivateKey(algorithm, priv));
        }
        else
        {
            java.security.spec.ECParameterSpec p = (java.security.spec.ECParameterSpec)ecParams;

            BCDSTU4145PublicKey pubKey = new BCDSTU4145PublicKey(algorithm, pub, p);

            return new KeyPair(pubKey, new BCDSTU4145PrivateKey(algorithm, priv, pubKey, p));
        }
    }
}

