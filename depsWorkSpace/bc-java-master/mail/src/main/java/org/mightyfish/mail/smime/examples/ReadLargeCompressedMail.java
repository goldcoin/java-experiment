package org.mightyfish.mail.smime.examples;

import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

import org.mightyfish.cms.jcajce.ZlibExpanderProvider;
import org.mightyfish.mail.smime.SMIMECompressedParser;
import org.mightyfish.mail.smime.SMIMEUtil;
import org.mightyfish.mail.smime.util.SharedFileInputStream;

/**
 * a simple example that reads an oversize compressed email and writes data contained
 * in the compressed part into a file.
 */
public class ReadLargeCompressedMail
{
    public static void main(
        String args[])
        throws Exception
    {
        //
        // Get a Session object with the default properties.
        //         
        Properties props = System.getProperties();

        Session session = Session.getDefaultInstance(props, null);

        MimeMessage msg = new MimeMessage(session, new SharedFileInputStream("compressed.message"));

        SMIMECompressedParser     m = new SMIMECompressedParser(msg);
        MimeBodyPart              res = SMIMEUtil.toMimeBodyPart(m.getContent(new ZlibExpanderProvider()));

        ExampleUtils.dumpContent(res, args[0]);
    }
}
