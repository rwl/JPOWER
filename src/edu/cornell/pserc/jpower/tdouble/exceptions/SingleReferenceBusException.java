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

package edu.cornell.pserc.jpower.tdouble.exceptions;

public class SingleReferenceBusException extends Exception {

	private static final long serialVersionUID = 6143997284102275844L;
	private int[] refs;


	public SingleReferenceBusException(int[] refs) {
		this.refs = refs;
	}

	public SingleReferenceBusException(String message, int[] refs) {
		super(message);
		this.refs = refs;
	}

	public SingleReferenceBusException(Throwable cause, int[] refs) {
		super(cause);
		this.refs = refs;
	}

	public SingleReferenceBusException(String message, Throwable cause, int[] refs) {
		super(message, cause);
		this.refs = refs;
	}

	public int[] getRefs(){
		return refs;
	}
}
