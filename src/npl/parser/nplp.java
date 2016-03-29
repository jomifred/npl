/* Generated By:JavaCC: Do not edit this line. nplp.java */
                   // NPL parser

  package npl.parser;

  import npl.*;
  import jason.asSyntax.*;
  import jason.asSyntax.ArithExpr.ArithmeticOp;
  import jason.asSyntax.LogExpr.LogicalOp;
  import jason.asSyntax.RelExpr.RelationalOp;
  import jason.asSemantics.*;

  import java.util.*;

  public class nplp implements nplpConstants {
    private String npSource = null;
    private DynamicFactsProvider dfp;
    private static LiteralFactory lFac = NPLLiteral.getFactory();
    private static NormFactory    nFac = Norm.getFactory();

    public static void setLiteralFactory(LiteralFactory l) {
        lFac = l;
    }
    public static void setNormFactory(NormFactory l) {
        nFac = l;
    }

    private String getSourceRef(SourceInfo s) {
        if (s == null)
            return "[]";
        else
            return "["+s.getSrcFile()+":"+s.getBeginSrcLine()+"]";
    }
    private String getSourceRef(DefaultTerm t) {
        return getSourceRef( t.getSrcInfo());
    }
    private String getSourceRef(Object t) {
        if (t instanceof DefaultTerm)
            return getSourceRef((DefaultTerm)t);
        else if (t instanceof SourceInfo)
            return getSourceRef((SourceInfo)t);
        else
            return "[]";
    }
    private Term changeToAtom(Object o) {
        Term u = (Term)o;
        if (u.isAtom()) {
           return new Atom((Literal)u);
        } else {
           return u;
        }
    }

/* NLP Grammar */
  final public void program(NormativeProgram np, DynamicFactsProvider dfp) throws ParseException {
  Token k;
                                  this.dfp  = dfp;
    scope(np, null);
    jj_consume_token(0);
  }

  final public void scope(NormativeProgram np, Scope superScope) throws ParseException {
  Literal scopeId; Rule r; Norm n; Scope scope;
    jj_consume_token(SCOPE);
    scopeId = literal();
                                scope = new Scope(scopeId, np);
                                if (superScope == null) { // it is root
                                  np.setRoot(scope);
                                } else {
                                  superScope.addScope(scope);
                                  scope.setFather(superScope);
                                }
    jj_consume_token(27);
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case SCOPE:
      case NORM:
      case FAIL:
      case OBLIGATION:
      case ATOM:
        ;
        break;
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NORM:
        n = norm();
        jj_consume_token(28);
                                scope.addNorm(n);
        break;
      case FAIL:
      case OBLIGATION:
      case ATOM:
        r = rule();
        jj_consume_token(28);
                                scope.addRule(r);
        break;
      case SCOPE:
        scope(np,scope);
        break;
      default:
        jj_la1[1] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    jj_consume_token(29);
  }

  final public Rule rule() throws ParseException {
  Literal h; Object b = Literal.LTrue;
    h = literal();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 30:
      jj_consume_token(30);
      b = log_expr();
      break;
    default:
      jj_la1[2] = jj_gen;
      ;
    }
                                {if (true) return new Rule(h,(LogicalFormula)b);}
    throw new Error("Missing return statement in function");
  }

  final public Norm norm() throws ParseException {
  Literal h; Object b; Token id;
    jj_consume_token(NORM);
    id = jj_consume_token(ATOM);
    jj_consume_token(31);
    b = log_expr();
    jj_consume_token(32);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case FAIL:
      h = fail();
      break;
    case OBLIGATION:
      h = obligation();
      break;
    default:
      jj_la1[3] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
                         {if (true) return nFac.createNorm(id.image,h,(LogicalFormula)b);}
    throw new Error("Missing return statement in function");
  }

  final public Literal fail() throws ParseException {
  Token k; Term t = null; Literal r;
    k = jj_consume_token(FAIL);
    jj_consume_token(33);
    t = term();
    jj_consume_token(34);
                         r = ASSyntax.createLiteral(NormativeProgram.FailFunctor, t);
                         r.setSrcInfo(new SourceInfo(npSource, k.beginLine));
                         if (dfp != null && dfp.isRelevant(r.getPredicateIndicator())) {
                            {if (true) return lFac.createNPLLiteral(r,dfp);}
                         } else {
                            {if (true) return r;}
                         }
    throw new Error("Missing return statement in function");
  }

  final public Literal obligation() throws ParseException {
  Token k; Term a, m, g, d = null; Literal r; Object o;
    k = jj_consume_token(OBLIGATION);
                         r = ASSyntax.createLiteral(NormativeProgram.OblFunctor);
                         r.setSrcInfo(new SourceInfo(npSource, k.beginLine));
    jj_consume_token(33);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ATOM:
      k = jj_consume_token(ATOM);
                         r.addTerm(new Atom(k.image));
      break;
    case VAR:
      k = jj_consume_token(VAR);
                         r.addTerm(new VarTerm(k.image));
      break;
    default:
      jj_la1[4] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    jj_consume_token(35);
    o = log_expr();
    jj_consume_token(35);
                         r.addTerm( changeToAtom(o) );
    o = log_expr();
    jj_consume_token(35);
                         r.addTerm( changeToAtom(o) );
    o = log_expr();
                         r.addTerm( changeToAtom(o) );
    jj_consume_token(34);
                         if (dfp != null && dfp.isRelevant(r.getPredicateIndicator())) {
                            {if (true) return lFac.createNPLLiteral(r,dfp);}
                         } else {
                            {if (true) return r;}
                         }
    throw new Error("Missing return statement in function");
  }

