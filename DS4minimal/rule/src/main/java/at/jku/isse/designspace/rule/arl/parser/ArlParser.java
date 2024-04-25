// Output created by jacc on Tue Oct 27 15:06:20 CET 2020

package at.jku.isse.designspace.rule.arl.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import at.jku.isse.designspace.rule.arl.exception.ParsingException;
import at.jku.isse.designspace.rule.arl.expressions.AddExpression;
import at.jku.isse.designspace.rule.arl.expressions.AllInstancesExpression;
import at.jku.isse.designspace.rule.arl.expressions.AlwaysExpression;
import at.jku.isse.designspace.rule.arl.expressions.AndExpression;
import at.jku.isse.designspace.rule.arl.expressions.AsSoonAsExpression;
import at.jku.isse.designspace.rule.arl.expressions.AsTypeExpression;
import at.jku.isse.designspace.rule.arl.expressions.CollectExpression;
import at.jku.isse.designspace.rule.arl.expressions.CollectionRangeExpression;
import at.jku.isse.designspace.rule.arl.expressions.DivExpression;
import at.jku.isse.designspace.rule.arl.expressions.EqualsExpression;
import at.jku.isse.designspace.rule.arl.expressions.EventuallyExpression;
import at.jku.isse.designspace.rule.arl.expressions.EverytimeExpression;
import at.jku.isse.designspace.rule.arl.expressions.ExistsExpression;
import at.jku.isse.designspace.rule.arl.expressions.Expression;
import at.jku.isse.designspace.rule.arl.expressions.ForAllExpression;
import at.jku.isse.designspace.rule.arl.expressions.GTEExpression;
import at.jku.isse.designspace.rule.arl.expressions.GTExpression;
import at.jku.isse.designspace.rule.arl.expressions.IfExpression;
import at.jku.isse.designspace.rule.arl.expressions.ImpliesExpression;
import at.jku.isse.designspace.rule.arl.expressions.IsKindOfExpression;
import at.jku.isse.designspace.rule.arl.expressions.IsTypeOfExpression;
import at.jku.isse.designspace.rule.arl.expressions.IterateExpression;
import at.jku.isse.designspace.rule.arl.expressions.LTEExpression;
import at.jku.isse.designspace.rule.arl.expressions.LTExpression;
import at.jku.isse.designspace.rule.arl.expressions.LetExpression;
import at.jku.isse.designspace.rule.arl.expressions.ListLiteralExpression;
import at.jku.isse.designspace.rule.arl.expressions.LiteralExpression;
import at.jku.isse.designspace.rule.arl.expressions.MulExpression;
import at.jku.isse.designspace.rule.arl.expressions.NextExpression;
import at.jku.isse.designspace.rule.arl.expressions.NotExpression;
import at.jku.isse.designspace.rule.arl.expressions.OperationCallExpression;
import at.jku.isse.designspace.rule.arl.expressions.OrExpression;
import at.jku.isse.designspace.rule.arl.expressions.PropertyCallExpression;
import at.jku.isse.designspace.rule.arl.expressions.RejectExpression;
import at.jku.isse.designspace.rule.arl.expressions.SelectExpression;
import at.jku.isse.designspace.rule.arl.expressions.SetLiteralExpression;
import at.jku.isse.designspace.rule.arl.expressions.SubExpression;
import at.jku.isse.designspace.rule.arl.expressions.TypeExpression;
import at.jku.isse.designspace.rule.arl.expressions.UntilExpression;
import at.jku.isse.designspace.rule.arl.expressions.VariableExpression;
import at.jku.isse.designspace.rule.arl.expressions.XorExpression;

// Output created by jacc on Mon Dec 05 11:23:09 CET 2022


public class ArlParser implements ArlTokenKind {
    private int yyss = 100;
    private int yytok;
    private int yysp = 0;
    private int[] yyst;
    protected int yyerrno = (-1);
    private Object[] yysv;
    private Object yyrv;

