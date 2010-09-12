#!/usr/bin/env python

__author__ = 'Richard Lincoln <r.w.lincoln@gmail.com>'

""" Creates a single precision copy of JPOWER. """

import os
import sys
import shutil

SRC_DIR = os.path.join(os.path.dirname(__file__), 'src')
BASE_PKG = os.path.join('edu', 'cornell', 'pserc', 'jpower')
D_SUBPKG = 'tdouble'
S_SUBPKG = 'tfloat'
D_PREFIX = 'D'
S_PREFIX = 'S'
DDIR = os.path.join(SRC_DIR, BASE_PKG, D_SUBPKG)
SDIR = os.path.join(SRC_DIR, BASE_PKG, S_SUBPKG)

def overwrite_files():
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

			s = s.replace(D_SUBPKG, S_SUBPKG)
			s = s.replace(D_PREFIX + "jp", S_PREFIX + "jp")
			s = s.replace("DoubleMatrix1D", "FloatMatrix1D")
			s = s.replace("DoubleMatrix2D", "FloatMatrix2D")
			s = s.replace("DoubleFunctions", "FloatFunctions")
			s = s.replace("dfunc", "sfunc")
			s = s.replace("SparseDoubleAlgebra", "SparseFloatAlgebra")

			w = open(fpath, "wb")
			w.write(s)
			w.close()

		if os.path.isdir(fpath):
			replace_doubles(fpath)

def main():
#	overwrite_files()
#	rename_files(SDIR)
	replace_doubles(SDIR)

if __name__ == '__main__':
	main()
