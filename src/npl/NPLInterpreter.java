package npl;

import jason.NoValueException;
import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.PredicateIndicator;
import jason.asSyntax.Rule;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.bb.BeliefBase;
import jason.util.ToDOM;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** 
 * Interprets a NP for a particular scope (group, scheme, ...)
 * 
 * @author jomi
 */
public class NPLInterpreter implements ToDOM {

    private Agent            ag = null; // use a Jason agent to store the facts (BB)
    private Map<String,Norm> normsFail = null; // norms with failure consequence
    private Map<String,Norm> normsObl  = null; // norms with obligation consequence
    private Scope            scope = null;

    private Object           syncTransState = new Object();
    
    List<NormativeListener>  listeners = new CopyOnWriteArrayList<NormativeListener>();
    
    private ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1); // a thread that checks deadlines of obligations
    private ObligationStateTransition   oblUpdateThread;
    
    public final static Atom NPAtom   = new Atom("np");
    public final static Atom DynAtom  = new Atom("dyn");
    public final static Atom NormAtom = new Atom("npli");

    public void init() {
        ag    = new Agent();
        normsFail = new HashMap<String,Norm>();
        normsObl  = new HashMap<String,Norm>();
        ag.initAg();
        clearNP();
        //clearDynamicFacts();    
        oblUpdateThread = new ObligationStateTransition();
        oblUpdateThread.start();
    }
    
    public void stop() {
        scheduler.shutdownNow();
        if (oblUpdateThread != null)
            oblUpdateThread.interrupt();
    }
    
    public void addListener(NormativeListener ol) {
        listeners.add(ol);
    }
    public boolean removeListener(NormativeListener ol) {
        return listeners.remove(ol);
    }
    
    /** get all facts from a kind of source (os or oe) */
    public List<Literal> getSource(Atom s) {
        List<Literal> oel = new ArrayList<Literal>();
        for (Literal b: ag.getBB())
            if (b.hasSource(s))
                oel.add(b);
        return oel;
    }

    
    /** resets the interpreter with a new NP */
    public void setScope(Scope scope) {
        init();
        this.scope = scope;
        loadNP(scope);
    }
    
    public Scope getScope() {
        return scope;
    }
    
    /** loads facts from a NP scope into the interpreter */
    public void loadNP(Scope scope) {
        BeliefBase bb = ag.getBB();
        
        for (Rule r: scope.getRules()) {
            // normalise rules with empty body
            Literal l;
            if (r.getBody().equals(Literal.LTrue) && r.isGround())
                l = r.headClone();
            else
                l = r.clone();
            l.addSource(NPAtom);
            bb.add(1,l); // add in the end of the BB to preserve the program order
        }
        for (Norm n: scope.getNorms()) {
            if (n.getConsequence().getFunctor().equals(NormativeProgram.OblFunctor))
                normsObl.put(n.getId(), n.clone());
            else 
                normsFail.put(n.getId(), n.clone());
        }
        
        if (scope.getFather() != null)
            loadNP(scope.getFather());
    }
    
    /** removes all facts/rules that comes from NP */
    public void clearNP() {
        BeliefBase bb = ag.getBB();
        for (Literal b: getSource(NPAtom))
            bb.remove(b);
    }
    
    
    /** replace all dynamic facts by those informed in the parameter ll  */
    /*
    public void setDynamicFacts(List<Literal> ll)  {
        synchronized (syncTransState) {
            //int x = ag.getBB().size();
            clearDynamicFacts();
            //int y = ag.getBB().size();
            loadDynamicFacts(ll);
            //System.out.println("***"+x+"/"+y+"/"+ag.getBB().size()+"/"+ll.size()+":"+ll);
        }
    }
    */
    
    /** include new dynamic facts */
    /*
    public void loadDynamicFacts(Collection<Literal> ll) {
        BeliefBase bb = ag.getBB();
        for (Literal l: ll) {
            l = l.copy();
            l.addSource(DynAtom);
            bb.add(l);
        }
    }
    */
    
    /** remove all dynamic facts/rules */
    /*
    public void clearDynamicFacts() {
        BeliefBase bb = ag.getBB();
        for (Literal b: getSource(DynAtom))
            bb.remove(b);
    }
    */
    
    /** get active obligations (those not fulfilled) */
    public List<Literal> getActiveObligations() {
        return getObligationsByState(NormativeProgram.ACTPI);
    }

    /** get fulfilled obligations */
    public List<Literal> getFulfilledObligations() {
        return getObligationsByState(NormativeProgram.FFPI);
    }
    
    /** get unfulfilled obligations */
    public List<Literal> getUnFulfilledObligations() {
        return getObligationsByState(NormativeProgram.UFPI);
    }
    
    /** get fulfilled obligations */
    public List<Literal> getInactiveObligations() {
        return getObligationsByState(NormativeProgram.INACPI);
    }
    
    private List<Literal> getObligationsByState(PredicateIndicator state) {
        List<Literal> ol = new ArrayList<Literal>();
        synchronized (syncTransState) {
            Iterator<Literal> i = ag.getBB().getCandidateBeliefs(state);
            if (i != null) {
                while (i.hasNext()) {
                    Literal b = i.next();
                    if (b.hasSource(NormAtom))
                        ol.add((Literal)b.getTerm(0));
                }
            }
        }
        return ol;
    }

    
    public Agent getAg() {
        return ag;
    }
    
    public boolean holds(LogicalFormula l) {
        try {
            Iterator<Unifier> i = l.logicalConsequence(ag, new Unifier());
            return i.hasNext();
        } catch (ConcurrentModificationException e) {
            System.out.println("*-*-* concurrent exception in NPLI holds method, I'll try again later....");
            // try again later
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {            }
            return holds(l);
        }
    }
    
    public Norm getNorm(String id) {
        Norm n = normsObl.get(id);
        if (n != null)
            return n;
        else
            return normsFail.get(id);
    }
    
    /** 
     * verifies all norms to identify failure (exception) or new obligations
     *  
     * @return list of obligations added
     */
    public Collection<Literal> verifyNorms() throws NormativeFailureException {
        BeliefBase bb = ag.getBB();
        List<Literal> newObl = new ArrayList<Literal>();
        synchronized (syncTransState) {            
            // test all fails first
            for (Norm n: normsFail.values()) {
                Iterator<Unifier> i = n.getCondition().logicalConsequence(ag, new Unifier());
                while (i.hasNext()) {
                    Unifier u = i.next();
                    //System.out.println("    solution "+u+" for "+n.getCondition());
                    Literal head = (Literal)n.getConsequence().capply(u);
                    if (head.getFunctor().equals(NormativeProgram.FailFunctor)) {
                        notifyNormFailure(head);
                        throw new NormativeFailureException((Structure)head);
                    }
                }
            }            
            
            List<Literal> activeObl = getActiveObligations();
            //List<Literal> unfulObl  = getUnFulfilledObligations();
            
            /*
            // -- transition active -> inactive, fulfilled
            for (Literal o: activeObl) { 
                Literal oasinbb = createObligationState(NormativeProgram.ActFunctor, o);
                boolean done = holds((Literal)o.getTerm(2));
                long ttf = System.currentTimeMillis()-(long)((NumberTerm)o.getTerm(3)).solve();
                if (done) {
                    // transition active -> fulfilled
                    if (!bb.remove(oasinbb)) System.out.println("ooops obligation should be removed 2");
                    o = o.copy();
                    o.addAnnot(ASSyntax.createStructure("done", new TimeTerm(ttf, "milliseconds")));
                    o.addAnnot(ASSyntax.createStructure("fulfilled", new TimeTerm(0,null)));
                    //System.out.println("fulfilled "+o);
                    bb.add(createObligationState(NormativeProgram.FFFunctor, o));
                    notifyOblFulfilled(o);
                } else if (!activationConditionHolds(o)) {
                    // transition active -> inactive
                    if (!bb.remove(oasinbb)) System.out.println("ooops obligation should be removed 1");
                    remObl.add(o);
                    o.addAnnot(ASSyntax.createStructure("inactive", new TimeTerm(0,null)));
                    if (!bb.add(createObligationState(NormativeProgram.InactFunctor, o))) System.out.println("ooops inactive obligation should be added");
                    notifyOblInactive(o);
                }
            }
            activeObl = getActiveObligations(); // update active obl
            */
            
            // -- computes new obligations
            for (Norm n: normsObl.values()) {
                Iterator<Unifier> i = n.getCondition().logicalConsequence(ag, new Unifier());
                while (i.hasNext()) {
                    Unifier u = i.next();
                    //System.out.println("    solution "+u+" for "+n.getCondition());
                    Literal obl = (Literal)n.getConsequence().capply(u);
                    // check if already in BB
                    if (!containsIgnoreDeadline(activeObl, obl) // is it a new obligation?  
                        //!containsIgnoreDeadline(unfulObl, head) &&
                        //!containsIgnoreDeadline(fulObl, head)
                        //) {
                        && !holds((Literal)obl.getTerm(2))) { // that is not achieved yet
                        
                        obl.addAnnot(ASSyntax.createStructure("created", new TimeTerm(0,null)));
                        if (bb.add(createObligationState(NormativeProgram.ActFunctor, obl))) {
                            //System.out.println("add "+createObligationState(NormativeProgram.ActFunctor, obl));
                            //System.out.println("* create "+obl+"\nactive: "+ activeObl);
                            newObl.add(obl);
                            activeObl.add(obl);
                            addObligationInSchedule(obl);
                            notifyOblCreated(obl);
                        }
                    }
                }
            }
            
            // The code below was moved to another thread
            
            // -- transition unfulfilled -> inactive
            /*
            for (Literal o: getUnFulfilledObligations()) {
                boolean done = holds((Literal)o.getTerm(2));
                if (done) { // if the agent did, even latter...
                    long ttf = System.currentTimeMillis()-(long)((NumberTerm)o.getTerm(3)).solve();
                    o.addAnnot(ASSyntax.createStructure("done", new TimeTerm(ttf, "milliseconds")));                
                }
                if (!activationConditionHolds(o)) {
                    Literal oasinbb = createObligationState(NormativeProgram.UFFFunctor, o);
                    if (!bb.remove(oasinbb)) System.out.println("ooops obligation should be removed 4");
                    o.addAnnot(ASSyntax.createStructure("inactive", new TimeTerm(0,null)));
                    bb.add(createObligationState(NormativeProgram.InactFunctor, o));
                    for (ObligationListener l: listeners)
                        l.inactive(o);
                }
            }
            */
            
            // -- transition fulfilled -> inactive
            /*
            for (Literal o: getFulfilledObligations()) {
                //boolean done = holds((Literal)o.getTerm(2));
                if (!activationConditionHolds(o)) {
                    Literal oasinbb = createObligationState(NormativeProgram.FFFunctor, o);
                    if (!bb.remove(oasinbb)) System.out.println("ooops obligation should be removed 5");
                    o.addAnnot(ASSyntax.createStructure("inactive", new TimeTerm(0,null)));
                    bb.add(createObligationState(NormativeProgram.InactFunctor, o));
                    for (ObligationListener l: listeners)
                        l.inactive(o);
                }
            }
            */
            
            // check done for unfulfilled and inactive
            /*List<Literal> unfulPlusInactObls = getInactiveObligations();
            unfulPlusInactObls.addAll(unfulObl);
            for (Literal o: unfulPlusInactObls) {
                if (holds((Literal)o.getTerm(2)) && o.getAnnots("done").isEmpty()) { // if the agent did, even latter...
                    long ttf = System.currentTimeMillis()-(long)((NumberTerm)o.getTerm(3)).solve();
                    o.addAnnot(ASSyntax.createStructure("done", new TimeTerm(ttf, "milliseconds")));                
                }            
            }*/
        }
        oblUpdateThread.update();
        return newObl;
    }

    private long getOblTTF(final Literal o, final int pos) {
        try {
            return (long)((NumberTerm)o.getTerm(pos)).solve();
        } catch (NoValueException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    private void addObligationInSchedule(final Literal o) {
        long ttf = getOblTTF(o,3) - System.currentTimeMillis();

        scheduler.schedule(new Runnable() {
            public void run() {
                oblUpdateThread.checkUnfulfilled(o);
                /* Moved to the update thread
                 
                // if the obligation is still active, if becomes unfulfilled
                synchronized (syncTransState) {            
                    if (containsIgnoreDeadline(getActiveObligations(), o)) {
                        BeliefBase bb = ag.getBB();
                        //System.out.println("*** unfulfilled "+o);
                        o.addAnnot(ASSyntax.createStructure("unfulfilled", new TimeTerm(0,null)));
                        Literal oasinbb = createObligationState(NormativeProgram.ActFunctor, o);
                        if (!bb.remove(oasinbb)) System.out.println("ooops 3 obligation "+o+" should be removed, becomes unfulfilled, but it is not in the set of facts.");
                        bb.add(createObligationState(NormativeProgram.UFFFunctor, o));
                        notifyOblUnfulfilled(o);
                        try {
                            verifyNorms();
                        } catch (NormativeFailureException e) {
                            //System.err.println("Error to set obligation "+o+" to unfulfilled!");
                            //e.printStackTrace();
                        }
                    }
                }
                */
            }
        }, ttf, TimeUnit.MILLISECONDS);
    }
    
    private void notifyOblCreated(Literal o) {
        for (NormativeListener l: listeners)
            try {
                l.created((Structure)o.clone());                
            } catch (Exception e) {
                System.err.println("Error notifying normative listener "+l);
                e.printStackTrace();
            }
    }
    private void notifyOblFulfilled(Literal o) {
        for (NormativeListener l: listeners)
            try {
                l.fulfilled((Structure)o.clone());
            } catch (Exception e) {
                System.err.println("Error notifying normative listener "+l);
                e.printStackTrace();
            }
    }
    private void notifyNormFailure(Literal f) {
        for (NormativeListener l: listeners)
            try {
                l.failure((Structure)f.clone());
            } catch (Exception e) {
                System.err.println("Error notifying normative listener "+l);
                e.printStackTrace();
            }
    }
    private void notifyOblUnfulfilled(Literal o) {
        for (NormativeListener l: listeners)
            try {
                l.unfulfilled((Structure)o.clone());
            } catch (Exception e) {
                System.err.println("Error notifying normative listener "+l);
                e.printStackTrace();
            }
    }
    private void notifyOblInactive(Literal o) {
        for (NormativeListener l: listeners)
            try {
                l.inactive((Structure)o.clone());
            } catch (Exception e) {
                System.err.println("Error notifying normative listener "+l);
                e.printStackTrace();
            }
    }

    
    private boolean activationConditionHolds(Literal obl) {
        Norm n = getNorm( ((Literal)obl.getTerm(1)).getFunctor() );
        // if the condition of the norm still holds
        Iterator<Unifier> i = n.getCondition().logicalConsequence(ag, new Unifier());
        while (i.hasNext()) {
            Unifier u = i.next();
            Literal head = (Literal)n.getConsequence().capply(u);
            if (equalsIgnoreDeadline(head, obl)) {
                return true;
            }
        }
        return false;
    }
    
    private Literal createObligationState(String state, Literal o) {
        Literal s = ASSyntax.createLiteral(state, o);
        s.addSource(NormAtom);
        return s;
    }
    
    private boolean containsIgnoreDeadline(Collection<Literal> list, Literal obl) {
        for (Literal l: list)
            if (equalsIgnoreDeadline(l, obl))
                return true;
        return false;
    }
    private boolean equalsIgnoreDeadline(Literal o1, Literal o2) {
        return o1.getTerm(0).equals(o2.getTerm(0)) && // agent
               o1.getTerm(1).equals(o2.getTerm(1)) && // reason
               o1.getTerm(2).equals(o2.getTerm(2));   // goal
    }
    
    public String getStateString() {
        StringBuilder out = new StringBuilder("--- normative state for program "+scope.getId()+" ---\n\n");
        out.append("active obligations:\n");
        for (Literal l: getActiveObligations()) {
            out.append("  "+wellFormatTime(l)+"\n");
        }
        out.append("\nunfulfilled obligations:\n");
        for (Literal l: getUnFulfilledObligations()) {
            out.append("  "+wellFormatTime(l)+"\n");
        }
        out.append("\nfulfilled obligations:\n");
        for (Literal l: getFulfilledObligations()) {
            out.append("  "+wellFormatTime(l)+"\n");
        }
        return out.toString();
    }
    private String wellFormatTime(Literal l) {
        if (l.getFunctor().equals(NormativeProgram.OblFunctor)) {
            long t = getOblTTF(l,3);
            return l.getFunctor()+"("+l.getTerm(0)+","+l.getTerm(1)+","+l.getTerm(2)+","+TimeTerm.toRelTimeStr(t)+")";
        } else if (l.getFunctor().equals(NormativeProgram.FFFunctor)) {
            Literal o = (Literal)l.getTerm(0);
            long t = getOblTTF(o,3);
            String so = o.getFunctor()+"("+o.getTerm(0)+","+o.getTerm(1)+","+o.getTerm(2)+","+TimeTerm.toTimeStamp(t)+")";
            t = getOblTTF(l,1);
            return l.getFunctor()+"("+so+","+TimeTerm.toAbsTimeStr(t)+")";            
        }
        return l.toString();
    }
    
    public Element getAsDOM(Document document) {
        Element ele = (Element) document.createElement("normative-state");
        if (scope != null)
            ele.setAttribute("id", scope.getId().toString());
        for (Literal l: getUnFulfilledObligations())
            ele.appendChild( obligation2dom(document, l, "unfulfilled", true));
        for (Literal l: getActiveObligations())
            ele.appendChild( obligation2dom(document, l, "active", true));
        for (Literal l: getFulfilledObligations()) 
            ele.appendChild( obligation2dom(document, l, "fulfilled", false));
        for (Literal l: getInactiveObligations()) 
            ele.appendChild( obligation2dom(document, l, "inactive", false));
        return ele;
    }
    private Element obligation2dom(Document document, Literal l, String state, boolean reltime) {
        Element oblele = (Element) document.createElement("obligation");
        try {            
            oblele.setAttribute("state", state);
            oblele.setAttribute("agent", l.getTerm(0).toString());
            oblele.setAttribute("reason", l.getTerm(1).toString());
            oblele.setAttribute("object", l.getTerm(2).toString());
            long ttf = getOblTTF(l,3);
            if (reltime)
                oblele.setAttribute("ttf", TimeTerm.toRelTimeStr(ttf));
            else
                oblele.setAttribute("ttf", TimeTerm.toTimeStamp(ttf));
            
            List<Term> al = l.getAnnots("done");
            if (!al.isEmpty()) {
                Structure annot = (Structure)al.get(0);
                long toff = getOblTTF(annot,0);
                oblele.setAttribute("done", TimeTerm.toAbsTimeStr(toff));  
            }
        } catch (Exception e) {
            System.err.println("Error adding attribute in DOM for "+l+" "+state);
            e.printStackTrace();            
        }

        try {       
            if (l.hasAnnot()) {
                for (Term t: l.getAnnots()) {
                    if (t instanceof Literal) {
                        Literal la = (Literal)t;
                        if (!la.getFunctor().equals("done")) {
                            Element annotele = (Element) document.createElement("annotation");
                            annotele.setAttribute("id", la.getFunctor());
                            if (la.getArity() == 1 && la.getTerm(0) instanceof TimeTerm) {
                                annotele.setAttribute("value", TimeTerm.toTimeStamp( (long)((TimeTerm)la.getTerm(0)).solve() ));                    
                            } else {
                                annotele.setAttribute("value", la.getTerms().toString());
                            }
                            oblele.appendChild(annotele);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error adding annotations in DOM for "+l+" "+state);
            e.printStackTrace();
        }
        return oblele;
    }

    
    @Override
    public String toString() {
        return "normative interpreter for "+scope.getId();
    }


    private int updateInterval = 1000;
    
    /** sets the update interval for checking the change in obligation states */
    public void setUpdateInterval(int miliseconds) {
        updateInterval = miliseconds;
    }

    /** this thread updates the state of obligations (e.g. active -> fulfilled) 
        each second (by default) */
    class ObligationStateTransition extends Thread {
        
        private boolean        update = false;
        private List<Literal>  activeObl = null;            
        private BeliefBase     bb;
        private Queue<Literal> toCheckUnfulfilled = new ConcurrentLinkedQueue<Literal>();
                        
        /** update the state of the obligations */
        void update() {
            update = true;
        }
        
        void setUpdateInterval(int miliseconds) {
            updateInterval = miliseconds;
        }
        
        void checkUnfulfilled(Literal o) {
            toCheckUnfulfilled.offer(o);
            update = true;
        }
        
        @Override
        synchronized public void run() {
            boolean concModifExp = false;
            while (true) {
                try {
                    if (concModifExp) {
                        sleep(50);
                        concModifExp = false;
                    } else {
                        sleep(updateInterval);
                    }
                    if (update) {
                        bb = ag.getBB();
                        synchronized (syncTransState) {
                            update = false;
                            updateActive();
                            updateUnfulfilled();
                            updateDoneForUnfulfilled();
                        }
                    }
                } catch (ConcurrentModificationException e) {
                    // sleeps a while and try again
                    concModifExp = true;
                } catch (InterruptedException e) {
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        // -- transition active -> inactive, fulfilled
        private void updateActive() {
            activeObl = getActiveObligations();  
            for (Literal o: activeObl) { 
                Literal oasinbb = createObligationState(NormativeProgram.ActFunctor, o);
                boolean done = holds((Literal)o.getTerm(2));
                long ttf = System.currentTimeMillis()-getOblTTF(o,3);
                if (done) {
                    // transition active -> fulfilled
                    if (!bb.remove(oasinbb)) System.out.println("ooops obligation should be removed 2");
                    o = o.copy();
                    o.addAnnot(ASSyntax.createStructure("done", new TimeTerm(ttf, "milliseconds")));
                    o.addAnnot(ASSyntax.createStructure("fulfilled", new TimeTerm(0,null)));
                    //System.out.println("fulfilled "+o);
                    bb.add(createObligationState(NormativeProgram.FFFunctor, o));
                    notifyOblFulfilled(o);
                } else if (!activationConditionHolds(o)) {
                    // transition active -> inactive
                    if (!bb.remove(oasinbb)) System.out.println("ooops obligation should be removed 1");
                    o.addAnnot(ASSyntax.createStructure("inactive", new TimeTerm(0,null)));
                    if (!bb.add(createObligationState(NormativeProgram.InactFunctor, o))) System.out.println("ooops inactive obligation should be added");
                    notifyOblInactive(o);
                }
            }            
            activeObl = getActiveObligations();            
        }
        
        // -- transition active -> unfulfilled
        private void updateUnfulfilled() {
            Literal o = toCheckUnfulfilled.poll();
            while (o != null) {
                if (containsIgnoreDeadline(activeObl, o)) {
                    //System.out.println("*** unfulfilled "+o);
                    Literal oasinbb = createObligationState(NormativeProgram.ActFunctor, o);                
                    if (!bb.remove(oasinbb)) System.out.println("ooops 3 obligation "+o+" should be removed, becomes unfulfilled, but it is not in the set of facts.");
                    o.addAnnot(ASSyntax.createStructure("unfulfilled", new TimeTerm(0,null)));
                    bb.add(createObligationState(NormativeProgram.UFFFunctor, o));
                    notifyOblUnfulfilled(o);
                    try {
                        verifyNorms();
                    } catch (NormativeFailureException e) {
                        //System.err.println("Error to set obligation "+o+" to unfulfilled!");
                        //e.printStackTrace();
                    }
                }
                o = toCheckUnfulfilled.poll();
            }
        }
        
        private void updateDoneForUnfulfilled() {
            // check done for unfulfilled and inactive
            List<Literal> unfulObl  = getUnFulfilledObligations();
            List<Literal> unfulPlusInactObls = getInactiveObligations();
            unfulPlusInactObls.addAll(unfulObl);
            for (Literal o: unfulPlusInactObls) {
                if (holds((Literal)o.getTerm(2)) && o.getAnnots("done").isEmpty()) { // if the agent did, even latter...
                    long ttf = System.currentTimeMillis()-getOblTTF(o,3);
                    o.addAnnot(ASSyntax.createStructure("done", new TimeTerm(ttf, "milliseconds")));                
                }            
            }
        }
                
    }
}
