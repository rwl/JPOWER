/*
 * Copyright (C) 2010-2011 Richard Lincoln
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package edu.cornell.pserc.jpower.exceptions;

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
