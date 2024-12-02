/*:-----------------------------------------------------------------------------
 *:                       INSTITUTO TECNOLOGICO DE LA LAGUNA
 *:                     INGENIERIA EN SISTEMAS COMPUTACIONALES
 *:                         LENGUAJES Y AUTOMATAS II           
 *: 
 *:        SEMESTRE: ______________            HORA: ______________ HRS
 *:                                   
 *:               
 *:    # Clase con la funcionalidad del Generador de COdigo Intermedio
 *                 
 *:                           
 *: Archivo       : GenCodigoInt.java
 *: Autor         : Fernando Gil  
 *: Fecha         : 03/SEP/2014
 *: Compilador    : Java JDK 7
 *: Descripción   :  
 *:                  
 *:           	     
 *: Ult.Modif.    :
 *:  Fecha      Modificó            Modificacion
 *:=============================================================================
 *: 26/Nov/2024 FGil                Se implementó el parser predictivo recursivo
 *:                                 de lenguaje SIMPLE extendido, con la sig
 *:                                 gramatica:
 *:                                 P -> V C
 *:                                 V -> id : T  V | ϵ
 *:                                 T -> caracter | entero | real 
 *:                                 C -> inicio S fin
 *:                                 S -> id opasig E  S  | 
 *:                                      si K entonces inicio S fin L  S    |
 *:                                      mientras K hacer inicio S fin S    |
 *:                                      ϵ
 *:                                 L -> sino inicio S fin |  ϵ
 *:                                 K -> E oprel E
 *:                                 E -> M  E’ 
 *:                                 E’-> opsuma M  E’  | ϵ 
 *:                                 M -> F  M’ 
 *:                                 M’-> opmult   F  M’  | ϵ 
 *:                                 F -> id  | num | num.num  |  ( E )  
 *:                                 
 *:-----------------------------------------------------------------------------
 */

package compilador;

import general.Linea_BE;

public class GenCodigoInt {
    public static final int NIL = 0;
    
    private Compilador cmp;
    private int        consecTemp;
    private int        consecutivoEtiq;    
    private int        p;
    private String     preAnalisis;

    
    //--------------------------------------------------------------------------
    // Constructor de la clase, recibe la referencia de la clase principal del 
    // compilador.
    //
	public GenCodigoInt ( Compilador c ) {
        cmp = c;
    }
    // Fin del Constructor
    //--------------------------------------------------------------------------
	
    public void generar () {
        consecTemp = 1;
        preAnalisis = cmp.be.preAnalisis.complex;
        P ();        
    }  
    
    //--------------------------------------------------------------------------

    private void emite ( String c3d ) {
        cmp.erroresListener.mostrarCodInt ( c3d );
    }
    
    //--------------------------------------------------------------------------
    
    private String tempnuevo () {
        return "t" + consecTemp++;
    }
    
    //--------------------------------------------------------------------------
    
    private String etiqnueva () {
        return "etiq" + consecutivoEtiq++;
    }
    
    //--------------------------------------------------------------------------

    private void emparejar(String t) {
        if (cmp.be.preAnalisis.complex.equals(t)) {
            cmp.be.siguiente();
            preAnalisis = cmp.be.preAnalisis.complex;            
        } else {
            errorEmparejar( t, cmp.be.preAnalisis.lexema, cmp.be.preAnalisis.numLinea );
        }
    }
    
    //--------------------------------------------------------------------------
    // Metodo para devolver un error al emparejar
    //--------------------------------------------------------------------------
 
    private void errorEmparejar(String _token, String _lexema, int numLinea ) {
        String msjError = "";

        if (_token.equals("id")) {
            msjError += "Se esperaba un identificador";
        } else if (_token.equals("num")) {
            msjError += "Se esperaba una constante entera";
        } else if (_token.equals("num.num")) {
            msjError += "Se esperaba una constante real";
        } else if (_token.equals("literal")) {
            msjError += "Se esperaba una literal";
        } else if (_token.equals("oparit")) {
            msjError += "Se esperaba un operador aritmetico";
        } else if (_token.equals("oprel")) {
            msjError += "Se esperaba un operador relacional";
        } else if (_token.equals("opasig")) {
            msjError += "Se esperaba operador de asignacion";
        } else {
            msjError += "Se esperaba " + _token;
        }
        msjError += " se encontró " + ( _lexema.equals ( "$" )? "fin de archivo" : _lexema ) + 
                    ". Linea " + numLinea;        // FGil: Se agregó el numero de linea

        cmp.me.error(Compilador.ERR_SINTACTICO, msjError);
    }

    // Fin de ErrorEmparejar
    //--------------------------------------------------------------------------
	
    //--------------------------------------------------------------------------
    // Metodo para mostrar un error sintactico
 
    private void error ( String _descripError) {
        cmp.me.error(cmp.ERR_SINTACTICO, _descripError);
    }
 
    // Fin de error
    //--------------------------------------------------------------------------    
    
    private void P() {
        if ( preAnalisis.equals("id")
                || preAnalisis.equals("inicio")) {
            //P -> V C 
            V();
            C();
        } else {
            error("[P] Inicio incorrecto del programa. "   +
                    "No. Linea " + cmp.be.preAnalisis.numLinea );
        }
    }

    //------------------------------------------------------------------------------

    private void V() {
        if ( preAnalisis.equals("id")) {
            //V -> id : T V 
            emparejar("id");
            emparejar(":");
            T();
            V();
        } else {
            //V -> EMPTY
        }
    }

    //------------------------------------------------------------------------------

