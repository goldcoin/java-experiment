package org.mightyfish.tsp.cms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.mightyfish.asn1.ASN1OctetString;
import org.mightyfish.asn1.BEROctetString;
import org.mightyfish.asn1.DERIA5String;
import org.mightyfish.asn1.cms.CMSObjectIdentifiers;
import org.mightyfish.asn1.cms.ContentInfo;
import org.mightyfish.asn1.cms.Evidence;
import org.mightyfish.asn1.cms.TimeStampAndCRL;
import org.mightyfish.asn1.cms.TimeStampTokenEvidence;
import org.mightyfish.asn1.cms.TimeStampedData;
import org.mightyfish.cms.CMSException;
import org.mightyfish.tsp.TimeStampToken;
import org.mightyfish.util.io.Streams;

public class CMSTimeStampedDataGenerator
    extends CMSTimeStampedGenerator
{
    public CMSTimeStampedData generate(TimeStampToken timeStamp) throws CMSException
    {
        return generate(timeStamp, (InputStream)null);
    }

    public CMSTimeStampedData generate(TimeStampToken timeStamp, byte[] content) throws CMSException
    {
        return generate(timeStamp, new ByteArrayInputStream(content));
    }

    public CMSTimeStampedData generate(TimeStampToken timeStamp, InputStream content)
        throws CMSException
    {
        ByteArrayOutputStream contentOut = new ByteArrayOutputStream();

        if (content != null)
        {
            try
            {
                Streams.pipeAll(content, contentOut);
            }
            catch (IOException e)
            {
                throw new CMSException("exception encapsulating content: " + e.getMessage(), e);
            }
        }

        ASN1OctetString encContent = null;

        if (contentOut.size() != 0)
        {
            encContent = new BEROctetString(contentOut.toByteArray());
        }

        TimeStampAndCRL stamp = new TimeStampAndCRL(timeStamp.toCMSSignedData().toASN1Structure());

        DERIA5String asn1DataUri = null;

        if (dataUri != null)
        {
            asn1DataUri = new DERIA5String(dataUri.toString());
        }
        
        return new CMSTimeStampedData(new ContentInfo(CMSObjectIdentifiers.timestampedData, new TimeStampedData(asn1DataUri, metaData, encContent, new Evidence(new TimeStampTokenEvidence(stamp)))));
    }
}

