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
public abstract class Djp_rundcpf_test extends Djp_base_test {

	protected Djp_jpc jpc;

	public Djp_rundcpf_test(String name) {
		super(name);
		this.fname = "rundcpf";
		/* Set 'jpc' in subclasses. */
	}

//	public void test_rundcpf__string() {
//		test_jpc(Djp_rundcpf.jp_rundcpf(casename));
//	}

	public void test_rundcpf__jpc() {
		test_jpc(Djp_rundcpf.jp_rundcpf(this.jpc));
	}

}
