package org.mightyfish.pqc.crypto.test;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.mightyfish.crypto.AsymmetricCipherKeyPair;
import org.mightyfish.crypto.Digest;
import org.mightyfish.crypto.Signer;
import org.mightyfish.crypto.digests.SHA224Digest;
import org.mightyfish.crypto.params.ParametersWithRandom;
import org.mightyfish.pqc.crypto.DigestingMessageSigner;
import org.mightyfish.pqc.crypto.gmss.GMSSDigestProvider;
import org.mightyfish.pqc.crypto.gmss.GMSSKeyGenerationParameters;
import org.mightyfish.pqc.crypto.gmss.GMSSKeyPairGenerator;
import org.mightyfish.pqc.crypto.gmss.GMSSParameters;
import org.mightyfish.pqc.crypto.gmss.GMSSPrivateKeyParameters;
import org.mightyfish.pqc.crypto.gmss.GMSSSigner;
import org.mightyfish.util.BigIntegers;
import org.mightyfish.util.encoders.Hex;
import org.mightyfish.util.test.FixedSecureRandom;
import org.mightyfish.util.test.SimpleTest;


public class GMSSSignerTest
    extends SimpleTest
{
    byte[] keyData = Hex.decode("b5014e4b60ef2ba8b6211b4062ba3224e0427dd3");

    SecureRandom keyRandom = new FixedSecureRandom(new byte[][]{keyData, keyData});

    public String getName()
    {
        return "GMSS";
    }

    public void performTest()
        throws Exception
    {

        GMSSParameters params = new GMSSParameters(3,
            new int[]{15, 15, 10}, new int[]{5, 5, 4}, new int[]{3, 3, 2});

        GMSSDigestProvider digProvider = new GMSSDigestProvider()
        {
            public Digest get()
            {
                return new SHA224Digest();
            }
        };

        GMSSKeyPairGenerator gmssKeyGen = new GMSSKeyPairGenerator(digProvider);

        GMSSKeyGenerationParameters genParam = new GMSSKeyGenerationParameters(keyRandom, params);

        gmssKeyGen.init(genParam);

        AsymmetricCipherKeyPair pair = gmssKeyGen.generateKeyPair();

        ParametersWithRandom param = new ParametersWithRandom(pair.getPrivate(), keyRandom);

        // TODO
        Signer gmssSigner = new DigestingMessageSigner(new GMSSSigner(digProvider), new SHA224Digest());
        gmssSigner.init(true, param);

        byte[] message = BigIntegers.asUnsignedByteArray(new BigInteger("968236873715988614170569073515315707566766479517"));
        gmssSigner.update(message, 0, message.length);
        byte[] sig = gmssSigner.generateSignature();


        gmssSigner.init(false, pair.getPublic());
        gmssSigner.update(message, 0, message.length);
        if (!gmssSigner.verifySignature(sig))
        {
            fail("verification fails");
        }

        if (!((GMSSPrivateKeyParameters)pair.getPrivate()).isUsed())
        {
            fail("private key not marked as used");
        }
    }

    public static void main(
        String[] args)
    {
        runTest(new GMSSSignerTest());
    }
}
