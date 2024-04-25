/**
 * ModelAnalyzerFramework
 * (C) Johannes Kepler University Linz, Austria, 2005-2013
 * Institute for Systems Engineering and Automation (SEA)
 * <p>
 * The software may only be used for academic purposes (teaching, scientific research). Any
 * redistribution or commercialization of the software program and documentation (or any part
 * thereof) requires prior written permission of the JKU. Redistributions of source code must retain
 * the above copyright notice, this list of conditions and the following disclaimer.
 * This software program and documentation are copyrighted by Johannes Kepler University Linz,
 * Austria (the JKU). The software program and documentation are supplied AS IS, without
 * any accompanying services from the JKU. The JKU does not warrant that the operation of the program
 * will be uninterrupted or error-free. The end-user understands that the program was developed for
 * research purposes and is advised not to rely exclusively on the program for any reason.
 * <p>
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR
 * CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS
 * DOCUMENTATION, EVEN IF THE AUTHOR HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. THE AUTHOR
 * SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE AUTHOR HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 * ENHANCEMENTS, OR MODIFICATIONS.
 */
package at.jku.isse.designspace.rule.arl.evaluator;

import at.jku.isse.designspace.rule.arl.exception.ParsingException;
import at.jku.isse.designspace.rule.arl.expressions.Expression;
import at.jku.isse.designspace.rule.arl.expressions.RootExpression;
import at.jku.isse.designspace.rule.arl.parser.ArlParser;
import at.jku.isse.designspace.rule.arl.parser.ArlType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RuleDefinitionImpl<E> implements RuleDefinition<E> {

    protected String rule=null;
    protected String name=null;
    protected ArlType contextType=null;
    protected String ruleError=null;
    protected Expression<Object> syntaxTree=null;

    public RuleDefinitionImpl(String name, String rule, ArlType contextType) {
        setName(name);
        setContextType(contextType);
        setRule(rule);
    }

    @Override public String getName() { return this.name; }
    @Override public void setName(String name) { this.name = name; }

    @Override public String getRule() {
        return rule;
    }
    @Override public void setRule(String rule) {
        ruleError = null;
        ArlParser parser = new ArlParser();
        try {
            syntaxTree = new RootExpression((Expression) parser.parse(rule, contextType, null));
        }
        catch (ParsingException ex) {
            if (log.isDebugEnabled()) ex.printStackTrace();
            setRuleError(String.format("Parsing error in \"%s\": %s (Line=%d, Column=%d)", rule, ex.getMessage(), parser.getLine(), parser.getColumn()));
        }
        catch (Exception ex) {
            ex.printStackTrace();
            setRuleError(String.format("Error in \"%s\": %s (", rule, ex.getMessage()));
        }
        this.rule = rule;
    }

    @Override public ArlType getContextType() { return this.contextType; }
    @Override public void setContextType(ArlType contextType) { this.contextType = contextType; }

    @Override public String getRuleError() { return this.ruleError; }
    @Override public void setRuleError(String ruleError) { this.ruleError=ruleError; }

    @Override
    public void delete() {
        rule = null;
        ruleError = null;
    }

    @Override
    public Expression<Object> getSyntaxTree() {
        return this.syntaxTree;
    }
}