    public boolean parse() {
        int yyn = 0;
        yysp = 0;
        yyst = new int[yyss];
        yysv = new Object[yyss];
        yytok = (getTokenKind()
        );
        loop:
        for (;;) {
            switch (yyn) {
                case 0:
                    yyst[yysp] = 0;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 193:
                    yyn = yys0();
                    continue;

                case 1:
                    yyst[yysp] = 1;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 194:
                    switch (yytok) {
                        case ENDINPUT:
                            yyn = 386;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 2:
                    yyst[yysp] = 2;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 195:
                    yyn = yys2();
                    continue;

                case 3:
                    yyst[yysp] = 3;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 196:
                    switch (yytok) {
                        case '{':
                            yyn = 35;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 4:
                    yyst[yysp] = 4;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 197:
                    yyn = yys4();
                    continue;

                case 5:
                    yyst[yysp] = 5;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 198:
                    yyn = yys5();
                    continue;

                case 6:
                    yyst[yysp] = 6;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 199:
                    yyn = yys6();
                    continue;

                case 7:
                    yyst[yysp] = 7;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 200:
                    yyn = yys7();
                    continue;

                case 8:
                    yyst[yysp] = 8;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 201:
                    yyn = yys8();
                    continue;

                case 9:
                    yyst[yysp] = 9;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 202:
                    yyn = yys9();
                    continue;

                case 10:
                    yyst[yysp] = 10;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 203:
                    yyn = yys10();
                    continue;

                case 11:
                    yyst[yysp] = 11;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 204:
                    yyn = yys11();
                    continue;

                case 12:
                    yyst[yysp] = 12;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 205:
                    switch (yytok) {
                        case '(':
                            yyn = 53;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 13:
                    yyst[yysp] = 13;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 206:
                    switch (yytok) {
                        case '(':
                            yyn = 54;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 14:
                    yyst[yysp] = 14;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 207:
                    yyn = yys14();
                    continue;

                case 15:
                    yyst[yysp] = 15;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 208:
                    switch (yytok) {
                        case NAME:
                            yyn = 24;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 16:
                    yyst[yysp] = 16;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 209:
                    switch (yytok) {
                        case '(':
                            yyn = 56;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 17:
                    yyst[yysp] = 17;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 210:
                    switch (yytok) {
                        case '(':
                            yyn = 57;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 18:
                    yyst[yysp] = 18;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 211:
                    yyn = yys18();
                    continue;

                case 19:
                    yyst[yysp] = 19;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 212:
                    yyn = yys19();
                    continue;

                case 20:
                    yyst[yysp] = 20;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 213:
                    yyn = yys20();
                    continue;

                case 21:
                    yyst[yysp] = 21;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 214:
                    switch (yytok) {
                        case NAME:
                            yyn = 24;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 22:
                    yyst[yysp] = 22;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 215:
                    yyn = yys22();
                    continue;

                case 23:
                    yyst[yysp] = 23;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 216:
                    yyn = yys23();
                    continue;

                case 24:
                    yyst[yysp] = 24;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 217:
                    yyn = yys24();
                    continue;

                case 25:
                    yyst[yysp] = 25;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 218:
                    switch (yytok) {
                        case '(':
                            yyn = 61;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 26:
                    yyst[yysp] = 26;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 219:
                    yyn = yys26();
                    continue;

                case 27:
                    yyst[yysp] = 27;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 220:
                    yyn = yys27();
                    continue;

                case 28:
                    yyst[yysp] = 28;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 221:
                    yyn = yys28();
                    continue;

                case 29:
                    yyst[yysp] = 29;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 222:
                    yyn = yys29();
                    continue;

                case 30:
                    yyst[yysp] = 30;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 223:
                    yyn = yys30();
                    continue;

                case 31:
                    yyst[yysp] = 31;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 224:
                    yyn = yys31();
                    continue;

                case 32:
                    yyst[yysp] = 32;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 225:
                    switch (yytok) {
                        case '(':
                            yyn = 63;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 33:
                    yyst[yysp] = 33;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 226:
                    yyn = yys33();
                    continue;

                case 34:
                    yyst[yysp] = 34;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 227:
                    yyn = yys34();
                    continue;

                case 35:
                    yyst[yysp] = 35;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 228:
                    yyn = yys35();
                    continue;

                case 36:
                    yyst[yysp] = 36;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 229:
                    yyn = yys36();
                    continue;

                case 37:
                    yyst[yysp] = 37;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 230:
                    yyn = yys37();
                    continue;

                case 38:
                    yyst[yysp] = 38;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 231:
                    yyn = yys38();
                    continue;

                case 39:
                    yyst[yysp] = 39;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 232:
                    yyn = yys39();
                    continue;

                case 40:
                    yyst[yysp] = 40;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 233:
                    yyn = yys40();
                    continue;

                case 41:
                    yyst[yysp] = 41;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 234:
                    yyn = yys41();
                    continue;

                case 42:
                    yyst[yysp] = 42;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 235:
                    yyn = yys42();
                    continue;

                case 43:
                    yyst[yysp] = 43;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 236:
                    yyn = yys43();
                    continue;

                case 44:
                    yyst[yysp] = 44;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 237:
                    yyn = yys44();
                    continue;

                case 45:
                    yyst[yysp] = 45;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 238:
                    yyn = yys45();
                    continue;

                case 46:
                    yyst[yysp] = 46;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 239:
                    yyn = yys46();
                    continue;

                case 47:
                    yyst[yysp] = 47;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 240:
                    switch (yytok) {
                        case NAME:
                            yyn = 24;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 48:
                    yyst[yysp] = 48;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 241:
                    yyn = yys48();
                    continue;

                case 49:
                    yyst[yysp] = 49;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 242:
                    yyn = yys49();
                    continue;

                case 50:
                    yyst[yysp] = 50;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 243:
                    yyn = yys50();
                    continue;

                case 51:
                    yyst[yysp] = 51;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 244:
                    yyn = yys51();
                    continue;

                case 52:
                    yyst[yysp] = 52;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 245:
                    yyn = yys52();
                    continue;

                case 53:
                    yyst[yysp] = 53;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 246:
                    yyn = yys53();
                    continue;

                case 54:
                    yyst[yysp] = 54;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 247:
                    yyn = yys54();
                    continue;

                case 55:
                    yyst[yysp] = 55;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 248:
                    switch (yytok) {
                        case ':':
                            yyn = 103;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 56:
                    yyst[yysp] = 56;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 249:
                    yyn = yys56();
                    continue;

                case 57:
                    yyst[yysp] = 57;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 250:
                    yyn = yys57();
                    continue;

                case 58:
                    yyst[yysp] = 58;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 251:
                    switch (yytok) {
                        case THEN:
                            yyn = 106;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 59:
                    yyst[yysp] = 59;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 252:
                    switch (yytok) {
                        case IN:
                            yyn = 107;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 60:
                    yyst[yysp] = 60;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 253:
                    switch (yytok) {
                        case ':':
                            yyn = 108;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 61:
                    yyst[yysp] = 61;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 254:
                    yyn = yys61();
                    continue;

                case 62:
                    yyst[yysp] = 62;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 255:
                    yyn = yys62();
                    continue;

                case 63:
                    yyst[yysp] = 63;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 256:
                    yyn = yys63();
                    continue;

                case 64:
                    yyst[yysp] = 64;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 257:
                    yyn = yys64();
                    continue;

                case 65:
                    yyst[yysp] = 65;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 258:
                    yyn = yys65();
                    continue;

                case 66:
                    yyst[yysp] = 66;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 259:
                    switch (yytok) {
                        case DOUBLEDOT:
                            yyn = 112;
                            continue;
                        case ',':
                        case '}':
                            yyn = yyr16();
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 67:
                    yyst[yysp] = 67;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 260:
                    switch (yytok) {
                        case ',':
                        case '}':
                            yyn = yyr13();
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 68:
                    yyst[yysp] = 68;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 261:
                    switch (yytok) {
                        case ',':
                            yyn = 113;
                            continue;
                        case '}':
                            yyn = 114;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 69:
                    yyst[yysp] = 69;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 262:
                    switch (yytok) {
                        case ',':
                        case '}':
                            yyn = yyr15();
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 70:
                    yyst[yysp] = 70;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 263:
                    yyn = yys70();
                    continue;

                case 71:
                    yyst[yysp] = 71;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 264:
                    yyn = yys71();
                    continue;

                case 72:
                    yyst[yysp] = 72;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 265:
                    switch (yytok) {
                        case '(':
                            yyn = 115;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 73:
                    yyst[yysp] = 73;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 266:
                    switch (yytok) {
                        case '(':
                            yyn = 116;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 74:
                    yyst[yysp] = 74;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 267:
                    switch (yytok) {
                        case '(':
                            yyn = 117;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 75:
                    yyst[yysp] = 75;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 268:
                    switch (yytok) {
                        case '(':
                            yyn = yyr73();
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 76:
                    yyst[yysp] = 76;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 269:
                    switch (yytok) {
                        case '(':
                            yyn = yyr72();
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 77:
                    yyst[yysp] = 77;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 270:
                    switch (yytok) {
                        case '(':
                            yyn = yyr71();
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 78:
                    yyst[yysp] = 78;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 271:
                    switch (yytok) {
                        case '(':
                            yyn = yyr76();
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 79:
                    yyst[yysp] = 79;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 272:
                    switch (yytok) {
                        case '(':
                            yyn = yyr74();
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 80:
                    yyst[yysp] = 80;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 273:
                    switch (yytok) {
                        case '(':
                            yyn = yyr75();
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 81:
                    yyst[yysp] = 81;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 274:
                    yyn = yys81();
                    continue;

                case 82:
                    yyst[yysp] = 82;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 275:
                    yyn = yys82();
                    continue;

                case 83:
                    yyst[yysp] = 83;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 276:
                    yyn = yys83();
                    continue;

                case 84:
                    yyst[yysp] = 84;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 277:
                    yyn = yys84();
                    continue;

                case 85:
                    yyst[yysp] = 85;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 278:
                    yyn = yys85();
                    continue;

                case 86:
                    yyst[yysp] = 86;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 279:
                    yyn = yys86();
                    continue;

                case 87:
                    yyst[yysp] = 87;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 280:
                    yyn = yys87();
                    continue;

                case 88:
                    yyst[yysp] = 88;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 281:
                    yyn = yys88();
                    continue;

                case 89:
                    yyst[yysp] = 89;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 282:
                    yyn = yys89();
                    continue;

                case 90:
                    yyst[yysp] = 90;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 283:
                    yyn = yys90();
                    continue;

                case 91:
                    yyst[yysp] = 91;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 284:
                    yyn = yys91();
                    continue;

                case 92:
                    yyst[yysp] = 92;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 285:
                    yyn = yys92();
                    continue;

                case 93:
                    yyst[yysp] = 93;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 286:
                    yyn = yys93();
                    continue;

                case 94:
                    yyst[yysp] = 94;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 287:
                    yyn = yys94();
                    continue;

                case 95:
                    yyst[yysp] = 95;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 288:
                    switch (yytok) {
                        case ')':
                            yyn = 119;
                            continue;
                        case ',':
                            yyn = 120;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 96:
                    yyst[yysp] = 96;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 289:
                    yyn = yys96();
                    continue;

                case 97:
                    yyst[yysp] = 97;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 290:
                    yyn = yys97();
                    continue;

                case 98:
                    yyst[yysp] = 98;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 291:
                    switch (yytok) {
                        case ')':
                            yyn = 123;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 99:
                    yyst[yysp] = 99;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 292:
                    yyn = yys99();
                    continue;

                case 100:
                    yyst[yysp] = 100;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 293:
                    switch (yytok) {
                        case NAME:
                            yyn = 24;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 101:
                    yyst[yysp] = 101;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 294:
                    yyn = yys101();
                    continue;

                case 102:
                    yyst[yysp] = 102;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 295:
                    yyn = yys102();
                    continue;

                case 103:
                    yyst[yysp] = 103;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 296:
                    switch (yytok) {
                        case COLLECTION:
                            yyn = 14;
                            continue;
                        case LIST:
                            yyn = 22;
                            continue;
                        case MAP:
                            yyn = 23;
                            continue;
                        case SET:
                            yyn = 29;
                            continue;
                        case '<':
                            yyn = 100;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 104:
                    yyst[yysp] = 104;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 297:
                    yyn = yys104();
                    continue;

                case 105:
                    yyst[yysp] = 105;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 298:
                    yyn = yys105();
                    continue;

                case 106:
                    yyst[yysp] = 106;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 299:
                    yyn = yys106();
                    continue;

                case 107:
                    yyst[yysp] = 107;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 300:
                    yyn = yys107();
                    continue;

                case 108:
                    yyst[yysp] = 108;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 301:
                    switch (yytok) {
                        case COLLECTION:
                            yyn = 14;
                            continue;
                        case LIST:
                            yyn = 22;
                            continue;
                        case MAP:
                            yyn = 23;
                            continue;
                        case SET:
                            yyn = 29;
                            continue;
                        case '<':
                            yyn = 100;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 109:
                    yyst[yysp] = 109;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 302:
                    yyn = yys109();
                    continue;

                case 110:
                    yyst[yysp] = 110;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 303:
                    yyn = yys110();
                    continue;

                case 111:
                    yyst[yysp] = 111;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 304:
                    yyn = yys111();
                    continue;

                case 112:
                    yyst[yysp] = 112;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 305:
                    yyn = yys112();
                    continue;

                case 113:
                    yyst[yysp] = 113;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 306:
                    yyn = yys113();
                    continue;

                case 114:
                    yyst[yysp] = 114;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 307:
                    yyn = yys114();
                    continue;

                case 115:
                    yyst[yysp] = 115;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 308:
                    switch (yytok) {
                        case NAME:
                            yyn = 24;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 116:
                    yyst[yysp] = 116;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 309:
                    yyn = yys116();
                    continue;

                case 117:
                    yyst[yysp] = 117;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 310:
                    yyn = yys117();
                    continue;

                case 118:
                    yyst[yysp] = 118;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 311:
                    yyn = yys118();
                    continue;

                case 119:
                    yyst[yysp] = 119;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 312:
                    yyn = yys119();
                    continue;

                case 120:
                    yyst[yysp] = 120;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 313:
                    yyn = yys120();
                    continue;

                case 121:
                    yyst[yysp] = 121;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 314:
                    switch (yytok) {
                        case COLLECTION:
                            yyn = 14;
                            continue;
                        case LIST:
                            yyn = 22;
                            continue;
                        case MAP:
                            yyn = 23;
                            continue;
                        case SET:
                            yyn = 29;
                            continue;
                        case '<':
                            yyn = 100;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 122:
                    yyst[yysp] = 122;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 315:
                    switch (yytok) {
                        case '=':
                        case ';':
                        case ',':
                        case ')':
                        case '|':
                            yyn = yyr25();
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 123:
                    yyst[yysp] = 123;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 316:
                    yyn = yys123();
                    continue;

                case 124:
                    yyst[yysp] = 124;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 317:
                    switch (yytok) {
                        case '>':
                        case '/':
                            yyn = yyr26();
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 125:
                    yyst[yysp] = 125;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 318:
                    switch (yytok) {
                        case '/':
                            yyn = 151;
                            continue;
                        case '>':
                            yyn = 152;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 126:
                    yyst[yysp] = 126;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 319:
                    yyn = yys126();
                    continue;

                case 127:
                    yyst[yysp] = 127;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 320:
                    yyn = yys127();
                    continue;

                case 128:
                    yyst[yysp] = 128;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 321:
                    switch (yytok) {
                        case '=':
                            yyn = 154;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 129:
                    yyst[yysp] = 129;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 322:
                    yyn = yys129();
                    continue;

                case 130:
                    yyst[yysp] = 130;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 323:
                    yyn = yys130();
                    continue;

                case 131:
                    yyst[yysp] = 131;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 324:
                    switch (yytok) {
                        case ELSE:
                            yyn = 156;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 132:
                    yyst[yysp] = 132;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 325:
                    yyn = yys132();
                    continue;

                case 133:
                    yyst[yysp] = 133;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 326:
                    switch (yytok) {
                        case '=':
                            yyn = 157;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 134:
                    yyst[yysp] = 134;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 327:
                    yyn = yys134();
                    continue;

                case 135:
                    yyst[yysp] = 135;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 328:
                    yyn = yys135();
                    continue;

                case 136:
                    yyst[yysp] = 136;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 329:
                    switch (yytok) {
                        case ',':
                        case '}':
                            yyn = yyr17();
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 137:
                    yyst[yysp] = 137;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 330:
                    switch (yytok) {
                        case ',':
                        case '}':
                            yyn = yyr14();
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 138:
                    yyst[yysp] = 138;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 331:
                    switch (yytok) {
                        case '|':
                            yyn = 159;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 139:
                    yyst[yysp] = 139;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 332:
                    switch (yytok) {
                        case ';':
                            yyn = 160;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 140:
                    yyst[yysp] = 140;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 333:
                    switch (yytok) {
                        case ':':
                            yyn = 161;
                            continue;
                        case ';':
                            yyn = yyr77();
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 141:
                    yyst[yysp] = 141;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 334:
                    switch (yytok) {
                        case ',':
                            yyn = 162;
                            continue;
                        case '|':
                            yyn = 163;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 142:
                    yyst[yysp] = 142;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 335:
                    yyn = yys142();
                    continue;

                case 143:
                    yyst[yysp] = 143;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 336:
                    yyn = yys143();
                    continue;

                case 144:
                    yyst[yysp] = 144;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 337:
                    switch (yytok) {
                        case ',':
                            yyn = 120;
                            continue;
                        case ')':
                            yyn = 166;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 145:
                    yyst[yysp] = 145;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 338:
                    switch (yytok) {
                        case ')':
                            yyn = 167;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 146:
                    yyst[yysp] = 146;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 339:
                    yyn = yys146();
                    continue;

                case 147:
                    yyst[yysp] = 147;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 340:
                    switch (yytok) {
                        case ',':
                            yyn = 120;
                            continue;
                        case ')':
                            yyn = 168;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 148:
                    yyst[yysp] = 148;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 341:
                    switch (yytok) {
                        case ')':
                            yyn = 169;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 149:
                    yyst[yysp] = 149;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 342:
                    yyn = yys149();
                    continue;

                case 150:
                    yyst[yysp] = 150;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 343:
                    yyn = yys150();
                    continue;

                case 151:
                    yyst[yysp] = 151;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 344:
                    switch (yytok) {
                        case NAME:
                            yyn = 24;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 152:
                    yyst[yysp] = 152;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 345:
                    switch (yytok) {
                        case '=':
                        case ';':
                        case ',':
                        case ')':
                        case '|':
                            yyn = yyr24();
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 153:
                    yyst[yysp] = 153;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 346:
                    yyn = yys153();
                    continue;

                case 154:
                    yyst[yysp] = 154;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 347:
                    yyn = yys154();
                    continue;

                case 155:
                    yyst[yysp] = 155;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 348:
                    yyn = yys155();
                    continue;

                case 156:
                    yyst[yysp] = 156;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 349:
                    yyn = yys156();
                    continue;

                case 157:
                    yyst[yysp] = 157;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 350:
                    yyn = yys157();
                    continue;

                case 158:
                    yyst[yysp] = 158;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 351:
                    yyn = yys158();
                    continue;

                case 159:
                    yyst[yysp] = 159;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 352:
                    yyn = yys159();
                    continue;

                case 160:
                    yyst[yysp] = 160;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 353:
                    switch (yytok) {
                        case NAME:
                            yyn = 24;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 161:
                    yyst[yysp] = 161;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 354:
                    switch (yytok) {
                        case COLLECTION:
                            yyn = 14;
                            continue;
                        case LIST:
                            yyn = 22;
                            continue;
                        case MAP:
                            yyn = 23;
                            continue;
                        case SET:
                            yyn = 29;
                            continue;
                        case '<':
                            yyn = 100;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 162:
                    yyst[yysp] = 162;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 355:
                    switch (yytok) {
                        case NAME:
                            yyn = 24;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 163:
                    yyst[yysp] = 163;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 356:
                    yyn = yys163();
                    continue;

                case 164:
                    yyst[yysp] = 164;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 357:
                    yyn = yys164();
                    continue;

                case 165:
                    yyst[yysp] = 165;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 358:
                    switch (yytok) {
                        case COLLECTION:
                            yyn = 14;
                            continue;
                        case LIST:
                            yyn = 22;
                            continue;
                        case MAP:
                            yyn = 23;
                            continue;
                        case SET:
                            yyn = 29;
                            continue;
                        case '<':
                            yyn = 100;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 166:
                    yyst[yysp] = 166;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 359:
                    yyn = yys166();
                    continue;

                case 167:
                    yyst[yysp] = 167;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 360:
                    yyn = yys167();
                    continue;

                case 168:
                    yyst[yysp] = 168;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 361:
                    yyn = yys168();
                    continue;

                case 169:
                    yyst[yysp] = 169;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 362:
                    yyn = yys169();
                    continue;

                case 170:
                    yyst[yysp] = 170;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 363:
                    switch (yytok) {
                        case '>':
                        case '/':
                            yyn = yyr27();
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 171:
                    yyst[yysp] = 171;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 364:
                    yyn = yys171();
                    continue;

                case 172:
                    yyst[yysp] = 172;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 365:
                    yyn = yys172();
                    continue;

                case 173:
                    yyst[yysp] = 173;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 366:
                    yyn = yys173();
                    continue;

                case 174:
                    yyst[yysp] = 174;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 367:
                    switch (yytok) {
                        case ENDIF:
                            yyn = 184;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 175:
                    yyst[yysp] = 175;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 368:
                    yyn = yys175();
                    continue;

                case 176:
                    yyst[yysp] = 176;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 369:
                    yyn = yys176();
                    continue;

                case 177:
                    yyst[yysp] = 177;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 370:
                    yyn = yys177();
                    continue;

                case 178:
                    yyst[yysp] = 178;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 371:
                    switch (yytok) {
                        case '|':
                            yyn = 186;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 179:
                    yyst[yysp] = 179;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 372:
                    switch (yytok) {
                        case '=':
                            yyn = 157;
                            continue;
                        case ';':
                            yyn = yyr78();
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 180:
                    yyst[yysp] = 180;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 373:
                    switch (yytok) {
                        case '|':
                            yyn = 187;
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 181:
                    yyst[yysp] = 181;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 374:
                    switch (yytok) {
                        case ':':
                            yyn = 165;
                            continue;
                        case '|':
                            yyn = yyr77();
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 182:
                    yyst[yysp] = 182;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 375:
                    yyn = yys182();
                    continue;

                case 183:
                    yyst[yysp] = 183;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 376:
                    switch (yytok) {
                        case ',':
                        case '|':
                            yyn = yyr78();
                            continue;
                    }
                    yyn = 389;
                    continue;

                case 184:
                    yyst[yysp] = 184;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 377:
                    yyn = yys184();
                    continue;

                case 185:
                    yyst[yysp] = 185;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 378:
                    yyn = yys185();
                    continue;

                case 186:
                    yyst[yysp] = 186;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 379:
                    yyn = yys186();
                    continue;

                case 187:
                    yyst[yysp] = 187;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 380:
                    yyn = yys187();
                    continue;

                case 188:
                    yyst[yysp] = 188;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 381:
                    yyn = yys188();
                    continue;

                case 189:
                    yyst[yysp] = 189;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 382:
                    yyn = yys189();
                    continue;

                case 190:
                    yyst[yysp] = 190;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 383:
                    yyn = yys190();
                    continue;

                case 191:
                    yyst[yysp] = 191;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 384:
                    yyn = yys191();
                    continue;

                case 192:
                    yyst[yysp] = 192;
                    yysv[yysp] = (currentNode
                    );
                    yytok = (nextToken()
                    );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 385:
                    yyn = yys192();
                    continue;

                case 386:
                    return true;
                case 387:
                    yyerror("stack overflow");
                case 388:
                    return false;
                case 389:
                    yyerror("syntax error");
                    return false;
            }
        }
    }

    protected void yyexpand() {
        int[] newyyst = new int[2*yyst.length];
        Object[] newyysv = new Object[2*yyst.length];
        for (int i=0; i<yyst.length; i++) {
            newyyst[i] = yyst[i];
            newyysv[i] = yysv[i];
        }
        yyst = newyyst;
        yysv = newyysv;
    }

    private int yys0() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case DEF:
                return 15;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LET:
                return 21;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys2() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr6();
        }
        return 389;
    }

    private int yys4() {
        switch (yytok) {
            case ENDIF:
            case ',':
            case ELSE:
            case THEN:
            case DOUBLEDOT:
            case ENDINPUT:
            case '}':
                return yyr2();
        }
        return 389;
    }

    private int yys5() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr33();
        }
        return 389;
    }

    private int yys6() {
        switch (yytok) {
            case ENDIF:
            case ',':
            case ELSE:
            case THEN:
            case DOUBLEDOT:
            case ENDINPUT:
            case '}':
                return yyr1();
        }
        return 389;
    }

    private int yys7() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr32();
        }
        return 389;
    }

    private int yys8() {
        switch (yytok) {
            case AND:
                return 36;
            case ARROW:
                return 37;
            case GEQ:
                return 38;
            case IMPLIES:
                return 39;
            case LEQ:
                return 40;
            case NEQ:
                return 41;
            case OR:
                return 42;
            case XOR:
                return 43;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '<':
                return 49;
            case '=':
                return 50;
            case '>':
                return 51;
            case ENDIF:
            case ',':
            case ELSE:
            case THEN:
            case DOUBLEDOT:
            case ENDINPUT:
            case '}':
                return yyr3();
        }
        return 389;
    }

    private int yys9() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr5();
        }
        return 389;
    }

    private int yys10() {
        switch (yytok) {
            case '(':
                return 52;
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr28();
        }
        return 389;
    }

    private int yys11() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr51();
        }
        return 389;
    }

    private int yys14() {
        switch (yytok) {
            case LIST:
            case '<':
            case SET:
            case '{':
            case MAP:
            case COLLECTION:
                return yyr12();
        }
        return 389;
    }

    private int yys18() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr21();
        }
        return 389;
    }

    private int yys19() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case DEF:
                return 15;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LET:
                return 21;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys20() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr18();
        }
        return 389;
    }

    private int yys22() {
        switch (yytok) {
            case LIST:
            case '<':
            case SET:
            case '{':
            case MAP:
            case COLLECTION:
                return yyr10();
        }
        return 389;
    }

    private int yys23() {
        switch (yytok) {
            case LIST:
            case '<':
            case SET:
            case '{':
            case MAP:
            case COLLECTION:
                return yyr11();
        }
        return 389;
    }

    private int yys24() {
        switch (yytok) {
            case '=':
            case '<':
            case ';':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case '+':
            case '*':
            case ')':
            case ELSE:
            case ',':
            case '(':
            case '.':
            case XOR:
            case ':':
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr4();
        }
        return 389;
    }

    private int yys26() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys27() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr23();
        }
        return 389;
    }

    private int yys28() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr19();
        }
        return 389;
    }

    private int yys29() {
        switch (yytok) {
            case LIST:
            case '<':
            case SET:
            case '{':
            case MAP:
            case COLLECTION:
                return yyr9();
        }
        return 389;
    }

    private int yys30() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr20();
        }
        return 389;
    }

    private int yys31() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr22();
        }
        return 389;
    }

    private int yys33() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys34() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys35() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case DEF:
                return 15;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LET:
                return 21;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
            case '}':
                return 70;
        }
        return 389;
    }

    private int yys36() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys37() {
        switch (yytok) {
            case NAME:
                return 24;
            case COLLECT:
                return 75;
            case EXISTS:
                return 76;
            case FORALL:
                return 77;
            case ITERATE:
                return 78;
            case REJECT:
                return 79;
            case SELECT:
                return 80;
        }
        return 389;
    }

    private int yys38() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys39() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys40() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys41() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys42() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys43() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys44() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys45() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys46() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys48() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys49() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys50() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys51() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys52() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
            case ')':
                return 99;
            case '<':
                return 100;
        }
        return 389;
    }

    private int yys53() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys54() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys56() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys57() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys61() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys62() {
        switch (yytok) {
            case ARROW:
                return 37;
            case '.':
                return 47;
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case ELSE:
            case XOR:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr50();
        }
        return 389;
    }

    private int yys63() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys64() {
        switch (yytok) {
            case AND:
                return 36;
            case ARROW:
                return 37;
            case GEQ:
                return 38;
            case IMPLIES:
                return 39;
            case LEQ:
                return 40;
            case NEQ:
                return 41;
            case OR:
                return 42;
            case XOR:
                return 43;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '<':
                return 49;
            case '=':
                return 50;
            case '>':
                return 51;
            case ')':
                return 111;
        }
        return 389;
    }

    private int yys65() {
        switch (yytok) {
            case ARROW:
                return 37;
            case '*':
                return 44;
            case '.':
                return 47;
            case '/':
                return 48;
            case '=':
            case '<':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case ')':
            case ELSE:
            case XOR:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr49();
        }
        return 389;
    }

    private int yys70() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr8();
        }
        return 389;
    }

    private int yys71() {
        switch (yytok) {
            case ARROW:
                return 37;
            case GEQ:
                return 38;
            case LEQ:
                return 40;
            case NEQ:
                return 41;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '<':
                return 49;
            case '=':
                return 50;
            case '>':
                return 51;
            case ENDIF:
            case ',':
            case ')':
            case ELSE:
            case XOR:
            case IN:
            case THEN:
            case IMPLIES:
            case DOUBLEDOT:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case AND:
                return yyr41();
        }
        return 389;
    }

    private int yys81() {
        switch (yytok) {
            case ARROW:
                return 37;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '=':
            case '<':
            case LEQ:
            case ENDIF:
            case ',':
            case ')':
            case ELSE:
            case XOR:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr47();
        }
        return 389;
    }

    private int yys82() {
        switch (yytok) {
            case AND:
                return 36;
            case ARROW:
                return 37;
            case GEQ:
                return 38;
            case LEQ:
                return 40;
            case NEQ:
                return 41;
            case OR:
                return 42;
            case XOR:
                return 43;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '<':
                return 49;
            case '=':
                return 50;
            case '>':
                return 51;
            case ENDIF:
            case ',':
            case ')':
            case ELSE:
            case IN:
            case THEN:
            case IMPLIES:
            case DOUBLEDOT:
            case ENDINPUT:
            case '}':
            case '|':
                return yyr38();
        }
        return 389;
    }

    private int yys83() {
        switch (yytok) {
            case ARROW:
                return 37;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '=':
            case '<':
            case LEQ:
            case ENDIF:
            case ',':
            case ')':
            case ELSE:
            case XOR:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr46();
        }
        return 389;
    }

    private int yys84() {
        switch (yytok) {
            case ARROW:
                return 37;
            case GEQ:
                return 38;
            case LEQ:
                return 40;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '<':
                return 49;
            case '>':
                return 51;
            case '=':
            case ENDIF:
            case ',':
            case ')':
            case ELSE:
            case XOR:
            case IN:
            case THEN:
            case IMPLIES:
            case DOUBLEDOT:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr42();
        }
        return 389;
    }

    private int yys85() {
        switch (yytok) {
            case AND:
                return 36;
            case ARROW:
                return 37;
            case GEQ:
                return 38;
            case LEQ:
                return 40;
            case NEQ:
                return 41;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '<':
                return 49;
            case '=':
                return 50;
            case '>':
                return 51;
            case ENDIF:
            case ',':
            case ')':
            case ELSE:
            case XOR:
            case IN:
            case THEN:
            case IMPLIES:
            case DOUBLEDOT:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
                return yyr40();
        }
        return 389;
    }

    private int yys86() {
        switch (yytok) {
            case AND:
                return 36;
            case ARROW:
                return 37;
            case GEQ:
                return 38;
            case LEQ:
                return 40;
            case NEQ:
                return 41;
            case OR:
                return 42;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '<':
                return 49;
            case '=':
                return 50;
            case '>':
                return 51;
            case ENDIF:
            case ',':
            case ')':
            case ELSE:
            case XOR:
            case IN:
            case THEN:
            case IMPLIES:
            case DOUBLEDOT:
            case ENDINPUT:
            case '}':
            case '|':
                return yyr39();
        }
        return 389;
    }

    private int yys87() {
        switch (yytok) {
            case ARROW:
                return 37;
            case '.':
                return 47;
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case ELSE:
            case XOR:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr36();
        }
        return 389;
    }

    private int yys88() {
        switch (yytok) {
            case ARROW:
                return 37;
            case '*':
                return 44;
            case '.':
                return 47;
            case '/':
                return 48;
            case '=':
            case '<':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case ')':
            case ELSE:
            case XOR:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr34();
        }
        return 389;
    }

    private int yys89() {
        switch (yytok) {
            case ARROW:
                return 37;
            case '*':
                return 44;
            case '.':
                return 47;
            case '/':
                return 48;
            case '=':
            case '<':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case ')':
            case ELSE:
            case XOR:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr35();
        }
        return 389;
    }

    private int yys90() {
        switch (yytok) {
            case '(':
                return 118;
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr52();
        }
        return 389;
    }

    private int yys91() {
        switch (yytok) {
            case ARROW:
                return 37;
            case '.':
                return 47;
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case ELSE:
            case XOR:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr37();
        }
        return 389;
    }

    private int yys92() {
        switch (yytok) {
            case ARROW:
                return 37;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '=':
            case '<':
            case LEQ:
            case ENDIF:
            case ',':
            case ')':
            case ELSE:
            case XOR:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr44();
        }
        return 389;
    }

    private int yys93() {
        switch (yytok) {
            case ARROW:
                return 37;
            case GEQ:
                return 38;
            case LEQ:
                return 40;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '<':
                return 49;
            case '>':
                return 51;
            case '=':
            case ENDIF:
            case ',':
            case ')':
            case ELSE:
            case XOR:
            case IN:
            case THEN:
            case IMPLIES:
            case DOUBLEDOT:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr43();
        }
        return 389;
    }

    private int yys94() {
        switch (yytok) {
            case ARROW:
                return 37;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '=':
            case '<':
            case LEQ:
            case ENDIF:
            case ',':
            case ')':
            case ELSE:
            case XOR:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr45();
        }
        return 389;
    }

    private int yys96() {
        switch (yytok) {
            case COLLECTION:
                return 14;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case SET:
                return 29;
            case '{':
                return 35;
            case '<':
                return 100;
        }
        return 389;
    }

    private int yys97() {
        switch (yytok) {
            case AND:
                return 36;
            case ARROW:
                return 37;
            case GEQ:
                return 38;
            case IMPLIES:
                return 39;
            case LEQ:
                return 40;
            case NEQ:
                return 41;
            case OR:
                return 42;
            case XOR:
                return 43;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '<':
                return 49;
            case '=':
                return 50;
            case '>':
                return 51;
            case ',':
            case ')':
                return yyr80();
        }
        return 389;
    }

    private int yys99() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr29();
        }
        return 389;
    }

    private int yys101() {
        switch (yytok) {
            case AND:
                return 36;
            case ARROW:
                return 37;
            case GEQ:
                return 38;
            case IMPLIES:
                return 39;
            case LEQ:
                return 40;
            case NEQ:
                return 41;
            case OR:
                return 42;
            case XOR:
                return 43;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '<':
                return 49;
            case '=':
                return 50;
            case '>':
                return 51;
            case ')':
                return 126;
        }
        return 389;
    }

    private int yys102() {
        switch (yytok) {
            case AND:
                return 36;
            case ARROW:
                return 37;
            case GEQ:
                return 38;
            case IMPLIES:
                return 39;
            case LEQ:
                return 40;
            case NEQ:
                return 41;
            case OR:
                return 42;
            case XOR:
                return 43;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '<':
                return 49;
            case '=':
                return 50;
            case '>':
                return 51;
            case ',':
                return 127;
        }
        return 389;
    }

    private int yys104() {
        switch (yytok) {
            case AND:
                return 36;
            case ARROW:
                return 37;
            case GEQ:
                return 38;
            case IMPLIES:
                return 39;
            case LEQ:
                return 40;
            case NEQ:
                return 41;
            case OR:
                return 42;
            case XOR:
                return 43;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '<':
                return 49;
            case '=':
                return 50;
            case '>':
                return 51;
            case ')':
                return 129;
        }
        return 389;
    }

    private int yys105() {
        switch (yytok) {
            case AND:
                return 36;
            case ARROW:
                return 37;
            case GEQ:
                return 38;
            case IMPLIES:
                return 39;
            case LEQ:
                return 40;
            case NEQ:
                return 41;
            case OR:
                return 42;
            case XOR:
                return 43;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '<':
                return 49;
            case '=':
                return 50;
            case '>':
                return 51;
            case ',':
                return 130;
        }
        return 389;
    }

    private int yys106() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case DEF:
                return 15;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LET:
                return 21;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys107() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case DEF:
                return 15;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LET:
                return 21;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys109() {
        switch (yytok) {
            case AND:
                return 36;
            case ARROW:
                return 37;
            case GEQ:
                return 38;
            case IMPLIES:
                return 39;
            case LEQ:
                return 40;
            case NEQ:
                return 41;
            case OR:
                return 42;
            case XOR:
                return 43;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '<':
                return 49;
            case '=':
                return 50;
            case '>':
                return 51;
            case ')':
                return 134;
        }
        return 389;
    }

    private int yys110() {
        switch (yytok) {
            case AND:
                return 36;
            case ARROW:
                return 37;
            case GEQ:
                return 38;
            case IMPLIES:
                return 39;
            case LEQ:
                return 40;
            case NEQ:
                return 41;
            case OR:
                return 42;
            case XOR:
                return 43;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '<':
                return 49;
            case '=':
                return 50;
            case '>':
                return 51;
            case ',':
                return 135;
        }
        return 389;
    }

    private int yys111() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr48();
        }
        return 389;
    }

    private int yys112() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case DEF:
                return 15;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LET:
                return 21;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys113() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case DEF:
                return 15;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LET:
                return 21;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys114() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr7();
        }
        return 389;
    }

    private int yys116() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys117() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
            case '<':
                return 100;
            case ')':
                return 146;
        }
        return 389;
    }

    private int yys118() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
            case '<':
                return 100;
            case ')':
                return 149;
        }
        return 389;
    }

    private int yys119() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr30();
        }
        return 389;
    }

    private int yys120() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys123() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr31();
        }
        return 389;
    }

    private int yys126() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr67();
        }
        return 389;
    }

    private int yys127() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys129() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr66();
        }
        return 389;
    }

    private int yys130() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys132() {
        switch (yytok) {
            case ENDIF:
            case ',':
            case ELSE:
            case THEN:
            case DOUBLEDOT:
            case ENDINPUT:
            case '}':
                return yyr82();
        }
        return 389;
    }

    private int yys134() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr65();
        }
        return 389;
    }

    private int yys135() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys142() {
        switch (yytok) {
            case AND:
                return 36;
            case ARROW:
                return 37;
            case GEQ:
                return 38;
            case IMPLIES:
                return 39;
            case LEQ:
                return 40;
            case NEQ:
                return 41;
            case OR:
                return 42;
            case XOR:
                return 43;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '<':
                return 49;
            case '=':
                return 50;
            case '>':
                return 51;
            case ')':
                return 164;
        }
        return 389;
    }

    private int yys143() {
        switch (yytok) {
            case '(':
                return 52;
            case ':':
                return 165;
            case ',':
            case '|':
                return yyr77();
            case '=':
            case '<':
            case '/':
            case '.':
            case '-':
            case LEQ:
            case '+':
            case '>':
            case '*':
            case ')':
            case XOR:
            case ARROW:
            case IMPLIES:
            case GEQ:
            case OR:
            case NEQ:
            case AND:
                return yyr28();
        }
        return 389;
    }

    private int yys146() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr57();
        }
        return 389;
    }

    private int yys149() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr54();
        }
        return 389;
    }

    private int yys150() {
        switch (yytok) {
            case AND:
                return 36;
            case ARROW:
                return 37;
            case GEQ:
                return 38;
            case IMPLIES:
                return 39;
            case LEQ:
                return 40;
            case NEQ:
                return 41;
            case OR:
                return 42;
            case XOR:
                return 43;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '<':
                return 49;
            case '=':
                return 50;
            case '>':
                return 51;
            case ',':
            case ')':
                return yyr81();
        }
        return 389;
    }

    private int yys153() {
        switch (yytok) {
            case AND:
                return 36;
            case ARROW:
                return 37;
            case GEQ:
                return 38;
            case IMPLIES:
                return 39;
            case LEQ:
                return 40;
            case NEQ:
                return 41;
            case OR:
                return 42;
            case XOR:
                return 43;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '<':
                return 49;
            case '=':
                return 50;
            case '>':
                return 51;
            case ')':
                return 171;
        }
        return 389;
    }

    private int yys154() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case DEF:
                return 15;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LET:
                return 21;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys155() {
        switch (yytok) {
            case AND:
                return 36;
            case ARROW:
                return 37;
            case GEQ:
                return 38;
            case IMPLIES:
                return 39;
            case LEQ:
                return 40;
            case NEQ:
                return 41;
            case OR:
                return 42;
            case XOR:
                return 43;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '<':
                return 49;
            case '=':
                return 50;
            case '>':
                return 51;
            case ')':
                return 173;
        }
        return 389;
    }

    private int yys156() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case DEF:
                return 15;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LET:
                return 21;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys157() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys158() {
        switch (yytok) {
            case AND:
                return 36;
            case ARROW:
                return 37;
            case GEQ:
                return 38;
            case IMPLIES:
                return 39;
            case LEQ:
                return 40;
            case NEQ:
                return 41;
            case OR:
                return 42;
            case XOR:
                return 43;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '<':
                return 49;
            case '=':
                return 50;
            case '>':
                return 51;
            case ')':
                return 176;
        }
        return 389;
    }

    private int yys159() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys163() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys164() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr60();
        }
        return 389;
    }

    private int yys166() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr58();
        }
        return 389;
    }

    private int yys167() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr59();
        }
        return 389;
    }

    private int yys168() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr55();
        }
        return 389;
    }

    private int yys169() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr56();
        }
        return 389;
    }

    private int yys171() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr69();
        }
        return 389;
    }

    private int yys172() {
        switch (yytok) {
            case ENDIF:
            case ',':
            case ELSE:
            case THEN:
            case DOUBLEDOT:
            case ENDINPUT:
            case '}':
                return yyr83();
        }
        return 389;
    }

    private int yys173() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr70();
        }
        return 389;
    }

    private int yys175() {
        switch (yytok) {
            case AND:
                return 36;
            case ARROW:
                return 37;
            case GEQ:
                return 38;
            case IMPLIES:
                return 39;
            case LEQ:
                return 40;
            case NEQ:
                return 41;
            case OR:
                return 42;
            case XOR:
                return 43;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '<':
                return 49;
            case '=':
                return 50;
            case '>':
                return 51;
            case IN:
            case '|':
                return yyr79();
        }
        return 389;
    }

    private int yys176() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr68();
        }
        return 389;
    }

    private int yys177() {
        switch (yytok) {
            case AND:
                return 36;
            case ARROW:
                return 37;
            case GEQ:
                return 38;
            case IMPLIES:
                return 39;
            case LEQ:
                return 40;
            case NEQ:
                return 41;
            case OR:
                return 42;
            case XOR:
                return 43;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '<':
                return 49;
            case '=':
                return 50;
            case '>':
                return 51;
            case ')':
                return 185;
        }
        return 389;
    }

    private int yys182() {
        switch (yytok) {
            case AND:
                return 36;
            case ARROW:
                return 37;
            case GEQ:
                return 38;
            case IMPLIES:
                return 39;
            case LEQ:
                return 40;
            case NEQ:
                return 41;
            case OR:
                return 42;
            case XOR:
                return 43;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '<':
                return 49;
            case '=':
                return 50;
            case '>':
                return 51;
            case ')':
                return 188;
        }
        return 389;
    }

    private int yys184() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr84();
        }
        return 389;
    }

    private int yys185() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr64();
        }
        return 389;
    }

    private int yys186() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys187() {
        switch (yytok) {
            case ALWAYS:
                return 12;
            case ASSOONAS:
                return 13;
            case COLLECTION:
                return 14;
            case EVENTUALLY:
                return 16;
            case EVERYTIME:
                return 17;
            case FALSE:
                return 18;
            case IF:
                return 19;
            case INTEGER:
                return 20;
            case LIST:
                return 22;
            case MAP:
                return 23;
            case NAME:
                return 24;
            case NEXT:
                return 25;
            case NOT:
                return 26;
            case NULL:
                return 27;
            case REAL:
                return 28;
            case SET:
                return 29;
            case STRING:
                return 30;
            case TRUE:
                return 31;
            case UNTIL:
                return 32;
            case '(':
                return 33;
            case '-':
                return 34;
        }
        return 389;
    }

    private int yys188() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr61();
        }
        return 389;
    }

    private int yys189() {
        switch (yytok) {
            case AND:
                return 36;
            case ARROW:
                return 37;
            case GEQ:
                return 38;
            case IMPLIES:
                return 39;
            case LEQ:
                return 40;
            case NEQ:
                return 41;
            case OR:
                return 42;
            case XOR:
                return 43;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '<':
                return 49;
            case '=':
                return 50;
            case '>':
                return 51;
            case ')':
                return 191;
        }
        return 389;
    }

    private int yys190() {
        switch (yytok) {
            case AND:
                return 36;
            case ARROW:
                return 37;
            case GEQ:
                return 38;
            case IMPLIES:
                return 39;
            case LEQ:
                return 40;
            case NEQ:
                return 41;
            case OR:
                return 42;
            case XOR:
                return 43;
            case '*':
                return 44;
            case '+':
                return 45;
            case '-':
                return 46;
            case '.':
                return 47;
            case '/':
                return 48;
            case '<':
                return 49;
            case '=':
                return 50;
            case '>':
                return 51;
            case ')':
                return 192;
        }
        return 389;
    }

    private int yys191() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr63();
        }
        return 389;
    }

    private int yys192() {
        switch (yytok) {
            case '=':
            case '<':
            case '/':
            case '-':
            case LEQ:
            case ENDIF:
            case ',':
            case '+':
            case '*':
            case ')':
            case '.':
            case ELSE:
            case XOR:
            case ARROW:
            case IN:
            case THEN:
            case '>':
            case IMPLIES:
            case DOUBLEDOT:
            case GEQ:
            case OR:
            case ENDINPUT:
            case '}':
            case '|':
            case NEQ:
            case AND:
                return yyr62();
        }
        return 389;
    }

    private int yyr1() { // ArlExpression : LetExp
        { yyrv = yysv[yysp-1]; }
        yysv[yysp-=1] = yyrv;
        return yypArlExpression();
    }

    private int yyr2() { // ArlExpression : DefExp
        { yyrv = yysv[yysp-1]; }
        yysv[yysp-=1] = yyrv;
        return yypArlExpression();
    }

    private int yyr3() { // ArlExpression : OperatorExp
        { yyrv = yysv[yysp-1]; }
        yysv[yysp-=1] = yyrv;
        return yypArlExpression();
    }

    private int yypArlExpression() {
        switch (yyst[yysp-1]) {
            case 156: return 174;
            case 154: return 172;
            case 112: return 136;
            case 107: return 132;
            case 106: return 131;
            case 19: return 58;
            case 0: return 1;
            default: return 66;
        }
    }

    private int yyr80() { // ArgumentsExp : OperatorExp
        { yyrv = createArgumentList(null, yysv[yysp-1]); }
        yysv[yysp-=1] = yyrv;
        return yypArgumentsExp();
    }

    private int yyr81() { // ArgumentsExp : ArgumentsExp ',' OperatorExp
        { yyrv = createArgumentList(yysv[yysp-3], yysv[yysp-1]); }
        yysv[yysp-=3] = yyrv;
        return yypArgumentsExp();
    }

    private int yypArgumentsExp() {
        switch (yyst[yysp-1]) {
            case 117: return 144;
            case 52: return 95;
            default: return 147;
        }
    }

    private int yyr7() { // CollectionLiteralExp : CollectionName '{' CollectionLiteralParts '}'
        { yyrv = createCollectionLiteral(yysv[yysp-4], yysv[yysp-2]); }
        yysv[yysp-=4] = yyrv;
        return 2;
    }

    private int yyr8() { // CollectionLiteralExp : CollectionName '{' '}'
        { yyrv = createCollectionLiteral(yysv[yysp-3], null); }
        yysv[yysp-=3] = yyrv;
        return 2;
    }

    private int yyr15() { // CollectionLiteralPart : CollectionRange
        { yyrv = yysv[yysp-1]; }
        yysv[yysp-=1] = yyrv;
        return yypCollectionLiteralPart();
    }

    private int yyr16() { // CollectionLiteralPart : ArlExpression
        { yyrv = yysv[yysp-1]; }
        yysv[yysp-=1] = yyrv;
        return yypCollectionLiteralPart();
    }

    private int yypCollectionLiteralPart() {
        switch (yyst[yysp-1]) {
            case 35: return 67;
            default: return 137;
        }
    }

    private int yyr13() { // CollectionLiteralParts : CollectionLiteralPart
        { yyrv = createCollectionLiteralParts(null, yysv[yysp-1]);  }
        yysv[yysp-=1] = yyrv;
        return 68;
    }

    private int yyr14() { // CollectionLiteralParts : CollectionLiteralParts ',' CollectionLiteralPart
        { yyrv = createCollectionLiteralParts(yysv[yysp-3], yysv[yysp-1]);    }
        yysv[yysp-=3] = yyrv;
        return 68;
    }

    private int yyr9() { // CollectionName : SET
        { yyrv = "Set"; }
        yysv[yysp-=1] = yyrv;
        return yypCollectionName();
    }

    private int yyr10() { // CollectionName : LIST
        { yyrv = "List"; }
        yysv[yysp-=1] = yyrv;
        return yypCollectionName();
    }

    private int yyr11() { // CollectionName : MAP
        { yyrv = "Map"; }
        yysv[yysp-=1] = yyrv;
        return yypCollectionName();
    }

    private int yyr12() { // CollectionName : COLLECTION
        { yyrv = "Collection"; }
        yysv[yysp-=1] = yyrv;
        return yypCollectionName();
    }

    private int yypCollectionName() {
        switch (yyst[yysp-1]) {
            case 165: return 121;
            case 161: return 121;
            case 121: return 121;
            case 118: return 96;
            case 117: return 96;
            case 108: return 121;
            case 103: return 121;
            case 96: return 121;
            case 52: return 96;
            default: return 3;
        }
    }

    private int yyr17() { // CollectionRange : ArlExpression DOUBLEDOT ArlExpression
        { yyrv = createCollectionRange(yysv[yysp-3], yysv[yysp-1]); }
        yysv[yysp-=3] = yyrv;
        return 69;
    }

    private int yyr83() { // DefExp : DEF SimpleName ':' TypeExp '=' ArlExpression
        { yyrv = createDefExpression(yysv[yysp-5], yysv[yysp-3], yysv[yysp-1]); }
        yysv[yysp-=6] = yyrv;
        return 4;
    }

    private int yyr84() { // IfExp : IF ArlExpression THEN ArlExpression ELSE ArlExpression ENDIF
        { yyrv = createIfExp(yysv[yysp-6], yysv[yysp-4], yysv[yysp-2]); }
        yysv[yysp-=7] = yyrv;
        return 5;
    }

    private int yyr79() { // InitVarDeclaration : SimpleName ':' TypeExp '=' OperatorExp
        { yyrv = createInitVariableDeclaration((String)yysv[yysp-5], yysv[yysp-3], yysv[yysp-1]); }
        yysv[yysp-=5] = yyrv;
        switch (yyst[yysp-1]) {
            case 115: return 138;
            case 21: return 59;
            default: return 178;
        }
    }

    private int yyr76() { // IterateName : ITERATE
        { yyrv = "iterate"; }
        yysv[yysp-=1] = yyrv;
        return 72;
    }

    private int yyr71() { // IteratorName : FORALL
        { yyrv = "forAll"; }
        yysv[yysp-=1] = yyrv;
        return 73;
    }

    private int yyr72() { // IteratorName : EXISTS
        { yyrv = "exists"; }
        yysv[yysp-=1] = yyrv;
        return 73;
    }

    private int yyr73() { // IteratorName : COLLECT
        { yyrv = "collect"; }
        yysv[yysp-=1] = yyrv;
        return 73;
    }

    private int yyr74() { // IteratorName : REJECT
        { yyrv = "reject"; }
        yysv[yysp-=1] = yyrv;
        return 73;
    }

    private int yyr75() { // IteratorName : SELECT
        { yyrv = "select"; }
        yysv[yysp-=1] = yyrv;
        return 73;
    }

    private int yyr77() { // IteratorVarDeclaration : SimpleName
        { yyrv = createIteratorVariable((String)yysv[yysp-1], null); }
        yysv[yysp-=1] = yyrv;
        return yypIteratorVarDeclaration();
    }

    private int yyr78() { // IteratorVarDeclaration : SimpleName ':' TypeExp
        { yyrv = createIteratorVariable((String)yysv[yysp-3], yysv[yysp-1]); }
        yysv[yysp-=3] = yyrv;
        return yypIteratorVarDeclaration();
    }

    private int yypIteratorVarDeclaration() {
        switch (yyst[yysp-1]) {
            case 116: return 141;
            case 115: return 139;
            default: return 180;
        }
    }

    private int yyr82() { // LetExp : LET InitVarDeclaration IN ArlExpression
        { openNewEnvironment(); yyrv = createLetExpression(yysv[yysp-3], yysv[yysp-1]); closeCurrentEnvironment(); }
        yysv[yysp-=4] = yyrv;
        return 6;
    }

    private int yyr5() { // LiteralExp : PrimitiveLiteralExp
        { yyrv = yysv[yysp-1]; }
        yysv[yysp-=1] = yyrv;
        return 7;
    }

    private int yyr6() { // LiteralExp : CollectionLiteralExp
        { yyrv = yysv[yysp-1]; }
        yysv[yysp-=1] = yyrv;
        return 7;
    }

    private int yyr28() { // OperatorExp : SimpleName
        { yyrv = createVariableReference(yysv[yysp-1]); }
        yysv[yysp-=1] = yyrv;
        return yypOperatorExp();
    }

    private int yyr29() { // OperatorExp : SimpleName '(' ')'
        { yyrv = createOperationCall(null, yysv[yysp-3], null); }
        yysv[yysp-=3] = yyrv;
        return yypOperatorExp();
    }

    private int yyr30() { // OperatorExp : SimpleName '(' ArgumentsExp ')'
        { yyrv = createOperationCall(null, yysv[yysp-4], yysv[yysp-2]); }
        yysv[yysp-=4] = yyrv;
        return yypOperatorExp();
    }

    private int yyr31() { // OperatorExp : SimpleName '(' TypeExp ')'
        { yyrv = createTypeOperationCall(null, yysv[yysp-4], yysv[yysp-2]); }
        yysv[yysp-=4] = yyrv;
        return yypOperatorExp();
    }

    private int yyr32() { // OperatorExp : LiteralExp
        { yyrv = yysv[yysp-1]; }
        yysv[yysp-=1] = yyrv;
        return yypOperatorExp();
    }

    private int yyr33() { // OperatorExp : IfExp
        { yyrv = yysv[yysp-1]; }
        yysv[yysp-=1] = yyrv;
        return yypOperatorExp();
    }

    private int yyr34() { // OperatorExp : OperatorExp '+' OperatorExp
        { yyrv = createInlineOperationCall(yysv[yysp-3], "+", yysv[yysp-1]); }
        yysv[yysp-=3] = yyrv;
        return yypOperatorExp();
    }

    private int yyr35() { // OperatorExp : OperatorExp '-' OperatorExp
        { yyrv = createInlineOperationCall(yysv[yysp-3], "-", yysv[yysp-1]); }
        yysv[yysp-=3] = yyrv;
        return yypOperatorExp();
    }

    private int yyr36() { // OperatorExp : OperatorExp '*' OperatorExp
        { yyrv = createInlineOperationCall(yysv[yysp-3], "*", yysv[yysp-1]); }
        yysv[yysp-=3] = yyrv;
        return yypOperatorExp();
    }

    private int yyr37() { // OperatorExp : OperatorExp '/' OperatorExp
        { yyrv = createInlineOperationCall(yysv[yysp-3], "/", yysv[yysp-1]); }
        yysv[yysp-=3] = yyrv;
        return yypOperatorExp();
    }

    private int yyr38() { // OperatorExp : OperatorExp IMPLIES OperatorExp
        { yyrv = createInlineOperationCall(yysv[yysp-3], "implies", yysv[yysp-1]); }
        yysv[yysp-=3] = yyrv;
        return yypOperatorExp();
    }

    private int yyr39() { // OperatorExp : OperatorExp XOR OperatorExp
        { yyrv = createInlineOperationCall(yysv[yysp-3], "xor", yysv[yysp-1]); }
        yysv[yysp-=3] = yyrv;
        return yypOperatorExp();
    }

    private int yyr40() { // OperatorExp : OperatorExp OR OperatorExp
        { yyrv = createInlineOperationCall(yysv[yysp-3], "or", yysv[yysp-1]); }
        yysv[yysp-=3] = yyrv;
        return yypOperatorExp();
    }

    private int yyr41() { // OperatorExp : OperatorExp AND OperatorExp
        { yyrv = createInlineOperationCall(yysv[yysp-3], "and", yysv[yysp-1]); }
        yysv[yysp-=3] = yyrv;
        return yypOperatorExp();
    }

    private int yyr42() { // OperatorExp : OperatorExp NEQ OperatorExp
        { yyrv = createInlineOperationCall(yysv[yysp-3], "<>", yysv[yysp-1]); }
        yysv[yysp-=3] = yyrv;
        return yypOperatorExp();
    }

    private int yyr43() { // OperatorExp : OperatorExp '=' OperatorExp
        { yyrv = createInlineOperationCall(yysv[yysp-3], "=", yysv[yysp-1]); }
        yysv[yysp-=3] = yyrv;
        return yypOperatorExp();
    }

    private int yyr44() { // OperatorExp : OperatorExp '<' OperatorExp
        { yyrv = createInlineOperationCall(yysv[yysp-3], "<", yysv[yysp-1]); }
        yysv[yysp-=3] = yyrv;
        return yypOperatorExp();
    }

    private int yyr45() { // OperatorExp : OperatorExp '>' OperatorExp
        { yyrv = createInlineOperationCall(yysv[yysp-3], ">", yysv[yysp-1]); }
        yysv[yysp-=3] = yyrv;
        return yypOperatorExp();
    }

    private int yyr46() { // OperatorExp : OperatorExp LEQ OperatorExp
        { yyrv = createInlineOperationCall(yysv[yysp-3], "<=", yysv[yysp-1]); }
        yysv[yysp-=3] = yyrv;
        return yypOperatorExp();
    }

    private int yyr47() { // OperatorExp : OperatorExp GEQ OperatorExp
        { yyrv = createInlineOperationCall(yysv[yysp-3], ">=", yysv[yysp-1]); }
        yysv[yysp-=3] = yyrv;
        return yypOperatorExp();
    }

    private int yyr48() { // OperatorExp : '(' OperatorExp ')'
        { yyrv = createExpressionInParenthesis(yysv[yysp-2]); }
        yysv[yysp-=3] = yyrv;
        return yypOperatorExp();
    }

    private int yyr49() { // OperatorExp : '-' OperatorExp
        { yyrv = createInlineOperationCall(yysv[yysp-1], "-", null); }
        yysv[yysp-=2] = yyrv;
        return yypOperatorExp();
    }

    private int yyr50() { // OperatorExp : NOT OperatorExp
        { yyrv = createInlineOperationCall(yysv[yysp-1], "not", null); }
        yysv[yysp-=2] = yyrv;
        return yypOperatorExp();
    }

    private int yyr51() { // OperatorExp : TemporalOperatorExp
        yysp -= 1;
        return yypOperatorExp();
    }

    private int yyr52() { // OperatorExp : OperatorExp '.' SimpleName
        { yyrv = createPropertyCall(yysv[yysp-3], yysv[yysp-1]); }
        yysv[yysp-=3] = yyrv;
        return yypOperatorExp();
    }

    private int yyr53() { // OperatorExp : OperatorExp '.' SimpleName
        { yyrv = createPropertyCall(yysv[yysp-3], yysv[yysp-1]); }
        yysv[yysp-=3] = yyrv;
        return yypOperatorExp();
    }

    private int yyr54() { // OperatorExp : OperatorExp '.' SimpleName '(' ')'
        { yyrv = createOperationCall(yysv[yysp-5], yysv[yysp-3], null); }
        yysv[yysp-=5] = yyrv;
        return yypOperatorExp();
    }

    private int yyr55() { // OperatorExp : OperatorExp '.' SimpleName '(' ArgumentsExp ')'
        { yyrv = createOperationCall(yysv[yysp-6], yysv[yysp-4], yysv[yysp-2]); }
        yysv[yysp-=6] = yyrv;
        return yypOperatorExp();
    }

    private int yyr56() { // OperatorExp : OperatorExp '.' SimpleName '(' TypeExp ')'
        { yyrv = createTypeOperationCall(yysv[yysp-6], yysv[yysp-4], yysv[yysp-2]); }
        yysv[yysp-=6] = yyrv;
        return yypOperatorExp();
    }

    private int yyr57() { // OperatorExp : OperatorExp ARROW SimpleName '(' ')'
        { yyrv = createOperationCall(yysv[yysp-5], yysv[yysp-3], null); }
        yysv[yysp-=5] = yyrv;
        return yypOperatorExp();
    }

    private int yyr58() { // OperatorExp : OperatorExp ARROW SimpleName '(' ArgumentsExp ')'
        { yyrv = createOperationCall(yysv[yysp-6], yysv[yysp-4], yysv[yysp-2]); }
        yysv[yysp-=6] = yyrv;
        return yypOperatorExp();
    }

    private int yyr59() { // OperatorExp : OperatorExp ARROW SimpleName '(' TypeExp ')'
        { yyrv = createTypeOperationCall(yysv[yysp-6], yysv[yysp-4], yysv[yysp-2]); }
        yysv[yysp-=6] = yyrv;
        return yypOperatorExp();
    }

    private int yyr60() { // OperatorExp : OperatorExp ARROW IteratorName '(' OperatorExp ')'
        { openNewEnvironment(); yyrv = createIteratorOperationCall(yysv[yysp-6], yysv[yysp-4], null, null, yysv[yysp-2]); closeCurrentEnvironment(); }
        yysv[yysp-=6] = yyrv;
        return yypOperatorExp();
    }

    private int yyr61() { // OperatorExp : OperatorExp ARROW IteratorName '(' IteratorVarDeclaration '|' OperatorExp ')'
        { openNewEnvironment(); 
        yyrv = createIteratorOperationCall(yysv[yysp-8], yysv[yysp-6], yysv[yysp-4],   null, yysv[yysp-2]); 
        closeCurrentEnvironment(); }
        yysv[yysp-=8] = yyrv;
        return yypOperatorExp();
    }

    private int yyr62() { // OperatorExp : OperatorExp ARROW IteratorName '(' IteratorVarDeclaration ',' IteratorVarDeclaration '|' OperatorExp ')'
        { openNewEnvironment(); yyrv = createIteratorOperationCall(yysv[yysp-10], yysv[yysp-8], yysv[yysp-6],   yysv[yysp-4],   yysv[yysp-2]); closeCurrentEnvironment(); }
        yysv[yysp-=10] = yyrv;
        return yypOperatorExp();
    }

    private int yyr63() { // OperatorExp : OperatorExp ARROW IterateName '(' IteratorVarDeclaration ';' InitVarDeclaration '|' OperatorExp ')'
        { openNewEnvironment(); yyrv = createIterateOperationCall(yysv[yysp-10], yysv[yysp-6], yysv[yysp-4], yysv[yysp-2]); closeCurrentEnvironment(); }
        yysv[yysp-=10] = yyrv;
        return yypOperatorExp();
    }

    private int yyr64() { // OperatorExp : OperatorExp ARROW IterateName '(' InitVarDeclaration '|' OperatorExp ')'
        { openNewEnvironment(); yyrv = createIterateOperationCall(yysv[yysp-8], null, yysv[yysp-4], yysv[yysp-2]); closeCurrentEnvironment(); }
        yysv[yysp-=8] = yyrv;
        return yypOperatorExp();
    }

    private int yypOperatorExp() {
        switch (yyst[yysp-1]) {
            case 187: return 190;
            case 186: return 189;
            case 163: return 182;
            case 159: return 177;
            case 157: return 175;
            case 135: return 158;
            case 130: return 155;
            case 127: return 153;
            case 120: return 150;
            case 118: return 97;
            case 117: return 97;
            case 116: return 142;
            case 63: return 110;
            case 61: return 109;
            case 57: return 105;
            case 56: return 104;
            case 54: return 102;
            case 53: return 101;
            case 52: return 97;
            case 51: return 94;
            case 50: return 93;
            case 49: return 92;
            case 48: return 91;
            case 46: return 89;
            case 45: return 88;
            case 44: return 87;
            case 43: return 86;
            case 42: return 85;
            case 41: return 84;
            case 40: return 83;
            case 39: return 82;
            case 38: return 81;
            case 36: return 71;
            case 34: return 65;
            case 33: return 64;
            case 26: return 62;
            default: return 8;
        }
    }

    private int yyr18() { // PrimitiveLiteralExp : INTEGER
        { yyrv = createIntegerLiteral((ArlToken)yysv[yysp-1]); }
        yysv[yysp-=1] = yyrv;
        return 9;
    }

    private int yyr19() { // PrimitiveLiteralExp : REAL
        { yyrv = createRealLiteral((ArlToken)yysv[yysp-1]); }
        yysv[yysp-=1] = yyrv;
        return 9;
    }

    private int yyr20() { // PrimitiveLiteralExp : STRING
        { yyrv = createStringLiteral((ArlToken)yysv[yysp-1]); }
        yysv[yysp-=1] = yyrv;
        return 9;
    }

    private int yyr21() { // PrimitiveLiteralExp : FALSE
        { yyrv = createBooleanLiteral(false); }
        yysv[yysp-=1] = yyrv;
        return 9;
    }

    private int yyr22() { // PrimitiveLiteralExp : TRUE
        { yyrv = createBooleanLiteral(true); }
        yysv[yysp-=1] = yyrv;
        return 9;
    }

    private int yyr23() { // PrimitiveLiteralExp : NULL
        { yyrv = createNullLiteral((ArlToken)yysv[yysp-1]); }
        yysv[yysp-=1] = yyrv;
        return 9;
    }

    private int yyr4() { // SimpleName : NAME
        { yyrv = createSimpleName((ArlToken)yysv[yysp-1]); }
        yysv[yysp-=1] = yyrv;
        switch (yyst[yysp-1]) {
            case 162: return 181;
            case 160: return 60;
            case 151: return 170;
            case 116: return 143;
            case 115: return 140;
            case 100: return 124;
            case 47: return 90;
            case 37: return 74;
            case 21: return 60;
            case 15: return 55;
            default: return 10;
        }
    }

    private int yyr26() { // SimpleTypeExp : SimpleName
        { yyrv = createPathName(null, yysv[yysp-1]); }
        yysv[yysp-=1] = yyrv;
        return 125;
    }

    private int yyr27() { // SimpleTypeExp : SimpleTypeExp '/' SimpleName
        { yyrv = createPathName(yysv[yysp-3], yysv[yysp-1]); }
        yysv[yysp-=3] = yyrv;
        return 125;
    }

    private int yyr65() { // TemporalOperatorExp : NEXT '(' OperatorExp ')'
        { yyrv = createInlineOperationCall(null, "next", yysv[yysp-2]); }
        yysv[yysp-=4] = yyrv;
        return 11;
    }

    private int yyr66() { // TemporalOperatorExp : EVENTUALLY '(' OperatorExp ')'
        { yyrv = createInlineOperationCall(null, "eventually", yysv[yysp-2]); }
        yysv[yysp-=4] = yyrv;
        return 11;
    }

    private int yyr67() { // TemporalOperatorExp : ALWAYS '(' OperatorExp ')'
        { yyrv = createInlineOperationCall(null, "always", yysv[yysp-2]); }
        yysv[yysp-=4] = yyrv;
        return 11;
    }

    private int yyr68() { // TemporalOperatorExp : UNTIL '(' OperatorExp ',' OperatorExp ')'
        { yyrv = createInlineOperationCall(yysv[yysp-4], "until", yysv[yysp-2]); }
        yysv[yysp-=6] = yyrv;
        return 11;
    }

    private int yyr69() { // TemporalOperatorExp : ASSOONAS '(' OperatorExp ',' OperatorExp ')'
        { yyrv = createInlineOperationCall(yysv[yysp-4], "asSoonAs", yysv[yysp-2]); }
        yysv[yysp-=6] = yyrv;
        return 11;
    }

    private int yyr70() { // TemporalOperatorExp : EVERYTIME '(' OperatorExp ',' OperatorExp ')'
        { yyrv = createInlineOperationCall(yysv[yysp-4], "everytime", yysv[yysp-2]); }
        yysv[yysp-=6] = yyrv;
        return 11;
    }

    private int yyr24() { // TypeExp : '<' SimpleTypeExp '>'
        { yyrv = createType(yysv[yysp-2]); }
        yysv[yysp-=3] = yyrv;
        return yypTypeExp();
    }

    private int yyr25() { // TypeExp : CollectionName TypeExp
        { yyrv = createCollectionType(yysv[yysp-2], yysv[yysp-1]); }
        yysv[yysp-=2] = yyrv;
        return yypTypeExp();
    }

    private int yypTypeExp() {
        switch (yyst[yysp-1]) {
            case 165: return 183;
            case 161: return 179;
            case 118: return 148;
            case 117: return 145;
            case 108: return 133;
            case 103: return 128;
            case 52: return 98;
            default: return 122;
        }
    }

    protected String[] yyerrmsgs = {
    };

