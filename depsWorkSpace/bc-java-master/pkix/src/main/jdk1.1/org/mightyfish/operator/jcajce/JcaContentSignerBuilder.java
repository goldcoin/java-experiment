package org.mightyfish.operator.jcajce;

import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;

import org.mightyfish.asn1.x509.AlgorithmIdentifier;
import org.mightyfish.jcajce.util.DefaultJcaJceHelper;
import org.mightyfish.jcajce.util.NamedJcaJceHelper;
import org.mightyfish.jcajce.util.ProviderJcaJceHelper;
import org.mightyfish.operator.ContentSigner;
import org.mightyfish.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.mightyfish.operator.OperatorCreationException;
import org.mightyfish.operator.OperatorStreamException;
import org.mightyfish.operator.RuntimeOperatorException;

public class JcaContentSignerBuilder
{
    private OperatorHelper helper = new OperatorHelper(new DefaultJcaJceHelper());
    private SecureRandom random;
    private String signatureAlgorithm;
    private AlgorithmIdentifier sigAlgId;

    public JcaContentSignerBuilder(String signatureAlgorithm)
    {
        this.signatureAlgorithm = signatureAlgorithm;
        this.sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find(signatureAlgorithm);
    }

    public JcaContentSignerBuilder setProvider(Provider provider)
    {
        this.helper = new OperatorHelper(new ProviderJcaJceHelper(provider));

        return this;
    }

    public JcaContentSignerBuilder setProvider(String providerName)
    {
        this.helper = new OperatorHelper(new NamedJcaJceHelper(providerName));

        return this;
    }

    public JcaContentSignerBuilder setSecureRandom(SecureRandom random)
    {
        this.random = random;

        return this;
    }

    public ContentSigner build(PrivateKey privateKey)
        throws OperatorCreationException
    {
        try
        {
            final Signature sig = helper.createSignature(sigAlgId);

            if (random != null)
            {
                sig.initSign(privateKey);
            }
            else
            {
                sig.initSign(privateKey);
            }

            return new ContentSigner()
            {
                private SignatureOutputStream stream = new SignatureOutputStream(sig);

                public AlgorithmIdentifier getAlgorithmIdentifier()
                {
                    return sigAlgId;
                }

                public OutputStream getOutputStream()
                {
                    return stream;
                }

                public byte[] getSignature()
                {
                    try
                    {
                        return stream.getSignature();
                    }
                    catch (SignatureException e)
                    {
                        throw new RuntimeOperatorException("exception obtaining signature: " + e.getMessage(), e);
                    }
                }
            };
        }
        catch (InvalidKeyException e)
        {
            throw new OperatorCreationException("cannot create signer: " + e.getMessage(), e);
        }
        catch (Exception e)
        {
            throw new OperatorCreationException("cannot create signer: " + e.getMessage(), e);
        }
    }

    private class SignatureOutputStream
        extends OutputStream
    {
        private Signature sig;

        SignatureOutputStream(Signature sig)
        {
            this.sig = sig;
        }

        public void write(byte[] bytes, int off, int len)
            throws IOException
        {
            try
            {
                sig.update(bytes, off, len);
            }
            catch (SignatureException e)
            {
                throw new OperatorStreamException("exception in content signer: " + e.getMessage(), e);
            }
        }

        public void write(byte[] bytes)
            throws IOException
        {
            try
            {
                sig.update(bytes);
            }
            catch (SignatureException e)
            {
                throw new OperatorStreamException("exception in content signer: " + e.getMessage(), e);
            }
        }

        public void write(int b)
            throws IOException
        {
            try
            {
                sig.update((byte)b);
            }
            catch (SignatureException e)
            {
                throw new OperatorStreamException("exception in content signer: " + e.getMessage(), e);
            }
        }

        byte[] getSignature()
            throws SignatureException
        {
            return sig.sign();
        }
    }
}
