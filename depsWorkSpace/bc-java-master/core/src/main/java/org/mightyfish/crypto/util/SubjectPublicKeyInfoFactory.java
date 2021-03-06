package org.mightyfish.crypto.util;

import java.io.IOException;

import org.mightyfish.asn1.ASN1Encodable;
import org.mightyfish.asn1.ASN1Integer;
import org.mightyfish.asn1.ASN1OctetString;
import org.mightyfish.asn1.DERNull;
import org.mightyfish.asn1.pkcs.PKCSObjectIdentifiers;
import org.mightyfish.asn1.pkcs.RSAPublicKey;
import org.mightyfish.asn1.x509.AlgorithmIdentifier;
import org.mightyfish.asn1.x509.SubjectPublicKeyInfo;
import org.mightyfish.asn1.x9.X962Parameters;
import org.mightyfish.asn1.x9.X9ECParameters;
import org.mightyfish.asn1.x9.X9ECPoint;
import org.mightyfish.asn1.x9.X9ObjectIdentifiers;
import org.mightyfish.crypto.params.AsymmetricKeyParameter;
import org.mightyfish.crypto.params.DSAPublicKeyParameters;
import org.mightyfish.crypto.params.ECDomainParameters;
import org.mightyfish.crypto.params.ECNamedDomainParameters;
import org.mightyfish.crypto.params.ECPublicKeyParameters;
import org.mightyfish.crypto.params.RSAKeyParameters;

/**
 * Factory to create ASN.1 subject public key info objects from lightweight public keys.
 */
public class SubjectPublicKeyInfoFactory
{
    /**
     * Create a SubjectPublicKeyInfo public key.
     *
     * @param publicKey the SubjectPublicKeyInfo encoding
     * @return the appropriate key parameter
     * @throws java.io.IOException on an error encoding the key
     */
    public static SubjectPublicKeyInfo createSubjectPublicKeyInfo(AsymmetricKeyParameter publicKey) throws IOException
    {
        if (publicKey instanceof RSAKeyParameters)
        {
            RSAKeyParameters pub = (RSAKeyParameters)publicKey;

            return new SubjectPublicKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE), new RSAPublicKey(pub.getModulus(), pub.getExponent()));
        }
        else if (publicKey instanceof DSAPublicKeyParameters)
        {
            DSAPublicKeyParameters pub = (DSAPublicKeyParameters)publicKey;

            return new SubjectPublicKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_dsa), new ASN1Integer(pub.getY()));
        }
        else if (publicKey instanceof ECPublicKeyParameters)
        {
            ECPublicKeyParameters pub = (ECPublicKeyParameters)publicKey;
            ECDomainParameters domainParams = pub.getParameters();
            ASN1Encodable      params;

            if (domainParams == null)
            {
                params = new X962Parameters(DERNull.INSTANCE);      // Implicitly CA
            }
            else if (domainParams instanceof ECNamedDomainParameters)
            {
                params = new X962Parameters(((ECNamedDomainParameters)domainParams).getName());
            }
            else
            {
                X9ECParameters ecP = new X9ECParameters(
                    domainParams.getCurve(),
                    domainParams.getG(),
                    domainParams.getN(),
                    domainParams.getH(),
                    domainParams.getSeed());

                params = new X962Parameters(ecP);
            }

            ASN1OctetString p = (ASN1OctetString)new X9ECPoint(pub.getQ()).toASN1Primitive();

            return new SubjectPublicKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, params), p.getOctets());
        }
        else
        {
            throw new IOException("key parameters not recognised.");
        }
    }
}
