package compilador;

public class Cuadruplo {
    String op = "";
    String arg1 = "";
    String arg2 = "";
    String resultado = "";
    public Cuadruplo ( String op, String arg1, String arg2, String resultado ){
        this.op = op;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.resultado = resultado;
    }

    public String getOp() {
        return op;
    }

    public String getArg1() {
        return arg1;
    }

    public String getArg2() {
        return arg2;
    }

    public String getResultado() {
        return resultado;
    }
}
