package edu.cornell.pserc.jpower.test;

import java.util.Map;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import edu.cornell.pserc.jpower.jpc.JPC;

public class JPCExt extends JPC {

	public DoubleMatrix2D xbus;

	public DoubleMatrix2D xgen;

	public DoubleMatrix2D xbranch;

	public DoubleMatrix2D xrows;

	public DoubleMatrix2D xcols;

	public Map<String, DoubleMatrix2D> x;

}
