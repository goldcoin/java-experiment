package org.mightyfish.cms.jcajce;

import java.security.cert.X509CertSelector;

import org.mightyfish.asn1.ASN1OctetString;
import org.mightyfish.asn1.x500.X500Name;
import org.mightyfish.cms.KeyTransRecipientId;
import org.mightyfish.cms.SignerId;

public class JcaSelectorConverter
{
    public JcaSelectorConverter()
    {

    }

    public SignerId getSignerId(X509CertSelector certSelector)
    {
try
{
        if (certSelector.getSubjectKeyIdentifier() != null)
        {
            return new SignerId(X500Name.getInstance(certSelector.getIssuerAsBytes()), certSelector.getSerialNumber(), ASN1OctetString.getInstance(certSelector.getSubjectKeyIdentifier()).getOctets());
        }
        else
        {
            return new SignerId(X500Name.getInstance(certSelector.getIssuerAsBytes()), certSelector.getSerialNumber());
        }
}
catch (Exception e)
{
    throw new IllegalArgumentException("conversion failed: " + e.toString());
}
    }

    public KeyTransRecipientId getKeyTransRecipientId(X509CertSelector certSelector)
    {
try
{
        if (certSelector.getSubjectKeyIdentifier() != null)
        {
            return new KeyTransRecipientId(X500Name.getInstance(certSelector.getIssuerAsBytes()), certSelector.getSerialNumber(), ASN1OctetString.getInstance(certSelector.getSubjectKeyIdentifier()).getOctets());
        }
        else
        {
            return new KeyTransRecipientId(X500Name.getInstance(certSelector.getIssuerAsBytes()), certSelector.getSerialNumber());
        }
}
catch (Exception e)
{
    throw new IllegalArgumentException("conversion failed: " + e.toString());
}
    }
}
