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
 *:                                 Se implementaron las acciones semánticas de 
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
        consecutivoEtiq = 0;
        preAnalisis = cmp.be.preAnalisis.complex;
        P ();        
    }  
    
    //--------------------------------------------------------------------------

    private void emite ( String c3d ) {
        cmp.iuListener.mostrarCodInt ( c3d );
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
    // Método para devolver un error al emparejar
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
    // Método para mostrar un error sintáctico
 
    private void error ( String _descripError ) {
        cmp.me.error ( cmp.ERR_SINTACTICO, _descripError );
    }
 
    // Fin de error
    //------------------------------------------------------------------------------    
    
    private void P () {
        if ( preAnalisis.equals("id")
                || preAnalisis.equals("inicio")) {
            // P -> V C 
            V();
            C();
        } else {
            error ( "[P] Inicio incorrecto del programa. " +
                    "No. Linea " + cmp.be.preAnalisis.numLinea );
        }
    }

    //------------------------------------------------------------------------------

    private void V () {
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

    private void T () {

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
                    "No. Línea " + cmp.be.preAnalisis.numLinea );
        }
    }

    //------------------------------------------------------------------------------

    private void C () {

        if ( preAnalisis.equals("inicio")) {
            //C -> inicio S fin
            emparejar("inicio");
            S();
            emparejar("fin");
        } else {
            error("[C] Se esperaba la palabra reservada inicio. "   +
                    "No. Línea " + cmp.be.preAnalisis.numLinea );
        }
    }

    //------------------------------------------------------------------------------

    private void S ( ) {
        // Variables locales
        Linea_BE id = new Linea_BE();
        Atributos E = new Atributos();
        Atributos K = new Atributos();       
        Atributos L = new Atributos();
        Atributos S = new Atributos();
        Atributos J = new Atributos();
        
        if ( preAnalisis.equals ( "id" ) ) {
            // S -> id := E { 1 } S
            
            id = cmp.be.preAnalisis;
            emparejar ( "id" );
            emparejar ( "opasig" );
            E ( E );
            //E.lugar = "t3"; // prueba, borrar
            
            // Acción Semántica 1
            p = cmp.ts.buscar ( id.lexema );
            
            if ( p != NIL ) {
                emite ( id.lexema + " := " + E.lugar );
            } else {
                cmp.me.error ( Compilador.ERR_CODINT, "[S] Símbolo " + id.lexema + " no encontrado" );
            }
            // Fin Acción Semántica 1
            
            S ();
        } else if ( preAnalisis.equals ( "si" ) ) {
            // S -> si  K entonces inicio S fin S
            
            emparejar ( "si" );
            
            // Acción Semántica 6
            K.verdadera = etiqnueva();
            S.siguiente = etiqnueva();
            J.siguiente = etiqnueva();
            
            K.falsa = S.siguiente;
            // Fin Acción Semántica 6
            
            K ( K );
            emparejar ( "entonces" );
            emparejar ( "inicio" );
            
            // Acción Semántica 7
            emite ( K.verdadera + ":" );
            // Fin Acción Semántica 7
            
            S ();
            
            emite ( "goto " + J.siguiente );
            // Acción Semántica 8
            emite ( K.falsa + ":" );
            // Fin Acción Semántica 8
            
            
            emparejar ( "fin" );
            L ( L );    
            
            // Acción Semántica 9
            L.h = K.falsa;
            L.vacio = S.siguiente;
            // Fin Acción Semántica 9
            
            emite (J.siguiente + ":" );
            
            S ();
            
            
        } else if ( preAnalisis.equals ( "mientras" ) ) {
            // S -> mientras  K hacer inicio S fin L S
            
            emparejar ( "mientras" );
            
            // Acción Semántica 2
            S.comienzo = etiqnueva();
            K.verdadera = etiqnueva();
            S.siguiente = etiqnueva();
            K.falsa = S.siguiente;
            emite ( S.comienzo + ":" );
            // Fin Acción Semántica 2
            
            K ( K );
            emparejar ( "hacer" );
            emparejar ( "inicio" );
            
            // Acción Semántica 3
            emite ( K.verdadera + ":" );
            // Fin Acción Semántica 3
            
            S ();
            
            // Acción Semántica 4
            emite ( "goto" + S.comienzo );
            emite ( K.falsa + ":" );
            // Fin Acción Semántica 4
            
            emparejar ( "fin" );
            S ();
            
        } else {
            // S -> EMPTY 
            
            // Acción Semántica 26
            S.siguiente = S.h;
            // Fin Acción Semántica 26
        }
    }

    //------------------------------------------------------------------------
    
    private void L ( Atributos L ) {
        if ( preAnalisis.equals ( "sino" ) ) {
            // L -> sino inicio S fin
            
            emparejar ( "sino" );
            emparejar ( "inicio" );
            
            // Acción Semántica 10
            //emite ( L.siguiente + ":" );
            // Fin Acción Semántica 10
            
            S ();
            emparejar ( "fin" );
        } else {
            // L -> empty
            
            // Acción Semántica 11
            L.siguiente = L.h;
            // Fin Acción Semántica 11
        }
    }
    
    //------------------------------------------------------------------------

    private void K ( Atributos K ) {
        // Variables locales
        Atributos E1 = new Atributos();
        Atributos E2 = new Atributos();
        Linea_BE oprel = new Linea_BE();
        
        if ( preAnalisis.equals("num" ) || preAnalisis.equals("num.num" )
             || preAnalisis.equals("id") || preAnalisis.equals("(") ) {
            
            // K -> E oprel E 
            E ( E1 );
            oprel = cmp.be.preAnalisis;
            emparejar("oprel");
            E ( E2 );
            
            // Acción Semántica 5
            emite ( "if" + E1.lugar + oprel.lexema + E2.lugar + "goto" + K.verdadera );
            emite ( "goto" + K.falsa );
            // Fin Acción Semántica 5
            
        } else {
            error("[K] Se esperaba el inicio de una expresión. "   +
                    "No. Línea " + cmp.be.preAnalisis.numLinea );
        }
    }

    //------------------------------------------------------------------------------
    
    private void E ( Atributos E ) {
        // Variables locales
        Atributos M = new Atributos();
        Atributos Ep = new Atributos();
        Polaca expr = new Polaca();
        String exprInfija = "";
        String exprPrefija = "";
        
        if ( preAnalisis.equals ( "id" ) || preAnalisis.equals ( "num" ) || 
             preAnalisis.equals ( "num.num" ) || preAnalisis.equals ( "(" ) ) {
            
            // E -> M E’
            
            M ( M );
            
            // Acción Semántica 12
            Ep.h = M.lugar;
            // Fin Acción Semántica 12

            Ep ( Ep );
            
            // Acción Semántica 13
            exprPrefija = expr.notacionPolaca ( exprInfija );
            E.lugar = Ep.h;
            //emite( E.lugar + " := " + Ep.h);
            // Fin Acción Semántica 13
        }   
        else {
            error ( "[E] Se esperaba el inicio de una expresión. "   +
                    "No. Línea " + cmp.be.preAnalisis.numLinea );
        }
    }

    //------------------------------------------------------------------------------
    // E’-> opsuma M  E’  | ϵ 
    
    private void Ep ( Atributos Ep ) {
        // Variables locales
        Atributos M = new Atributos();
        Atributos Ep1 = new Atributos();
        Linea_BE opsum = new Linea_BE();
        
        if ( preAnalisis.equals ( "opsuma" ) ) {
          // E’-> opsuma M  E’
          opsum = cmp.be.preAnalisis;
          emparejar ( "opsuma" );
          M ( M );
          // Acción Semántica 16
          Ep1.h = tempnuevo();
          emite ( Ep1.h + " := " + Ep.h + opsum.lexema + M.lugar );
          cmp.cua.insertar(new Cuadruplo ( opsum.lexema, Ep.h, M.lugar, Ep1.h ) );
          // Fin Acción Semántica 16
          
          // Acción Semántica 17
          Ep.lugar = Ep1.lugar;
          Ep.h = Ep1.h;
          // Fin Acción Semántica 17
          
          Ep ( Ep );
          
        } else {
            // E' -> ϵ 
            
            // Acción Semántica 18
            Ep.lugar = Ep.h;
            // Fin Acción Semántica 18
        }   
    }    

    //------------------------------------------------------------------------------
    // M -> F  M’ 
    
    private void M ( Atributos M ) {
        // Variables locales
        Atributos F = new Atributos();
        Atributos Mp = new Atributos();
        Polaca expr = new Polaca();
        String exprPrefija = "";
        String exprInfija = "";
        
        if ( preAnalisis.equals ( "id" ) ||
             preAnalisis.equals ( "num" ) || 
             preAnalisis.equals ( "num.num" ) ||
             preAnalisis.equals ( "(" ) ) {
            // M -> F  M’ 
            
            F ( F );
             
            // Acción Semántica 14
            Mp.h = F.lugar;
            // Fin Acción Semántica 14
            
            Mp ( Mp );
            
            // Acción Semántica 15
            exprPrefija = expr.notacionPolaca ( exprInfija );
            M.lugar = Mp.h;
            //emite(M.lugar + " := " + Mp.h);
            // Fin Acción Semántica 15
            
        } else {
            error ( "[M] Se esperaba el termino de una expresión. "  +
                    "No. Línea " + cmp.be.preAnalisis.numLinea );
        }     
    }
    
    //------------------------------------------------------------------------------    
    // M’ -> opmult   F  M’  | ϵ 
    
    private void Mp ( Atributos Mp ) {
        // Variables locales
        Atributos F = new Atributos();
        Atributos Mp1 = new Atributos();
        Linea_BE opmult = new Linea_BE();
        
        if ( preAnalisis.equals ( "opmult" ) ) {
          // M’-> opmult   F  M’
          opmult = cmp.be.preAnalisis;
          emparejar ( "opmult" );
          F ( F );
          
          // Acción Semántica 19
          Mp1.h = tempnuevo();
          emite ( Mp1.h + " := " + Mp.h + opmult.lexema + F.lugar );
          cmp.cua.insertar(new Cuadruplo ( opmult.lexema, Mp.h, F.lugar, Mp1.h ) );
          // Fin Acción Semántica 19
          
          // Acción Semántica 20
          Mp.h = Mp1.h;
          // Fin Acción Semántica 20
          
          Mp ( Mp );
        } else {
            // M' -> ϵ 
            
            // Acción Semántica 21
            Mp.lugar = Mp.h;
            // Fin Acción Semántica 21
        }         
    }
    
    //------------------------------------------------------------------------------
    // F -> id  | num | num.num  |  ( E )
    
    private void F ( Atributos F ) {
        // Variables locales
        Atributos E = new Atributos();
        Linea_BE id = new Linea_BE();
        Linea_BE num = new Linea_BE();
        Linea_BE num_num = new Linea_BE();
        
        if ( preAnalisis.equals("num")) {
            // F -> num
            num = cmp.be.preAnalisis;
            emparejar ("num");
            
            // Acción Semántica 23
            p = cmp.ts.buscar ( num.lexema );
            
            if ( p != NIL ) {
                F.lugar = num.lexema;
            } else {
                error ( "[F] Id redeclarado" );
            }
            // Fin Acción Semántica 23
            
        } else if ( preAnalisis.equals("num.num")) {
            // F -> num.num
            num_num = cmp.be.preAnalisis;
            emparejar ("num.num");
            
            // Acción Semántica 24
            p = cmp.ts.buscar ( num_num.lexema );
            
            if ( p != NIL ) {
                F.lugar = num_num.lexema;
            } else {
                error ( "[F] Id redeclarado" );
            }
            // Fin Acción Semántica 24
            
        } else if ( preAnalisis.equals("id")) {
            // F -> id
            id = cmp.be.preAnalisis;
            emparejar("id");
            
            // Acción Semántica 22
            p = cmp.ts.buscar ( id.lexema );
            
            if ( p != NIL ) {
                F.lugar = id.lexema;
            } else {
                error ( "[F] Id redeclarado" );
            }
            // Fin Acción Semántica 22
            
        } else if ( preAnalisis.equals("(") ) {
            // F -> ( E )
            emparejar("(");
            E ( E );
            emparejar(")");
            
            // Acción Semántica 25
            F.lugar = E.lugar;
            // Fin Acción Semántica 25
            
        } else {
            error("[F] Expresión mal formada. " +
                  "No. Línea " + cmp.be.preAnalisis.numLinea );
        }
    }

    //------------------------------------------------------------------------------
    
}