/* what follows is mostly from Jason */


/* Literal */
  final public Literal literal() throws ParseException {
  Token k; List l = null; Literal r;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ATOM:
      k = jj_consume_token(ATOM);
      break;
    case FAIL:
      k = jj_consume_token(FAIL);
      break;
    case OBLIGATION:
      k = jj_consume_token(OBLIGATION);
      break;
    default:
      jj_la1[5] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
                         r = ASSyntax.createLiteral(k.image);
                         r.setSrcInfo(new SourceInfo(npSource, k.beginLine));
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 33:
      jj_consume_token(33);
      l = terms();
                         r.setTerms(l);
      jj_consume_token(34);
      break;
    default:
      jj_la1[6] = jj_gen;
      ;
    }
                         if (r.getFunctor().indexOf(".") >= 0) {
                            try {
                               {if (true) return new InternalActionLiteral((Structure)r, (Agent)null);}
                            } catch (Exception e) {
                               e.printStackTrace();
                            }
                         } else if (dfp != null && dfp.isRelevant(r.getPredicateIndicator())) {
                            {if (true) return lFac.createNPLLiteral(r,dfp);}
                         } else {
                            {if (true) return r;}
                         }
    throw new Error("Missing return statement in function");
  }

/* List of terms */
  final public List terms() throws ParseException {
                    ArrayList listTerms = new ArrayList(); Term v;
    v = term();
                         listTerms.add(v);
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 35:
        ;
        break;
      default:
        jj_la1[7] = jj_gen;
        break label_2;
      }
      jj_consume_token(35);
      v = term();
                         listTerms.add(v);
    }
                         listTerms.trimToSize();
                         {if (true) return listTerms;}
    throw new Error("Missing return statement in function");
  }

  final public Term term() throws ParseException {
                         Object o;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 36:
      o = list();
      break;
    case VAR:
    case TK_NOT:
    case NUMBER:
    case STRING:
    case FAIL:
    case OBLIGATION:
    case ATOM:
    case UNNAMEDVAR:
    case 33:
    case 49:
    case 53:
      o = log_expr();
      break;
    default:
      jj_la1[8] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
                         {if (true) return changeToAtom(o);}
    throw new Error("Missing return statement in function");
  }

  final public ListTermImpl list() throws ParseException {
                            ListTermImpl lt = new ListTermImpl(); ListTerm last; Token K; Term f;
    jj_consume_token(36);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case VAR:
    case NUMBER:
    case STRING:
    case FAIL:
    case OBLIGATION:
    case ATOM:
    case UNNAMEDVAR:
    case 33:
    case 36:
    case 49:
    case 53:
      f = term_in_list();
                            last = lt.append(f); lt.setSrcInfo(f.getSrcInfo());
      label_3:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case 35:
          ;
          break;
        default:
          jj_la1[9] = jj_gen;
          break label_3;
        }
        jj_consume_token(35);
        f = term_in_list();
                            last = last.append(f);
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 37:
        jj_consume_token(37);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case VAR:
          K = jj_consume_token(VAR);
                            last.setNext(new VarTerm(K.image));
          break;
        case UNNAMEDVAR:
          K = jj_consume_token(UNNAMEDVAR);
                            last.setNext(UnnamedVar.create(K.image));
          break;
        case 36:
          f = list();
                            last = last.concat((ListTerm)f);
          break;
        default:
          jj_la1[10] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
        break;
      default:
        jj_la1[11] = jj_gen;
        ;
      }
      break;
    default:
      jj_la1[12] = jj_gen;
      ;
    }
    jj_consume_token(38);
                            {if (true) return lt;}
    throw new Error("Missing return statement in function");
  }

