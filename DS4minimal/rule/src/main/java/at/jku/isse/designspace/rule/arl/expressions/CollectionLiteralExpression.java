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
package at.jku.isse.designspace.rule.arl.expressions;

import java.util.Collection;

public abstract class CollectionLiteralExpression<RT extends Collection<AT>,AT> extends Expression<RT> {

	protected Collection<Expression<AT>> literals;

	public CollectionLiteralExpression(Collection<Expression<AT>> literals) {
		super();
		this.literals = literals;
		for (Expression<AT> literal : literals) literal.setParent(this);
	}

	@Override
	public String getARL() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (Expression child : getChildren()) {
			sb.append(child.getARL());
			sb.append(";");
		}
		sb.append("}");
		return sb.toString();
	}
	
	@Override
	public String getOriginalARL(int indentation, boolean isOnNewLine) {
		String whitespace = createWhitespace(indentation);
		StringBuilder sb = new StringBuilder();
		sb.append(whitespace);
		for (Expression child : getChildren()) {
			sb.append(child.getARL());
			sb.append(" ");
		}
		//sb.append("}");
		return sb.toString();
	}
	
	@Override 
	public String getLocalARL() { return "(...)";	}
}
