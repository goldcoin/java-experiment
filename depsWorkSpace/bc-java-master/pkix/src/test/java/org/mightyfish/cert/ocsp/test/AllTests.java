package org.mightyfish.cert.ocsp.test;

import java.security.Security;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.mightyfish.jce.provider.BouncyCastleProvider;
import org.mightyfish.util.test.SimpleTestResult;

public class AllTests
    extends TestCase
{
    public void testOCSP()
    {   
        Security.addProvider(new BouncyCastleProvider());
        
        org.mightyfish.util.test.Test[] tests = new org.mightyfish.util.test.Test[] { new OCSPTest() };
        
        for (int i = 0; i != tests.length; i++)
        {
            SimpleTestResult  result = (SimpleTestResult)tests[i].perform();
            
            if (!result.isSuccessful())
            {
                fail(result.toString());
            }
        }
    }
    
    public static void main (String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite()
    {
        TestSuite suite = new TestSuite("OCSP Tests");
        
        suite.addTestSuite(AllTests.class);
        
        return suite;
    }
}
