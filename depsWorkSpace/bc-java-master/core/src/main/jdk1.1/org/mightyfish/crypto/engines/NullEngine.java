package org.mightyfish.crypto.engines;

import org.mightyfish.crypto.BlockCipher;
import org.mightyfish.crypto.CipherParameters;
import org.mightyfish.crypto.DataLengthException;
import org.mightyfish.crypto.OutputLengthException;

/**
 * The no-op engine that just copies bytes through, irrespective of whether encrypting and decrypting.
 * Provided for the sake of completeness.
 */
public class NullEngine implements BlockCipher
{
    private boolean initialised;
    protected static final int DEFAULT_BLOCK_SIZE = 1;
    private int blockSize;

    /**
     * Constructs a null engine with a block size of 1 byte.
     */
    public NullEngine()
    {
        this(DEFAULT_BLOCK_SIZE);
    }

    /**
     * Constructs a null engine with a specific block size.
     * 
     * @param blockSize the block size in bytes.
     */
    public NullEngine(int blockSize)
    {
        this.blockSize = blockSize;
    }

    /* (non-Javadoc)
     * @see org.mightyfish.crypto.BlockCipher#init(boolean, org.mightyfish.crypto.CipherParameters)
     */
    public void init(boolean forEncryption, CipherParameters params) throws IllegalArgumentException
    {
        // we don't mind any parameters that may come in
        this.initialised = true;
    }

    /* (non-Javadoc)
     * @see org.mightyfish.crypto.BlockCipher#getAlgorithmName()
     */
    public String getAlgorithmName()
    {
        return "Null";
    }

    /* (non-Javadoc)
     * @see org.mightyfish.crypto.BlockCipher#getBlockSize()
     */
    public int getBlockSize()
    {
        return blockSize;
    }

    /* (non-Javadoc)
     * @see org.mightyfish.crypto.BlockCipher#processBlock(byte[], int, byte[], int)
     */
    public int processBlock(byte[] in, int inOff, byte[] out, int outOff)
        throws DataLengthException, IllegalStateException
    {
        if (!initialised)
        {
            throw new IllegalStateException("Null engine not initialised");
        }
        if ((inOff + blockSize) > in.length)
        {
            throw new DataLengthException("input buffer too short");
        }

        if ((outOff + blockSize) > out.length)
        {
            throw new OutputLengthException("output buffer too short");
        }

        for (int i = 0; i < blockSize; ++i)
        {
            out[outOff + i] = in[inOff + i];
        }

        return blockSize;
    }

    /* (non-Javadoc)
     * @see org.mightyfish.crypto.BlockCipher#reset()
     */
    public void reset()
    {
        // nothing needs to be done
    }
}
