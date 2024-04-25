/**
 * ModelAnalyzerFramework
 * (C) Johannes Kepler University Linz, Austria, 2005-2013
 * Institute for Systems Engineering and Automation (SEA)
 *
 * The software may only be used for academic purposes (teaching, scientific research). Any
 * redistribution or commercialization of the software program and documentation (or any part
 * thereof) requires prior written permission of the JKU. Redistributions of source code must retain
 * the above copyright notice, this list of conditions and the following disclaimer.
 * This software program and documentation are copyrighted by Johannes Kepler University Linz,
 * Austria (the JKU). The software program and documentation are supplied AS IS, without
 * any accompanying services from the JKU. The JKU does not warrant that the operation of the program
 * will be uninterrupted or error-free. The end-user understands that the program was developed for
 * research purposes and is advised not to rely exclusively on the program for any reason.
 *
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR
 * CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS
 * DOCUMENTATION, EVEN IF THE AUTHOR HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. THE AUTHOR
 * SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE AUTHOR HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 * ENHANCEMENTS, OR MODIFICATIONS.
 */
package at.jku.isse.designspace.rule.arl.parser;

import java.util.HashMap;
import java.util.Map;

/**
 * @version 1.7
 */
public class Environment<T> {

	private final Environment parent;
	public final Map<String, Object> locals = new HashMap<String, Object>();
	private Object implicitVariable;
	private Object elementType;

	public Environment(Environment parent) {
		this.parent = parent;
	}

	public void addVariable(String name, Object variableDeclaration) {
		locals.put(name, variableDeclaration);
	}

	public Object lookupLocal(String name) {
		return locals.get(name);
	}

	public Object lookup(String name) {
		if (locals.containsKey(name)) {
			// name is in this Environment
			return locals.get(name);
		} else if (parent != null) {
			// name is in this Environment, look in out Environment
			return parent.lookup(name);
		} else {
			// unknown name
			return null;
		}
	}

	public Environment getParent() {
		return parent;
	}

	public Object getImplicitVariable() {
		return implicitVariable;
	}

	public void addAllVariables(Environment e) {
		this.locals.putAll(e.locals);
	}

	public void setImplicitVariable(Object variable) {
		this.implicitVariable = variable;
	}

	@Override
	public String toString() {
		return parent + " -> " + locals.keySet();
	}

	public Object getElementType() {
		return elementType;
	}

	public void setElementType(Object elementType) {
		this.elementType = elementType;
	}

}