// term_in_list is the same as term, but log_expr/plan_body must be enclosed by "("....")" to avoid problem with |
  final public Term term_in_list() throws ParseException {
                            Object o;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 36:
      o = list();
      break;
    case VAR:
    case NUMBER:
    case FAIL:
    case OBLIGATION:
    case ATOM:
    case UNNAMEDVAR:
    case 33:
    case 49:
    case 53:
      o = arithm_expr();
      break;
    case STRING:
      o = string();
      break;
    default:
      jj_la1[13] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
                            {if (true) return changeToAtom(o);}
    throw new Error("Missing return statement in function");
  }

/* logical expression */
  final public Object log_expr() throws ParseException {
                              Object t1, t2;
    t1 = log_expr_trm();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 37:
      jj_consume_token(37);
      t2 = log_expr();
                              {if (true) return new LogExpr((LogicalFormula)t1,LogicalOp.or,(LogicalFormula)t2);}
      break;
    default:
      jj_la1[14] = jj_gen;
      ;
    }
                              {if (true) return t1;}
    throw new Error("Missing return statement in function");
  }

  final public Object log_expr_trm() throws ParseException {
                              Object t1, t2;
    t1 = log_expr_factor();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 39:
      jj_consume_token(39);
      t2 = log_expr_trm();
                              {if (true) return new LogExpr((LogicalFormula)t1,LogicalOp.and,(LogicalFormula)t2);}
      break;
    default:
      jj_la1[15] = jj_gen;
      ;
    }
                              {if (true) return t1;}
    throw new Error("Missing return statement in function");
  }

  final public Object log_expr_factor() throws ParseException {
                                      Object t;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case TK_NOT:
      jj_consume_token(TK_NOT);
      t = log_expr_factor();
                                      {if (true) return new LogExpr(LogicalOp.not,(LogicalFormula)t);}
      break;
    case VAR:
    case NUMBER:
    case STRING:
    case FAIL:
    case OBLIGATION:
    case ATOM:
    case UNNAMEDVAR:
    case 33:
    case 49:
    case 53:
      t = rel_expr();
                                      {if (true) return t;}
      break;
    default:
      jj_la1[16] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/* relational expression 
   used in context, body and term
   
     <VAR>      [ <OPREL> <EXP> ]  --> this method returns the VarTerm
   | <LITERAL>  [ <OPREL> <EXP> ]  --> returns the Literal
   | <EXP>      [ <OPREL> <EXP> ]  --> returns the ExprTerm 
*/
  final public Object rel_expr() throws ParseException {
                                             Object op1 = null;
                                             Object op2 = null;
                                             RelationalOp operator = RelationalOp.none;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case VAR:
    case NUMBER:
    case FAIL:
    case OBLIGATION:
    case ATOM:
    case UNNAMEDVAR:
    case 33:
    case 49:
    case 53:
      op1 = arithm_expr();
      break;
    case STRING:
      op1 = string();
      break;
    default:
      jj_la1[17] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 40:
    case 41:
    case 42:
    case 43:
    case 44:
    case 45:
    case 46:
    case 47:
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 40:
        jj_consume_token(40);
                                             operator = RelationalOp.lt;
        break;
      case 41:
        jj_consume_token(41);
                                             operator = RelationalOp.lte;
        break;
      case 42:
        jj_consume_token(42);
                                             operator = RelationalOp.gt;
        break;
      case 43:
        jj_consume_token(43);
                                             operator = RelationalOp.gte;
        break;
      case 44:
        jj_consume_token(44);
                                             operator = RelationalOp.eq;
        break;
      case 45:
        jj_consume_token(45);
                                             operator = RelationalOp.dif;
        break;
      case 46:
        jj_consume_token(46);
                                             operator = RelationalOp.unify;
        break;
      case 47:
        jj_consume_token(47);
                                             operator = RelationalOp.literalBuilder;
        break;
      default:
        jj_la1[18] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case VAR:
      case NUMBER:
      case FAIL:
      case OBLIGATION:
      case ATOM:
      case UNNAMEDVAR:
      case 33:
      case 49:
      case 53:
        op2 = arithm_expr();
        break;
      case STRING:
        op2 = string();
        break;
      case 36:
        op2 = list();
        break;
      default:
        jj_la1[19] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
                                             if ( ((Term)op1).isInternalAction() && operator != RelationalOp.literalBuilder)
                                                {if (true) throw new ParseException(getSourceRef(op1)+" RelExpr: operand '"+op1+"' can not be an internal action.");}
                                             if ( ((Term)op2).isInternalAction() && operator != RelationalOp.literalBuilder)
                                                {if (true) throw new ParseException(getSourceRef(op2)+" RelExpr: operand '"+op2+"' can not be an internal action.");}
                                             {if (true) return new RelExpr((Term)op1, operator, (Term)op2);}
      break;
    default:
      jj_la1[20] = jj_gen;
      ;
    }
                                             {if (true) return op1;}
    throw new Error("Missing return statement in function");
  }

