package org.mightyfish.cert.crmf.jcajce;

import java.security.Provider;
import java.security.PublicKey;

import org.mightyfish.asn1.crmf.CertReqMsg;
import org.mightyfish.asn1.x509.SubjectPublicKeyInfo;
import org.mightyfish.cert.crmf.CRMFException;
import org.mightyfish.cert.crmf.CertificateRequestMessage;
import org.mightyfish.jcajce.util.DefaultJcaJceHelper;
import org.mightyfish.jcajce.util.NamedJcaJceHelper;
import org.mightyfish.jcajce.util.ProviderJcaJceHelper;

public class JcaCertificateRequestMessage
    extends CertificateRequestMessage
{
    private CRMFHelper helper = new CRMFHelper(new DefaultJcaJceHelper());

    public JcaCertificateRequestMessage(CertificateRequestMessage certReqMsg)
    {
        this(certReqMsg.toASN1Structure());
    }

    public JcaCertificateRequestMessage(CertReqMsg certReqMsg)
    {
        super(certReqMsg);
    }

    public JcaCertificateRequestMessage setProvider(String providerName)
    {
        this.helper = new CRMFHelper(new NamedJcaJceHelper(providerName));

        return this;
    }

    public JcaCertificateRequestMessage setProvider(Provider provider)
    {
        this.helper = new CRMFHelper(new ProviderJcaJceHelper(provider));

        return this;
    }

    public PublicKey getPublicKey()
        throws CRMFException
    {
        SubjectPublicKeyInfo subjectPublicKeyInfo = getCertTemplate().getPublicKey();

        if (subjectPublicKeyInfo != null)
        {
            return helper.toPublicKey(subjectPublicKeyInfo);
        }

        return null;
    }
}
