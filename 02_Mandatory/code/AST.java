import java.util.ArrayList;
import java.util.List;

public abstract class AST {
    public void error(String msg) {
        System.err.println(msg);
        System.exit(-1);
    }
};

/* Expressions are similar to arithmetic expressions in the impl
   language: the atomic expressions are just Signal (similar to
   variables in expressions) and they can be composed to larger
   expressions with And (Conjunction), Or (Disjunction), and Not
   (Negation). Moreover, an expression can be using any of the
   functions defined in the definitions. */

abstract class Expr extends AST {
    abstract public boolean eval(Environment env);
}


class Conjunction extends Expr {
    // Example: Signal1 * Signal2 
    Expr e1, e2;

    Conjunction(Expr e1, Expr e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    @Override
    public boolean eval(Environment env) {
        return e1.eval(env) && e2.eval(env);
    }
}

class Disjunction extends Expr {
    // Example: Signal1 + Signal2 
    Expr e1, e2;

    Disjunction(Expr e1, Expr e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    @Override
    public boolean eval(Environment env) {
        return e1.eval(env) || e2.eval(env);
    }
}

class Negation extends Expr {
    // Example: /Signal
    Expr e;

    Negation(Expr e) {
        this.e = e;
    }

    @Override
    public boolean eval(Environment env) {
        return !e.eval(env);
    }
}


class Def extends AST {
    // Definition of a function
    // Example: def xor(A,B) = A * /B + /A * B
    String f; // function name, e.g. "xor"
    List<String> args;  // formal arguments, e.g. [A,B]
    Expr e;  // body of the definition, e.g. A * /B + /A * B

    Def(String f, List<String> args, Expr e) {
        this.f = f;
        this.args = args;
        this.e = e;
    }

    @Override
    public String toString() {
        return f + "\n" + args + "\n";
    }
}

class UseDef extends Expr {
    // Using any of the functions defined by "def"
    // e.g. xor(Signal1,/Signal2) 
    String f;  // the name of the function, e.g. "xor" 
    List<Expr> args;  // arguments, e.g. [Signal1, /Signal2]

    UseDef(String f, List<Expr> args) {
        this.f = f;
        this.args = args;
    }

    @Override
    public boolean eval(Environment env) {
        Def def = env.getDef(f);

        Environment defEnv = new Environment(env);

        for (int i = 0; i < def.args.size(); i++) {
            defEnv.setVariable(def.args.get(i), this.args.get(i).eval(env));
        }

        return def.e.eval(defEnv);
    }
}

class Signal extends Expr {
    String varname; // a signal is just identified by a name 

    Signal(String varname) {
        this.varname = varname;
    }

    @Override
    public boolean eval(Environment env) {
        return env.getVariable(varname);
    }
}



// An Update is any of the lines " signal = expression "
// in the update section

class Update extends AST {
    // Example Signal1 = /Signal2
    String name;  // Signal being updated, e.g. "Signal1"
    Expr e;  // The value it receives, e.g., "/Signal2"

    Update(String name, Expr e) {
        this.e = e;
        this.name = name;
    }

    void eval(Environment env) {
        env.setVariable(name, e.eval(env));
    }
}

/* A Trace is a signal and an array of Booleans, for instance each
   line of the .simulate section that specifies the traces for the
   input signals of the circuit. It is suggested to use this class
   also for the output signals of the circuit in the second
   assignment.
*/

class Trace extends AST {
    // Example Signal = 0101010
    String signal;
    Boolean[] values;

    Trace(String signal, Boolean[] values) {
        this.signal = signal;
        this.values = values;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Boolean value : values) {
            sb.append(value ? "1" : "0");
        }
        return sb.toString();
    }
}

/* The main data structure of this simulator: the entire circuit with
   its inputs, outputs, latches, definitions and updates. Additionally
   for each input signal, it has a Trace as simulation input.

   There are two variables that are not part of the abstract syntax
   and thus not initialized by the constructor (so far): simoutputs
   and simlength. It is suggested to use these two variables for
   assignment 2 as follows:

   1. all siminputs should have the same length (this is part of the
   checks that you should implement). set simlength to this length: it
   is the number of simulation cycles that the interpreter should run.

   2. use the simoutputs to store the value of the output signals in
   each simulation cycle, so they can be displayed at the end. These
   traces should also finally have the length simlength.
*/


class Circuit extends AST {
    String name;
    List<String> inputs;
    List<String> outputs;
    List<String> latches;
    List<Def> definitions;
    List<Update> updates;
    List<Trace> siminputs;
    List<Trace> simoutputs;

    int simlength;

    Circuit(String name,
            List<String> inputs,
            List<String> outputs,
            List<String> latches,
            List<Def> definitions,
            List<Update> updates,
            List<Trace> siminputs) {
        this.name = name;
        this.inputs = inputs;
        this.outputs = outputs;
        this.latches = latches;
        this.definitions = definitions;
        this.updates = updates;
        this.siminputs = siminputs;

        this.simoutputs = new ArrayList<>();
        // total cycles
        this.simlength = siminputs.getFirst().values.length;
    }

    private void initialize(Environment env) {
        if (siminputs.isEmpty()) {
            error("No Inputs Given");
        }

        // Latches init
        // initializes all latches with false
        for (String latch : latches) {
            env.setVariable(latch + "'", false);
        }

        // initializes all input values.
        // Reading the input values of every input signal at time point 0.
        for (Trace siminput : siminputs) {
            env.setVariable(siminput.signal, siminput.values[0]);
        }

        // first cycle
        // is run to initilize the first v
        /*
            Example of a 4 sized update:

                Red = /Red' * Button
                Green  = /Red
                PGreen = Red
                PRed   = /PGreen
         */
        for (int i = 0, updatesSize = updates.size(); i < updatesSize; i++) {
            Update update = updates.get(i);
            update.eval(env);

            // for storing outputs later:
            Trace traceOutput = new Trace(update.name, new Boolean[simlength]);
            traceOutput.values[0] = env.getVariable(update.name);
            simoutputs.add(traceOutput);
        }
    }


    private void latchesUpdate(Environment env) {
        for (String latch : latches) {
            boolean updatedVal = env.getVariable(latch);
            env.setVariable(latch + "'", updatedVal);
        }
    }

    private void nextCycle(Environment env, int i) {
        // errors can happen if i is greater than sim input length:
        for (Trace siminput : siminputs) {
            env.setVariable(siminput.signal, siminput.values[i]);
        }

        latchesUpdate(env);

        // run evales on update
        for (int j = 0, updatesSize = updates.size(); j < updatesSize; j++) {
            // j counts updates
            // i is current cycle
            Update update = updates.get(j);
            update.eval(env);

            // for storing outputs later:
            this.simoutputs.get(j).values[i] = env.getVariable(update.name);
        }
    }

    public String runSimulator() {
        Environment env = new Environment(definitions);
        initialize(env);

        // We start from 1 not 0. because 1 iteration is run in the initialize method!
        for (int i = 1; i < simlength; i++) {
            nextCycle(env, i);
        }

        return output();
    }

    private String output(){
        simoutputs.addAll(siminputs);

        StringBuilder sb = new StringBuilder();

        int maxSignalLength = 0;
        for (Trace simoutput : simoutputs) {
            maxSignalLength = Math.max(maxSignalLength, simoutput.signal.length());
        }
        // Create the table
        for (Trace simoutput : simoutputs) {
            // Format signal name to align properly
            sb.append(String.format("%-" + (maxSignalLength + 2) + "s", simoutput.signal)); // Left-align signal names

            for (boolean value : simoutput.values) {
                sb.append(value ? "1 " : "0 ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

}