// -----------------------------------------------------------
    // ------------------- hand-written code ---------------------
    // -----------------------------------------------------------

    private ArlScanner scanner;
    private ArlToken token;
    private Object currentNode;
    public Environment currentEnvironment;
    private int tempIndex = 1;

    //main

    public Object parse(String arlExpression, ArlType contextType, ArrayList<Parameter<?>> parameters) {
        currentEnvironment = null;
        tempIndex = 1;
        scanner = new ArlScanner(new ByteArrayInputStream(arlExpression.getBytes()));

        Object contextVariable = createContextDeclaration(new TypeExpression(contextType));

        openNewEnvironment();
        currentEnvironment.addVariable("self", contextVariable);
        currentEnvironment.setImplicitVariable(contextVariable);

        if (parameters != null) {
            for (Parameter<?> parameter : parameters) {
                Object type = parameter.getElementType();
                String name = parameter.getName();
                Object parameterVariable = createVariableDeclaration(name, type, null);
                addVariable(name, parameterVariable);
            }
        }

        parse();

        if (currentEnvironment.getParent() != null)
            throw new ParsingException("more than one parsing environment left (all parentheses closed?)");
        Object r = yysv[yysp - 1];
        return r;
    }

    public int getLine() {
        return scanner.getLine();
    }

    public int getColumn() {
        return scanner.getColumn();
    }


    //parsing helper

    private int nextToken() {
        try {
            token = scanner.yylex();
            currentNode = createTokenNode(token);
            return token.kind;
        } catch (IOException e) {
            yyerror(e.getMessage());
            return 0;
        }
    }

    private Object createTokenNode(ArlToken t) {
        return t;
    }

    private int getTokenKind() {
        if (token == null) return nextToken();
        return token.kind;
    }

    private void yyerror(String msg, Object... arguments) {
        for (Object o : arguments) msg = msg.replaceFirst("%s", o.toString());
        throw new ParsingException(msg, scanner.getLine(), scanner.getColumn());
    }


    //literals

    private Object createNullLiteral(ArlToken token) {
        return new LiteralExpression(null);
    }

    private Object createBooleanLiteral(boolean value) {
        return new LiteralExpression(value);
    }

    private Object createIntegerLiteral(ArlToken token) {
        return new LiteralExpression((long) token.longValue);
    }

    private Object createRealLiteral(ArlToken token) {
        return new LiteralExpression(token.doubleValue);
    }

    private Object createStringLiteral(ArlToken token) {
        return new LiteralExpression(token.stringValue);
    }

    private Object createCollectionLiteral(Object name, Object parts) {
        List<Object> partList = convertToList(parts);
        String stringName = (String) name;
        if (partList.size() == 1 && partList.get(0) instanceof CollectionRangeExpression) {
            return partList.get(0);
        }

        if (stringName.equalsIgnoreCase(ArlType.SET.collection.name())) {
            Set<Expression<Object>> mySet = new HashSet<Expression<Object>>();
            mySet.addAll((Collection<? extends Expression<Object>>) parts);
            return new SetLiteralExpression(mySet);
        } else if (stringName.equalsIgnoreCase(ArlType.LIST.collection.name()))
            return new ListLiteralExpression((List<Expression<Object>>) (Object) parts);
        else
            throw new ParsingException("collection literal cannot be '%s'", name);
    }

    private Object createCollectionLiteralParts(Object parts, Object newPart) {
        return addToList((List<Object>) parts, newPart);
    }

    private Object createCollectionRange(Object from, Object to) {
        return new CollectionRangeExpression((Expression<Long>) from, (Expression<Long>) to);
    }


    //types

    private Object createType(Object typeName) {
        ArlType type = ArlType.get((List<String>) typeName, null);
        if (type == null) throw new ParsingException("unknown type '%s'", toPathNameString((List<String>) typeName));
        return new TypeExpression(type);
    }

    private Object createCollectionType(Object collectionName, Object element) {
        ArlType type = ArlType.get(null, (String) collectionName);
        type = ArlType.get(((TypeExpression) element).value.type, type.collection, ((TypeExpression) element).value.nativeType);
        return new TypeExpression(type);
    }

    private Object createTupleType(Object variable) {
        return null;
    }


    //variables

    private Object createVariableDeclaration(Object name, Object type, Object initValue) {
        return new VariableExpression((String) name, (TypeExpression) type, (Expression<Object>) initValue);
    }

    public Object createContextDeclaration(TypeExpression context) {
        return createVariableDeclaration("self", context, null);
    }

    private Object createImplicitVariable(Object type) {
        String name = "temp" + tempIndex++;
        Object implicitVariable = createVariableDeclaration(name, type, null);
        currentEnvironment.setImplicitVariable(implicitVariable);
        return implicitVariable;
    }

    private void addVariable(String name, Object variableDeclaration) {
        if (currentEnvironment.lookupLocal(name) != null)
            throw new ParsingException("variable '%s' already declared", name);
        currentEnvironment.addVariable(name, variableDeclaration);
    }

    private Object createVariableReference(Object name) {
        Object variable = currentEnvironment.lookup((String) name);
        if (variable == null) throw new ParsingException("variable '%s' is unknown", name);
        return variable;
    }

    private Object createArgumentList(Object arguments, Object newArgument) {
        return addToList((List<Object>) arguments, newArgument);
    }


    //if

    private Object createIfExp(Object condition, Object then_, Object else_) {
        return new IfExpression((Expression<Boolean>) condition, (Expression) then_, (Expression) else_);
    }


    //let

    private Object createLetExpression(Object variable, Object body) {
        return new LetExpression((VariableExpression<?>) variable, (Expression<Object>) body);
    }


    //def
    private Object createDefExpression(Object variable, Object type, Object body) {
        //return new Expression((VariableExpression<?>) variable, (Expression<Object>) body);
        return null;
        //def allChildren:Set<Person>=self.children->collect(child | child.children)->including(self.children)
        //def allParents:Set<Person>=self.parent.allParents->including(self.parent)
    }


    //name

    private Object createSimpleName(ArlToken token) {
        return token.stringValue;
    }

    private List<String> createPathName(Object path, Object string) {
        if (path instanceof String)
            return addToList((List<String>) path, (String) string);
        else
            return addToList((List<String>) path, (String) string);
    }


    //propertycall

    private Object createPropertyCall(Object source, Object property) {
        Expression propertyCall = new PropertyCallExpression((Expression) source, (String) property);
        currentEnvironment.setElementType(propertyCall.getResultType());
        return propertyCall;
    }


    //operation call

    private Object createInlineOperationCall(Object source, String operation, Object argument) {
        switch (operation) {
            case "+":
                return new AddExpression<Number>((Expression<Number>) source, (Expression<Number>) argument);
            case "-":
                if (argument == null)
                    return new MulExpression(new LiteralExpression(-1), (Expression) source);
                else
                    return new SubExpression((Expression) source, (Expression) argument);
            case "*":
                return new MulExpression((Expression) source, (Expression) argument);
            case "/":
                return new DivExpression((Expression) source, (Expression) argument);

            case "=":
                return new EqualsExpression((Expression) source, (Expression) argument);
            case "<>":
                return new NotExpression(new EqualsExpression((Expression) source, (Expression) argument));
            case ">":
                return new GTExpression((Expression) source, (Expression) argument);
            case "<":
                return new LTExpression((Expression) source, (Expression) argument);
            case ">=":
                return new GTEExpression((Expression) source, (Expression) argument);
            case "<=":
                return new LTEExpression((Expression) source, (Expression) argument);

            case "and":
                return new AndExpression((Expression<Boolean>) source, (Expression<Boolean>) argument);
            case "or":
                return new OrExpression((Expression<Boolean>) source, (Expression<Boolean>) argument);
            case "xor":
                return new XorExpression((Expression<Boolean>) source, (Expression<Boolean>) argument);
            case "implies":
                return new ImpliesExpression((Expression<Boolean>) source, (Expression<Boolean>) argument);
            case "not":
                return new NotExpression((Expression<Boolean>) source);
            case "next":
                System.out.println("NEXT " + source + " " + argument);
                return new NextExpression((Expression<Boolean>) argument);
            case "always":
                System.out.println("ALWAYS " + source + " " + argument);
                return new AlwaysExpression((Expression<Boolean>) argument);
            case "eventually":
                System.out.println("EVENTUALLY " + source + " " + argument);
                return new EventuallyExpression((Expression<Boolean>) argument);
            case "until":
                System.out.println("UNTIL " + source + " " + argument);
                return new UntilExpression((Expression<Boolean>) source,(Expression<Boolean>) argument);
            case "asSoonAs":
                System.out.println("ASSOONAS " + source + " " + argument);
                return new AsSoonAsExpression((Expression<Boolean>) source,(Expression<Boolean>) argument);
            case "everytime":
                System.out.println("EVERYTIME " + source + " " + argument);
                return new EverytimeExpression((Expression<Boolean>) source,(Expression<Boolean>) argument);
            default:
                throw new ParsingException("inline operation '%s' does not exist", operation);
        }
    }

    private Object createTypeOperationCall(Object source, Object operation, Object argument) {
        switch ((String) operation) {
            case "asType":
                return new AsTypeExpression((Expression) source, (Expression) argument);
            case "isTypeOf":
                return new IsTypeOfExpression((Expression) source, (Expression) argument);
            case "isKindOf":
                return new IsKindOfExpression((Expression) source, (Expression) argument);
            case "allInstances":
                return new AllInstancesExpression((Expression) argument);
            default:
                throw new ParsingException("type operation '%s' does not exist", operation);
        }
    }

    private Object createIteratorVariable(String name, Object type) {
        if (type == null) type = new TypeExpression(((ArlType) currentEnvironment.getElementType()).toSingle());
        Object variableDeclaration = createVariableDeclaration(name, type, null);
        addVariable(name, variableDeclaration);
        return variableDeclaration;
    }

    private Object createIteratorOperationCall(Object source, Object operation, Object iterator1, Object iterator2, Object body) {
        Object type = ((Expression) source).getResultType();

        currentEnvironment.setElementType(type);
        if (iterator1 == null)
            iterator1 = createImplicitVariable(ArlType.get(((ArlType) type).type, ArlType.CollectionKind.SINGLE, ((ArlType) type).nativeType));

        switch ((String) operation) {
            case "forAll":
                return new ForAllExpression((Expression) source, (VariableExpression<?>) iterator1, (VariableExpression<?>) iterator2, (Expression) body);
            case "exists":
                return new ExistsExpression((Expression) source, (VariableExpression<?>) iterator1, (VariableExpression<?>) iterator2, (Expression) body);
            case "select":
            	// here we need to reset the parent currentEvnrionment element type to the previous expression from before the select/reject (as this current environment is closed after this call)
            	this.currentEnvironment.getParent().setElementType(type);
                return new SelectExpression((Expression) source, (VariableExpression<?>) iterator1, (VariableExpression<?>) iterator2, (Expression<Boolean>) body);
            case "reject":
            	// here we need to reset the parent currentEvnrionment element type to the previous expression from before the select/reject (as this current environment is closed after this call)
            	this.currentEnvironment.getParent().setElementType(type);
            	return new RejectExpression((Expression) source, (VariableExpression<?>) iterator1, (VariableExpression<?>) iterator2, (Expression<Boolean>) body);
            case "collect":
                return new CollectExpression((Expression) source, (VariableExpression<?>) iterator1, (VariableExpression<?>) iterator2, (Expression) body);
            default:
                throw new ParsingException("iterator operation '%s' does not exist", operation);
        }
    }

    private Object createInitVariableDeclaration(String name, Object type, Object initValue) {
        if (type == null) type = currentEnvironment.getElementType();
        Object variableDeclaration = createVariableDeclaration(name, type, initValue);
        addVariable(name, variableDeclaration);
        return variableDeclaration;
    }

    private Object createIterateOperationCall(Object source, Object iterator, Object accumulator, Object body) {
        Object type = ((Expression) source).getResultType();

        currentEnvironment.setElementType(type);
        if (iterator == null)
            iterator = createImplicitVariable(ArlType.get(((ArlType) type).type, ArlType.CollectionKind.SINGLE, ((ArlType) type).nativeType));

        return new IterateExpression((Expression) source, (VariableExpression<?>) iterator, (VariableExpression<?>) accumulator, (Expression) body);
    }

    private Object createOperationCall(Object source, Object operation, Object arguments) {
        List<Object> argumentList = convertToList(arguments);
        return new OperationCallExpression((Expression) source, (String) operation, argumentList);
    }

    private Object createExpressionInParenthesis(Object object) {
        return object;
    }


    //list handlers

    private <T> List<T> addToList(List<T> list, T item) {
        if (list == null) {
            list = new ArrayList<T>();
        }
        list.add(item);
        return list;
    }

    private List<Object> convertToList(Object listObject) {
        if (listObject != null) {
            return (List<Object>) listObject;
        } else {
            return Collections.emptyList();
        }
    }

    private String toPathNameString(List<String> pathName) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pathName.size(); i++) {
            sb.append(pathName.get(i));
            if (i < pathName.size() - 1) {
                sb.append(":");
            }
        }
        return sb.toString();
    }


    //environment

    private void openNewEnvironment() {
        currentEnvironment = new Environment(currentEnvironment);
    }

    private void closeCurrentEnvironment() {
        currentEnvironment = currentEnvironment.getParent();
    }
}

