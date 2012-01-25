/*
 * Copyright (C) 1996-2010 Power System Engineering Research Center
 * Copyright (C) 2010-2011 Richard Lincoln
 *
 * JPOWER is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * JPOWER is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPOWER. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package edu.cornell.pserc.jpower;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Ray Zimmerman
 * @author Richard Lincoln
 *
 */
public class Djp_jpver {

	public static Map<String, String> jpver(String all) {
		Map<String, String> ver = new HashMap<String, String>();
		ver.put("Name", "JPOWER");
		ver.put("Version", "4.1.0");
		ver.put("Release", "");
		ver.put("Date", "26-Jan-2012");
		return ver;
	}

}
