#!/usr/bin/env python

__author__ = 'Richard Lincoln <r.w.lincoln@gmail.com>'

""" Creates a single precision copy of JPOWER. """

import os
import sys
import shutil
import re

SRC_DIR = os.path.join(os.path.dirname(__file__), 'src')
BASE_PKG = 'edu.cornell.pserc.jpower'
D_SUBPKG = 'tdouble'
S_SUBPKG = 'tfloat'
D_PREFIX = 'D'
S_PREFIX = 'S'
DDIR = os.path.join(SRC_DIR, BASE_PKG.replace('.', '/'), D_SUBPKG)
SDIR = os.path.join(SRC_DIR, BASE_PKG.replace('.', '/'), S_SUBPKG)

DOUBLE = \
	r"-(\ \ +|-)?((\ \ .[0-9]+)|([0-9]+(\ \ .[0-9]*)?))(e(\ \ +|-)?[0-9]+)?$"
FLOAT = r"[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?"


FIND_REPLACE = {
	D_SUBPKG: S_SUBPKG,
	D_PREFIX + "jp": S_PREFIX + "jp",
	"tdcomplex": "tfcomplex",
	"DoubleMatrix1D": "FloatMatrix1D",
	"DoubleMatrix2D": "FloatMatrix2D",
	"DComplexMatrix1D": "FComplexMatrix1D",
	"DComplexMatrix2D": "FComplexMatrix2D",
	"DoubleFactory1D": "FloatFactory1D",
	"DoubleFactory2D": "FloatFactory2D",
	"DComplexFactory1D": "FComplexFactory1D",
	"DComplexFactory2D": "FComplexFactory2D",
	"DoubleFunctions": "FloatFunctions",
	"DComplexFunctions": "FComplexFunctions",
	"DoubleArrayList": "FloatArrayList",
	"SparseDoubleAlgebra": "SparseFloatAlgebra",
	"dfunc": "sfunc",
	"double": "float",
	"Double": "Float",
	"Math.PI": "(float) Math.PI",
	"Math.pow": "(float) Math.pow"
}

def write_files():
#	if not os.path.exists(SDIR):
#		os.makedirs(SDIR)
	if os.path.exists(SDIR):
		shutil.rmtree(SDIR)
	shutil.copytree(DDIR, SDIR)#os.path.join(SRC_DIR, BASE_PKG))

def rename_files(dir):
	print "Renaming:", dir
	for dname in os.listdir(dir):
		dpath = os.path.join(dir, dname)
		if os.path.isfile(dpath) and dname.startswith(D_PREFIX):
			sname = S_PREFIX + dname[len(D_PREFIX):]
			print "Writing:", os.path.join(dir, sname)
			os.rename(dpath, os.path.join(dir, sname))

		if os.path.isdir(dpath):
			rename_files(dpath)

def replace_doubles(dir):
	print "Scanning:", dir
	for fname in os.listdir(dir):
		fpath = os.path.join(dir, fname)
		if os.path.isfile(fpath) and fname.startswith(S_PREFIX):
			print "Rewriting:", fpath
			r = open(fpath, "rb")
			s = r.read()
			r.close()

			for k, v in FIND_REPLACE.iteritems():
				s = s.replace(k, v)

			# TODO: append 'f' to all doubles
#			s = re.sub(DOUBLE, "", s)

			w = open(fpath, "wb")
			w.write(s)
			w.close()

		if os.path.isdir(fpath):
			replace_doubles(fpath)

def main():
	write_files()
	rename_files(SDIR)
	replace_doubles(SDIR)

if __name__ == '__main__':
	main()
