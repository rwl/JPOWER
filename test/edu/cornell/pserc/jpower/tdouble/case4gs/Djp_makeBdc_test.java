/*
 * Copyright (C) 2010 Richard Lincoln
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 *
 */

package edu.cornell.pserc.jpower.tdouble.case4gs;

import junit.textui.TestRunner;
import edu.cornell.pserc.jpower.tdouble.cases.Djp_case4gs;

/**
 *
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class Djp_makeBdc_test extends edu.cornell.pserc.jpower.tdouble.Djp_makeBdc_test {

	public static void main(String[] args) {
		TestRunner.run(Djp_makeBdc_test.class);
	}

	public Djp_makeBdc_test(String name) {
		super(name);
		this.casename = "case4gs";
		this.jpc = Djp_case4gs.jp_case4gs();
	}

}
