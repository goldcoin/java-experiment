package org.mightyfish.pkcs.jcajce;

import java.io.InputStream;
import java.security.Provider;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.mightyfish.asn1.ASN1Encodable;
import org.mightyfish.asn1.ASN1ObjectIdentifier;
import org.mightyfish.asn1.ASN1OctetString;
import org.mightyfish.asn1.cryptopro.GOST28147Parameters;
import org.mightyfish.asn1.pkcs.PBES2Parameters;
import org.mightyfish.asn1.pkcs.PBKDF2Params;
import org.mightyfish.asn1.pkcs.PKCS12PBEParams;
import org.mightyfish.asn1.pkcs.PKCSObjectIdentifiers;
import org.mightyfish.asn1.x509.AlgorithmIdentifier;
import org.mightyfish.jcajce.provider.symmetric.util.BCPBEKey;
import org.mightyfish.jcajce.spec.GOST28147ParameterSpec;
import org.mightyfish.jcajce.spec.PBKDF2KeySpec;
import org.mightyfish.jcajce.util.DefaultJcaJceHelper;
import org.mightyfish.jcajce.util.JcaJceHelper;
import org.mightyfish.jcajce.util.NamedJcaJceHelper;
import org.mightyfish.jcajce.util.ProviderJcaJceHelper;
import org.mightyfish.operator.DefaultSecretKeySizeProvider;
import org.mightyfish.operator.InputDecryptor;
import org.mightyfish.operator.InputDecryptorProvider;
import org.mightyfish.operator.OperatorCreationException;
import org.mightyfish.operator.SecretKeySizeProvider;

public class JcePKCSPBEInputDecryptorProviderBuilder
{
    private JcaJceHelper helper = new DefaultJcaJceHelper();
    private boolean      wrongPKCS12Zero = false;
    private SecretKeySizeProvider keySizeProvider = DefaultSecretKeySizeProvider.INSTANCE;

    public JcePKCSPBEInputDecryptorProviderBuilder()
    {
    }

    public JcePKCSPBEInputDecryptorProviderBuilder setProvider(Provider provider)
    {
        this.helper = new ProviderJcaJceHelper(provider);

        return this;
    }

    public JcePKCSPBEInputDecryptorProviderBuilder setProvider(String providerName)
    {
        this.helper = new NamedJcaJceHelper(providerName);

        return this;
    }

    public JcePKCSPBEInputDecryptorProviderBuilder setTryWrongPKCS12Zero(boolean tryWrong)
    {
        this.wrongPKCS12Zero = tryWrong;

        return this;
    }

    /**
     * Set the lookup provider of AlgorithmIdentifier returning key_size_in_bits used to
     * handle PKCS5 decryption.
     *
     * @param keySizeProvider  a provider of integer secret key sizes.
     *
     * @return the current builder.
     */
    public JcePKCSPBEInputDecryptorProviderBuilder setKeySizeProvider(SecretKeySizeProvider keySizeProvider)
    {
        this.keySizeProvider = keySizeProvider;

        return this;
    }

    public InputDecryptorProvider build(final char[] password)
    {
        return new InputDecryptorProvider()
        {
            private Cipher cipher;
            private SecretKey key;
            private AlgorithmIdentifier encryptionAlg;

            public InputDecryptor get(final AlgorithmIdentifier algorithmIdentifier)
                throws OperatorCreationException
            {
                ASN1ObjectIdentifier algorithm = algorithmIdentifier.getAlgorithm();

                try
                {
                    if (algorithm.on(PKCSObjectIdentifiers.pkcs_12PbeIds))
                    {
                        PKCS12PBEParams pbeParams = PKCS12PBEParams.getInstance(algorithmIdentifier.getParameters());

                        PBEKeySpec pbeSpec = new PBEKeySpec(password);

                        SecretKeyFactory keyFact = helper.createSecretKeyFactory(algorithm.getId());

                        PBEParameterSpec defParams = new PBEParameterSpec(
                            pbeParams.getIV(),
                            pbeParams.getIterations().intValue());

                        key = keyFact.generateSecret(pbeSpec);

                        if (key instanceof BCPBEKey)
                        {
                            ((BCPBEKey)key).setTryWrongPKCS12Zero(wrongPKCS12Zero);
                        }

                        cipher = helper.createCipher(algorithm.getId());

                        cipher.init(Cipher.DECRYPT_MODE, key, defParams);

                        encryptionAlg = algorithmIdentifier;
                    }
                    else if (algorithm.equals(PKCSObjectIdentifiers.id_PBES2))
                    {
                        PBES2Parameters alg = PBES2Parameters.getInstance(algorithmIdentifier.getParameters());
                        PBKDF2Params func = PBKDF2Params.getInstance(alg.getKeyDerivationFunc().getParameters());
                        AlgorithmIdentifier encScheme = AlgorithmIdentifier.getInstance(alg.getEncryptionScheme());

                        SecretKeyFactory keyFact = helper.createSecretKeyFactory(alg.getKeyDerivationFunc().getAlgorithm().getId());

                        if (func.isDefaultPrf())
                        {
                            key = keyFact.generateSecret(new PBEKeySpec(password, func.getSalt(), func.getIterationCount().intValue(), keySizeProvider.getKeySize(encScheme)));
                        }
                        else
                        {
                            key = keyFact.generateSecret(new PBKDF2KeySpec(password, func.getSalt(), func.getIterationCount().intValue(), keySizeProvider.getKeySize(encScheme), func.getPrf()));
                        }

                        cipher = helper.createCipher(alg.getEncryptionScheme().getAlgorithm().getId());

                        encryptionAlg = AlgorithmIdentifier.getInstance(alg.getEncryptionScheme());

                        ASN1Encodable encParams = alg.getEncryptionScheme().getParameters();
                        if (encParams instanceof ASN1OctetString)
                        {
                            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(ASN1OctetString.getInstance(encParams).getOctets()));
                        }
                        else
                        {
                            // TODO: at the moment it's just GOST, but...
                            GOST28147Parameters gParams = GOST28147Parameters.getInstance(encParams);

                            cipher.init(Cipher.DECRYPT_MODE, key, new GOST28147ParameterSpec(gParams.getEncryptionParamSet(), gParams.getIV()));
                        }
                    }
                }
                catch (Exception e)
                {
                    throw new OperatorCreationException("unable to create InputDecryptor: " + e.getMessage(), e);
                }

                return new InputDecryptor()
                {
                    public AlgorithmIdentifier getAlgorithmIdentifier()
                    {
                        return encryptionAlg;
                    }

                    public InputStream getInputStream(InputStream input)
                    {
                        return new CipherInputStream(input, cipher);
                    }
                };
            }
        };
    }
}
