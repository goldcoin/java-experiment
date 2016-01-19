package org.mightyfish.tsp.cms;

import org.mightyfish.tsp.TimeStampToken;

public class ImprintDigestInvalidException
    extends Exception
{
    private TimeStampToken token;

    public ImprintDigestInvalidException(String message, TimeStampToken token)
    {
        super(message);

        this.token = token;
    }

    public TimeStampToken getTimeStampToken()
    {
        return token;
    }
}