/* arithmetic expression */
  final public Object arithm_expr() throws ParseException {
                                Object t1, t2; ArithmeticOp op;
    t1 = arithm_expr_trm();
                                op = ArithmeticOp.none;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 48:
    case 49:
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 48:
        jj_consume_token(48);
                                op = ArithmeticOp.plus;
        break;
      case 49:
        jj_consume_token(49);
                                op = ArithmeticOp.minus;
        break;
      default:
        jj_la1[21] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      t2 = arithm_expr();
                                if (!(t1 instanceof NumberTerm)) {
                                   {if (true) throw new ParseException(getSourceRef(t1)+" ArithExpr: first operand '"+t1+"' is not numeric or variable.");}
                                }
                                if (!(t2 instanceof NumberTerm)) {
                                   {if (true) throw new ParseException(getSourceRef(t2)+" ArithExpr: second operand '"+t2+"' is not numeric or variable.");}
                                }
                                {if (true) return new ArithExpr((NumberTerm)t1, op, (NumberTerm)t2);}
      break;
    default:
      jj_la1[22] = jj_gen;
      ;
    }
                                {if (true) return t1;}
    throw new Error("Missing return statement in function");
  }

  final public Object arithm_expr_trm() throws ParseException {
                                  Object t1, t2; ArithmeticOp op;
    t1 = arithm_expr_factor();
                                  op = ArithmeticOp.none;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case TK_INTDIV:
    case TK_INTMOD:
    case 50:
    case 51:
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 50:
        jj_consume_token(50);
                                  op = ArithmeticOp.times;
        break;
      case 51:
        jj_consume_token(51);
                                  op = ArithmeticOp.div;
        break;
      case TK_INTDIV:
        jj_consume_token(TK_INTDIV);
                                  op = ArithmeticOp.intdiv;
        break;
      case TK_INTMOD:
        jj_consume_token(TK_INTMOD);
                                  op = ArithmeticOp.mod;
        break;
      default:
        jj_la1[23] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      t2 = arithm_expr_trm();
                                  if (!(t1 instanceof NumberTerm)) {
                                    {if (true) throw new ParseException(getSourceRef(t1)+" ArithTerm: first operand '"+t1+"' is not numeric or variable.");}
                                  }
                                  if (!(t2 instanceof NumberTerm)) {
                                    {if (true) throw new ParseException(getSourceRef(t2)+" ArithTerm: second operand '"+t2+"' is not numeric or variable.");}
                                  }
                                  {if (true) return new ArithExpr((NumberTerm)t1, op, (NumberTerm)t2);}
      break;
    default:
      jj_la1[24] = jj_gen;
      ;
    }
                                  {if (true) return t1;}
    throw new Error("Missing return statement in function");
  }

  final public Object arithm_expr_factor() throws ParseException {
                                  Object t1, t2; ArithmeticOp op;
    t1 = arithm_expr_simple();
                                  op = ArithmeticOp.none;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 52:
      jj_consume_token(52);
                                  op = ArithmeticOp.pow;
      t2 = arithm_expr_factor();
                                  if (!(t1 instanceof NumberTerm)) {
                                    {if (true) throw new ParseException(getSourceRef(t1)+" ArithFactor: first operand '"+t1+"' is not numeric or variable.");}
                                  }
                                  if (!(t2 instanceof NumberTerm)) {
                                    {if (true) throw new ParseException(getSourceRef(t2)+" ArithFactor: second operand '"+t2+"' is not numeric or variable.");}
                                  }
                                  {if (true) return new ArithExpr((NumberTerm)t1, op, (NumberTerm)t2);}
      break;
    default:
      jj_la1[25] = jj_gen;
      ;
    }
                                  {if (true) return t1;}
    throw new Error("Missing return statement in function");
  }

  final public Object arithm_expr_simple() throws ParseException {
                                  Token K; Object t; VarTerm v;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NUMBER:
      K = jj_consume_token(NUMBER);
                                  NumberTerm ni = ASSyntax.parseNumber(K.image);
                                  ni.setSrcInfo(new SourceInfo(npSource, K.beginLine));
                                  {if (true) return ni;}
      break;
    case 49:
      jj_consume_token(49);
      t = arithm_expr_simple();
                                  if (!(t instanceof NumberTerm)) {
                                    {if (true) throw new ParseException(getSourceRef(t)+" The argument '"+t+"' of operator '-' is not numeric or variable.");}
                                  }
                                  {if (true) return new ArithExpr(ArithmeticOp.minus, (NumberTerm)t);}
      break;
    case 33:
      jj_consume_token(33);
      t = log_expr();
      jj_consume_token(34);
                                  {if (true) return t;}
      break;
    case VAR:
    case UNNAMEDVAR:
      v = var();
                                  {if (true) return v;}
      break;
    case FAIL:
    case OBLIGATION:
    case ATOM:
      t = literal();
                                  {if (true) return t;}
      break;
    case 53:
      t = time();
                                  {if (true) return t;}
      break;
    default:
      jj_la1[26] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public VarTerm var() throws ParseException {
                      Token K; VarTerm v; ListTerm lt;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case VAR:
      K = jj_consume_token(VAR);
                      v = new VarTerm(K.image); v.setSrcInfo(new SourceInfo(npSource, K.beginLine));
      break;
    case UNNAMEDVAR:
      K = jj_consume_token(UNNAMEDVAR);
                      v = UnnamedVar.create(K.image);
      break;
    default:
      jj_la1[27] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 36:
      lt = list();
                      v.setAnnots(lt);
      break;
    default:
      jj_la1[28] = jj_gen;
      ;
    }
                      {if (true) return v;}
    throw new Error("Missing return statement in function");
  }

  final public StringTerm string() throws ParseException {
                      Token k; StringTermImpl s;
    k = jj_consume_token(STRING);
                      s = new StringTermImpl(k.image.substring(1,k.image.length()-1));
                      s.setSrcInfo(new SourceInfo(npSource,k.beginLine));
                      {if (true) return s;}
    throw new Error("Missing return statement in function");
  }

  final public Term time() throws ParseException {
                      Token k; long t = -1; String u = null;
    jj_consume_token(53);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NUMBER:
      k = jj_consume_token(NUMBER);
                      t = Long.parseLong(k.image);
      break;
    default:
      jj_la1[29] = jj_gen;
      ;
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ATOM:
      k = jj_consume_token(ATOM);
                      u = k.image;
      break;
    default:
      jj_la1[30] = jj_gen;
      ;
    }
    jj_consume_token(53);
                      //if (u == null && t != 0)
                      //   throw new ParseException("no time unit can be used only with 0");
                      if (t == -1 && !u.equals("now") && !u.equals("never"))
                         {if (true) throw new ParseException("only 'now' and 'never' cannot have a number after @");}
                      {if (true) return new TimeTerm(t,u);}
    throw new Error("Missing return statement in function");
  }

  public nplpTokenManager token_source;
  SimpleCharStream jj_input_stream;
  public Token token, jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[31];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static {
      jj_la1_0();
      jj_la1_1();
   }
   private static void jj_la1_0() {
      jj_la1_0 = new int[] {0x1f0000,0x1f0000,0x40000000,0xc0000,0x100080,0x1c0000,0x0,0x0,0x3c5180,0x0,0x200080,0x0,0x3c5080,0x3c5080,0x0,0x0,0x3c5180,0x3c5080,0x0,0x3c5080,0x0,0x0,0x0,0xc00,0xc00,0x0,0x3c1080,0x200080,0x0,0x1000,0x100000,};
   }
   private static void jj_la1_1() {
      jj_la1_1 = new int[] {0x0,0x0,0x0,0x0,0x0,0x0,0x2,0x8,0x220012,0x8,0x10,0x20,0x220012,0x220012,0x20,0x80,0x220002,0x220002,0xff00,0x220012,0xff00,0x30000,0x30000,0xc0000,0xc0000,0x100000,0x220002,0x0,0x10,0x0,0x0,};
   }

  public nplp(java.io.InputStream stream) {
     this(stream, null);
  }
  public nplp(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new nplpTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 31; i++) jj_la1[i] = -1;
  }

  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 31; i++) jj_la1[i] = -1;
  }

  public nplp(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new nplpTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 31; i++) jj_la1[i] = -1;
  }

  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 31; i++) jj_la1[i] = -1;
  }

  public nplp(nplpTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 31; i++) jj_la1[i] = -1;
  }

  public void ReInit(nplpTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 31; i++) jj_la1[i] = -1;
  }

  final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.Vector jj_expentries = new java.util.Vector();
  private int[] jj_expentry;
  private int jj_kind = -1;

  public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[54];
    for (int i = 0; i < 54; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 31; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1<<j)) != 0) {
            la1tokens[32+j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 54; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  final public void enable_tracing() {
  }

  final public void disable_tracing() {
  }

  }
