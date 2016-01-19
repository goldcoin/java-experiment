package org.mightyfish.jce.interfaces;

import java.math.BigInteger;
import java.security.PublicKey;

public interface ElGamalPublicKey
    extends ElGamalKey, PublicKey
{
    public BigInteger getY();
}