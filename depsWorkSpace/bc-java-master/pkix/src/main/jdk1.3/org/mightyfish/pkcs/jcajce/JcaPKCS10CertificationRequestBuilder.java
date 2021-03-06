package org.mightyfish.pkcs.jcajce;

import java.security.PublicKey;

import org.mightyfish.asn1.x500.X500Name;
import org.mightyfish.asn1.x509.SubjectPublicKeyInfo;
import org.mightyfish.pkcs.PKCS10CertificationRequestBuilder;

/**
 * Extension of the PKCS#10 builder to support PublicKey and X500Principal objects.
 */
public class JcaPKCS10CertificationRequestBuilder
    extends PKCS10CertificationRequestBuilder
{
    /**
     * Create a PKCS#10 builder for the passed in subject and JCA public key.
     *
     * @param subject an X500Name containing the subject associated with the request we are building.
     * @param publicKey a JCA public key that is to be associated with the request we are building.
     */
    public JcaPKCS10CertificationRequestBuilder(X500Name subject, PublicKey publicKey)
    {
        super(subject, SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()));
    }
}
