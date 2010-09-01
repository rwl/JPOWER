package edu.cornell.pserc.jpower.tdcomplex;

import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import junit.framework.TestCase;

public class DZjp_runpfTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testRunPF() {
//        DZjp_runpf.jp_runpf();

        DoubleMatrix1D d = DoubleFactory1D.dense.make(6, 4);

        int[] a = (int[]) d.elements();

        for (int i = 0; i < a.length; i++) {
            System.out.println("Foo: " + a[i]);
        }
    }

}