    private void T() {

        if ( preAnalisis.equals("caracter")) {
            //T -> caracter
            emparejar("caracter");
        } else if ( preAnalisis.equals("entero")) {
            //T -> entero
            emparejar("entero");
        } else if ( preAnalisis.equals("real")) {
            //T -> real
            emparejar("real");
        } else {
            error("[T] No se encontró tipo de dato. "   +
                    "No. Linea " + cmp.be.preAnalisis.numLinea );
        }
    }

    //------------------------------------------------------------------------------

    private void C() {

        if ( preAnalisis.equals("inicio")) {
            //C -> inicio S fin
            emparejar("inicio");
            S();
            emparejar("fin");
        } else {
            error("[C] Se esperaba la palabra reservada inicio. "   +
                    "No. Linea " + cmp.be.preAnalisis.numLinea );
        }
    }

    //------------------------------------------------------------------------------

    private void S() {
        Atributos E = new Atributos();
        Atributos K = new Atributos();
        Atributos L = new Atributos();  
        Linea_BE id = new Linea_BE ();
        
        if ( preAnalisis.equals("id")) {            
            //S -> id := E  S
            id = cmp.be.preAnalisis;
            emparejar("id");
            emparejar("opasig");
            E(E);
            //Acción Semántica 1
            p = cmp.ts.buscar(id.lexema);
            if( p!= NIL){
                emite( p + ":=" + E.lugar);
            }else{
                cmp.me.error(Compilador.ERR_CODINT, "[S] Simbolo no encontrado "+id.lexema );
            }
            //Fin Acción semántica 2
            S();
        } else if ( preAnalisis.equals("si")) {
            // S -> si  K entonces inicio S fin S
            emparejar("si");
            K(K);
            emparejar("entonces");
            emparejar("inicio");
            S();
            emparejar("fin");
            L (L);            
            S();
        } else if ( preAnalisis.equals("mientras")) {
            // S -> mientras  K hacer inicio S fin L S
            emparejar("mientras");
            K(K);
            emparejar("hacer");
            emparejar("inicio");
            S();
            emparejar("fin");
            S ();
        } else {
            //S -> EMPTY
        }
    }

    //------------------------------------------------------------------------
    
    private void L (Atributos L) {
        
        if ( preAnalisis.equals ( "sino" ) ) {
            // L -> sino inicio S fin
            emparejar ( "sino" );
            emparejar ( "inicio" );
            S ();
            emparejar ( "fin" );
        } else {
            // L -> empty
        }
    }
    
    //------------------------------------------------------------------------

    private void K(Atributos K) {
        if ( preAnalisis.equals("num" )
                || preAnalisis.equals("num.num" )
                || preAnalisis.equals("id")
                || preAnalisis.equals("(") ) {
            // K -> E oprel E 
            E();
            emparejar("oprel");
            E();
        } else {
            error("[K] Se esperaba el inicio de una expresion. "   +
                    "No. Linea " + cmp.be.preAnalisis.numLinea );
        }
    }

    //------------------------------------------------------------------------------
    
    private void E (Atributos E) {
        if ( preAnalisis.equals ( "id" ) ||
             preAnalisis.equals ( "num" ) || 
             preAnalisis.equals ( "num.num" ) ||
             preAnalisis.equals ( "(" ) ) {
            // E -> M  E’
            M  ();
            Ep ();
        }   
        else {
            error ( "[E] Se esperaba el inicio de una expresion. "   +
                    "No. Linea " + cmp.be.preAnalisis.numLinea );
        }
    }

    //------------------------------------------------------------------------------
    // E’-> opsuma M  E’  | ϵ 
    
    private void Ep (Atributos Ep) {
        if ( preAnalisis.equals ( "opsuma" ) ) {
          // E’-> opsuma M  E’
          emparejar ( "opsuma" );
          M ();
          Ep ();
        } else {
            // E' -> ϵ 
        }   
    }    

    //------------------------------------------------------------------------------
    // M -> F  M’ 
    
    private void M () {
        if ( preAnalisis.equals ( "id" ) ||
             preAnalisis.equals ( "num" ) || 
             preAnalisis.equals ( "num.num" ) ||
             preAnalisis.equals ( "(" ) ) {
            // M -> F  M’ 
            F ();
            Mp ();
        } else {
            error ( "[M] Se esperaba el termino de una expresion. "  +
                    "No. Linea " + cmp.be.preAnalisis.numLinea );
        }     
    }
    
    //------------------------------------------------------------------------------    
    // M’-> opmult   F  M’  | ϵ 
    
    private void Mp (Atributos Mp) {
        if ( preAnalisis.equals ( "opmult" ) ) {
          // M’-> opmult   F  M’
          emparejar ( "opmult" );
          F  ();
          Mp ();
        } else {
            // M' -> ϵ 
        }         
    }
    
    //------------------------------------------------------------------------------
    // F -> id  | num | num.num  |  ( E )
    
    private void F (Atributos F) {
        if ( preAnalisis.equals("num")) {
            // F -> num
            emparejar ("num");
        } else if ( preAnalisis.equals("num.num")) {
            // F -> num.num
            emparejar ("num.num");
        } else if ( preAnalisis.equals("id")) {
            // F -> id
            emparejar("id");
        } else if ( preAnalisis.equals("(") ) {
            // F -> ( E )
            emparejar("(");
            E ();
            emparejar(")");
        } else {
            error("[F] Expresion mal formada. " +
                    "No. Linea " + cmp.be.preAnalisis.numLinea );
        }
    }

    //------------------------------------------------------------------------------
    
}
