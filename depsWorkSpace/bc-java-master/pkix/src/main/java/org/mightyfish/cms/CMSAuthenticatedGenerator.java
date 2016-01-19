package org.mightyfish.cms;

import java.util.HashMap;
import java.util.Map;

import org.mightyfish.asn1.ASN1ObjectIdentifier;
import org.mightyfish.asn1.x509.AlgorithmIdentifier;
import org.mightyfish.util.Arrays;

public class CMSAuthenticatedGenerator
    extends CMSEnvelopedGenerator
{
    protected CMSAttributeTableGenerator authGen;
    protected CMSAttributeTableGenerator unauthGen;

    /**
     * base constructor
     */
    public CMSAuthenticatedGenerator()
    {
    }

    public void setAuthenticatedAttributeGenerator(CMSAttributeTableGenerator authGen)
    {
        this.authGen = authGen;
    }

    public void setUnauthenticatedAttributeGenerator(CMSAttributeTableGenerator unauthGen)
    {
        this.unauthGen = unauthGen;
    }

    protected Map getBaseParameters(ASN1ObjectIdentifier contentType, AlgorithmIdentifier digAlgId, byte[] hash)
    {
        Map param = new HashMap();
        param.put(CMSAttributeTableGenerator.CONTENT_TYPE, contentType);
        param.put(CMSAttributeTableGenerator.DIGEST_ALGORITHM_IDENTIFIER, digAlgId);
        param.put(CMSAttributeTableGenerator.DIGEST,  Arrays.clone(hash));
        return param;
    }
}
