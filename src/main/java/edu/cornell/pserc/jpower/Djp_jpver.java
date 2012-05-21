/*
 * Copyright (C) 1996-2010 Power System Engineering Research Center
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
		ver.put("Version", "0.3.2");
		ver.put("Release", "");
		ver.put("Date", "21-May-2012");
		return ver;
	}

}
