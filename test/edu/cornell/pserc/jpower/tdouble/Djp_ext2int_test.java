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

package edu.cornell.pserc.jpower.tdouble;

import edu.cornell.pserc.jpower.tdouble.jpc.Djp_jpc;

/**
 *
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public abstract class Djp_ext2int_test extends Djp_base_test {

	protected Djp_jpc jpc;

	public Djp_ext2int_test(String name) {
		super(name);
		this.fname = "ext2int";
		/* Set 'jpc' in subclasses. */
	}

	public void test_ext2int__jpc() {
		Djp_jpc jpc = Djp_loadcase.jp_loadcase(this.jpc);
		test_jpc(Djp_ext2int.jp_ext2int(jpc));
	}

	// TODO: test overloaded methods.

}
